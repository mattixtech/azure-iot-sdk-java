/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.DeliveryOutcome;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.MessageSentCallback;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.*;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.message.Message;

import java.nio.BufferOverflowException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Instance of the QPID-Proton-J BaseHandler class to override
 * the events what are needed to handle the send operation
 * Contains and sets connection parameters (path, port, endpoint)
 * Maintains the layers of AMQP protocol (Link, Session, Connection, Transport)
 * Creates and sets SASL authentication for transport
 */
@Slf4j
public class AmqpSendHandler extends AmqpConnectionHandler
{
    public static final String SEND_TAG = "sender";
    public static final String ENDPOINT = "/messages/devicebound";
    public static final String DEVICE_PATH_FORMAT = "/devices/%s/messages/devicebound";
    public static final String MODULE_PATH_FORMAT = "/devices/%s/modules/%s/messages/devicebound";
    private static final String THREAD_POSTFIX_NAME = "SendHandler";
    private Queue<TransportMessage> messageQueue;
    private final Map<Integer, TransportMessage> inProgressMessages;
    private static final int expectedLinkCount = 1;
    private Sender cloudToDeviceMessageSender;
    private static final int SEND_MESSAGES_PERIOD_MILLIS = 50; //every 50 milliseconds, the method onTimerTask will fire to send queued messages

    /**
     * Constructor to set up connection parameters and initialize handshaker for transport
     *
     * @param hostName The address string of the service (example: AAA.BBB.CCC)
     * @param userName The username string to use SASL authentication (example: user@sas.service)
     * @param iotHubServiceClientProtocol protocol to use
     */
    public AmqpSendHandler(String hostName, String userName, IotHubServiceClientProtocol iotHubServiceClientProtocol)
    {
        super(hostName, userName, iotHubServiceClientProtocol, SEND_TAG, ENDPOINT, expectedLinkCount);

        this.messageQueue = new ConcurrentLinkedQueue<>();
        this.inProgressMessages = new ConcurrentHashMap<>();
    }

    /**
     * Queue the provided message so that it is sent to the provided device
     * @param deviceId The device name string
     * @param message The message to be sent to the device
     */
    public void queueMessage(com.microsoft.azure.sdk.iot.service.Message message, String deviceId, MessageSentCallback messageSentCallback, Object context)
    {
        this.messageQueue.add(new TransportMessage(deviceId, null, message, messageSentCallback, context));
    }

    /**
     * Queue the provided message so that it is sent to the provided module in the provided device
     * @param deviceId The device identifier
     * @param moduleId The module identifier
     * @param message The message to be sent to the module
     */
    public void queueMessage(com.microsoft.azure.sdk.iot.service.Message message, String deviceId, String moduleId, MessageSentCallback messageSentCallback, Object context)
    {
        this.messageQueue.add(new TransportMessage(deviceId, moduleId, message, messageSentCallback, context));
    }

    private Message convertToProtonMessage(String targetPath, com.microsoft.azure.sdk.iot.service.Message message)
    {
        // Codes_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_005: [The function shall create a new Message (Proton) object]
        org.apache.qpid.proton.message.Message protonMessage = Proton.message();

        // Codes_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_006: [The function shall set
        // the standard properties on the Proton Message object]
        Properties properties = new Properties();
        properties.setMessageId(message.getMessageId());
        properties.setTo(targetPath);
        properties.setAbsoluteExpiryTime(message.getExpiryTimeUtc());
        properties.setCorrelationId(message.getCorrelationId());
        if (message.getUserId() != null)
        {
            properties.setUserId(new Binary(message.getUserId().getBytes()));
        }
        protonMessage.setProperties(properties);

        // Codes_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_023: [The function shall set
        // the application properties on the Proton Message object]
        if (message.getProperties() != null && message.getProperties().size() > 0)
        {
            Map<String, Object> applicationPropertiesMap = new HashMap<>(message.getProperties().size());
            for(Map.Entry<String, String> entry : message.getProperties().entrySet())
            {
                applicationPropertiesMap.put(entry.getKey(), entry.getValue());
            }
            ApplicationProperties applicationProperties = new ApplicationProperties(applicationPropertiesMap);
            protonMessage.setApplicationProperties(applicationProperties);
        }

        // Codes_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_007: [The function shall create a Binary (Proton) object from the content string]
        Binary binary = new Binary(message.getBytes());
        // Codes_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_008: [The function shall create a data Section (Proton) object from the Binary]
        Section section = new Data(binary);
        // Codes_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_009: [The function shall set the Message body to the created data section]
        protonMessage.setBody(section);

        return protonMessage;
    }

    /**
     * Event handler for the link flow event
     * @param event The proton event object
     */
    @Override
    public void onLinkFlow(Event event)
    {
        sendQueuedMessages();
    }

    @Override
    public void onLinkRemoteOpen(Event event)
    {
        super.onLinkRemoteOpen(event);
        event.getReactor().schedule(SEND_MESSAGES_PERIOD_MILLIS, this);
    }

    private void sendQueuedMessages()
    {
        synchronized (stateChangeLock)
        {
            TransportMessage messageToSend = messageQueue.poll();
            if (messageToSend != null)
            {
                sendMessage(messageToSend, cloudToDeviceMessageSender);
            }
        }
    }

    private void sendMessage(TransportMessage transportMessage, Sender sender)
    {
        log.debug("Sending amqp message with correlation id {}", transportMessage.message.getCorrelationId());

        Message message;
        if (transportMessage.moduleId == null)
        {
            message =  convertToProtonMessage(String.format(DEVICE_PATH_FORMAT, transportMessage.deviceId), transportMessage.message);
        }
        else
        {
            message =  convertToProtonMessage(String.format(MODULE_PATH_FORMAT, transportMessage.deviceId, transportMessage.moduleId), transportMessage.message);
        }

        byte[] msgData = new byte[1024];
        int length;
        while (true)
        {
            try
            {
                length = message.encode(msgData, 0, msgData.length);
                break;
            }
            catch (BufferOverflowException e)
            {
                msgData = new byte[msgData.length * 2];
            }
        }
        byte[] tag = String.valueOf(nextSendTag).getBytes();

        this.inProgressMessages.put(this.nextSendTag, transportMessage);

        //want to avoid negative delivery tags since -1 is the designated failure value
        if (this.nextSendTag == Integer.MAX_VALUE || this.nextSendTag < 0)
        {
            this.nextSendTag = 0;
        }
        else
        {
            this.nextSendTag++;
        }

        Delivery dlv = sender.delivery(tag);
        sender.send(msgData, 0, length);
        sender.advance();
    }

    @Override
    public void onTimerTask(Event event)
    {
        sendQueuedMessages();

        event.getReactor().schedule(SEND_MESSAGES_PERIOD_MILLIS, this);
    }

    @Override
    public DeliveryOutcome onMessageArrived(Message message)
    {
        // should never be called since this is a sending only connection. Any received messages should be rejected
        return DeliveryOutcome.Reject;
    }

    @Override
    public void onMessageAcknowledged(MessageSentCallback.AcknowledgementState acknowledgementState, String statusCode, String statusDescription, int deliveryTag)
    {
        TransportMessage correspondingTransportMessage = this.inProgressMessages.remove(deliveryTag);

        if (correspondingTransportMessage == null)
        {
            log.warn("Received acknowledgement for a message that this sender did not send, or that it sent before closing. Ignoring it");
            return;
        }

        correspondingTransportMessage.messageSentCallback.onMessageSent(acknowledgementState, statusCode, statusDescription, correspondingTransportMessage.context);
    }

    @Override
    public void openLinks(Session session, Map<Symbol, Object> properties)
    {
        log.debug("Opening links for sending messages");
        cloudToDeviceMessageSender = session.sender(tag);
        cloudToDeviceMessageSender.setProperties(properties);
        cloudToDeviceMessageSender.open();
    }

    @Override
    public void closeLinks()
    {
        if (cloudToDeviceMessageSender != null)
        {
            log.debug("Closing cloud to device message sender link");
            cloudToDeviceMessageSender.close();
        }
    }

    @Override
    public void onLinkInit(Event event)
    {
        Link link = event.getLink();
        Target t = new Target();
        t.setAddress(endpoint);
        link.setTarget(t);
    }

    @Override
    public String getThreadNamePostfix()
    {
        return THREAD_POSTFIX_NAME;
    }

    @Override
    public void close()
    {
        //super.close() also needs to grab this lock, but grabbing a lock within a synchronized block of the same lock is a no-op, so it is safe to grab here
        synchronized (this.stateChangeLock)
        {
            super.close();

            //After all links/sessions/connections are closed, all queued and in progress messages should callback with Released to signal they weren't processed
            for (int deliveryTag : this.inProgressMessages.keySet())
            {
                TransportMessage abandonedMessage = this.inProgressMessages.get(deliveryTag);
                abandonedMessage.messageSentCallback.onMessageSent(MessageSentCallback.AcknowledgementState.Released, "", "", null);
            }

            this.inProgressMessages.clear();

            for (TransportMessage unsentMessage : this.messageQueue)
            {
                unsentMessage.messageSentCallback.onMessageSent(MessageSentCallback.AcknowledgementState.Released, "", "", null);
            }

            this.messageQueue.clear();
        }
    }
}

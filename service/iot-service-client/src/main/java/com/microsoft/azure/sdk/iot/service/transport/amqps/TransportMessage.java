/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.Message;
import com.microsoft.azure.sdk.iot.service.MessageSentCallback;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class TransportMessage
{
    String deviceId;
    String moduleId;

    @Getter
    Message message;

    MessageSentCallback messageSentCallback;

    Object context;
}

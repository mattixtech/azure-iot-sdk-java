/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpResponseVerification;

public interface MessageSentCallback
{
    public void onMessageSent(AcknowledgementState acknowledgementState, String errorCode, String errorDescription, Object context);

    public enum AcknowledgementState
    {
        Accepted,
        Released,
        Rejected,
        Modified,
        Received
    }
}

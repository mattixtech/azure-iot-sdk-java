/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.helpers;

import org.junit.Before;

import static org.junit.Assume.assumeTrue;

public class IotHubIntegrationTest extends IntegrationTest
{
    @Override
    public boolean isProvisioningTest()
    {
        return false;
    }

    @Override
    public boolean isIotHubTest()
    {
        return true;
    }

    protected static String iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
    protected static String storageAccountConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.STORAGE_ACCOUNT_CONNECTION_STRING_ENV_VAR_NAME);
}
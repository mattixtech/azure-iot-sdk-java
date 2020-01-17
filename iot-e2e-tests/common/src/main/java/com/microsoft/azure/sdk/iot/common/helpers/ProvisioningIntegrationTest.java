/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.helpers;

import org.junit.Before;

import static org.junit.Assume.assumeTrue;

public class ProvisioningIntegrationTest extends IntegrationTest
{
    public static final String FAR_AWAY_IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "FAR_AWAY_IOTHUB_CONNECTION_STRING";
    public static final String CUSTOM_ALLOCATION_WEBHOOK_URL_VAR_NAME = "CUSTOM_ALLOCATION_POLICY_WEBHOOK";
    public static final String DPS_CONNECTION_STRING_ENV_VAR_NAME = "IOT_DPS_CONNECTION_STRING";
    public static final String DPS_CONNECTION_STRING_WITH_INVALID_CERT_ENV_VAR_NAME = "PROVISIONING_CONNECTION_STRING_INVALIDCERT";
    public static final String DPS_GLOBAL_ENDPOINT_WITH_INVALID_CERT_ENV_VAR_NAME = "DPS_GLOBALDEVICEENDPOINT_INVALIDCERT";
    public static final String DPS_ID_SCOPE_ENV_VAR_NAME = "IOT_DPS_ID_SCOPE";

    @Override
    public boolean isProvisioningTest()
    {
        return true;
    }

    @Override
    public boolean isIotHubTest()
    {
        return false;
    }

    protected static String iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);

    public static String farAwayIotHubConnectionString = Tools.retrieveEnvironmentVariableValue(FAR_AWAY_IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);;
    public static String customAllocationWebhookUrl = Tools.retrieveEnvironmentVariableValue(CUSTOM_ALLOCATION_WEBHOOK_URL_VAR_NAME);;
    public static String provisioningServiceConnectionString = Tools.retrieveEnvironmentVariableValue(DPS_CONNECTION_STRING_ENV_VAR_NAME);
    public static String provisioningServiceWithInvalidCertConnectionString = Tools.retrieveEnvironmentVariableValue(DPS_CONNECTION_STRING_WITH_INVALID_CERT_ENV_VAR_NAME);;
    public static String provisioningServiceGlobalEndpoint = "global.azure-devices-provisioning.net";
    public static String provisioningServiceGlobalEndpointWithInvalidCert = Tools.retrieveEnvironmentVariableValue(DPS_GLOBAL_ENDPOINT_WITH_INVALID_CERT_ENV_VAR_NAME);;
    public static String provisioningServiceIdScope = Tools.retrieveEnvironmentVariableValue(DPS_ID_SCOPE_ENV_VAR_NAME);;
}

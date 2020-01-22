// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestDigitalTwinDevice;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance1;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceAsyncClient;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceAsyncClientImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClientImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.models.DigitalTwinCommandResponse;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT_WS;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_OK;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.serialize;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants.*;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.*;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.EventHubListener.verifyThatMessageWasReceived;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.synchronizedList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RunWith(Parameterized.class)
public class DigitalTwinCommandE2ETests {
    private static final String IOT_HUB_CONNECTION_STRING = retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
    private static final String TEST_INTERFACE_INSTANCE_NAME_1 = retrieveInterfaceNameFromInterfaceId(TestInterfaceInstance1.TEST_INTERFACE_ID);
    private static final String TEST_INTERFACE_INSTANCE_NAME_2 = retrieveInterfaceNameFromInterfaceId(TEST_INTERFACE_ID);

    private static final String DEVICE_ID_PREFIX = "DigitalTwinCommandE2ETests_";

    private static final String SAMPLE_COMMAND_STRING_PAYLOAD = "\"samplePayload\"";
    private static final String SAMPLE_COMMAND_JSON_PAYLOAD = "{\"desc\":\"abc\"}";
    private static final String SAMPLE_COMMAND_INT_PAYLOAD = "5";
    private static final String INVALID_INTERFACE_INSTANCE_NAME = "invalidInterfaceInstanceName";
    private static final String INVALID_COMMAND_NAME = "invalidCommandName";
    private static final String INTERFACE_INSTANCE_NOT_FOUND_MESSAGE_PATTERN = "Interface instance [%s] not found.";

    private static DigitalTwinServiceClient digitalTwinServiceClient;
    private static DigitalTwinServiceAsyncClient digitalTwinServiceAsyncClient;
    private TestDigitalTwinDevice testDevice;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(5 * 60); // 5 minutes max per method tested

    @Parameterized.Parameter(0)
    public IotHubClientProtocol protocol;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return asList(new Object[][]{
                {MQTT},
                {MQTT_WS},
        });
    }

    @BeforeClass
    public static void setUp() {
    }


    @Test
    public void testDeviceClientReceivesSyncCommandWithStringPayloadAndResponds() throws IOException, IotHubException, URISyntaxException {

    }

    @After
    public void tearDownTest() {
    }
}

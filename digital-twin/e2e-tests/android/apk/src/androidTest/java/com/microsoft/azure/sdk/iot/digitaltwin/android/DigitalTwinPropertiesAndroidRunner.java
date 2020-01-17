/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.digitaltwin.android;

import com.microsoft.appcenter.espresso.Factory;
import com.microsoft.appcenter.espresso.ReportHelper;
import com.microsoft.azure.sdk.iot.digitaltwin.android.helper.TestGroupDigitalTwin1;
import com.microsoft.azure.sdk.iot.digitaltwin.android.helper.TestGroupDigitalTwin2;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests.DigitalTwinPropertiesE2ETests;

import org.junit.After;
import org.junit.Rule;
import org.junit.rules.Timeout;

@TestGroupDigitalTwin2
public class DigitalTwinPropertiesAndroidRunner extends DigitalTwinPropertiesE2ETests {
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5 * 60); // 5 minutes max per method tested

    @Rule
    public ReportHelper reportHelper = Factory.getReportHelper();

    @After
    public void labelSnapshot()
    {
        reportHelper.label("Stopping Digital Twin E2E App");
    }
}

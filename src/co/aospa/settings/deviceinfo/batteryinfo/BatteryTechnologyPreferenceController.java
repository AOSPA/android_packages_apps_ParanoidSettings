/*
 * SPDX-FileCopyrightText: 2024 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.deviceinfo.batteryinfo;

import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.fuelgauge.BatteryUtils;

/**
 * A controller that manages the information about battery technology.
 */
public class BatteryTechnologyPreferenceController extends BasePreferenceController {

    public BatteryTechnologyPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public CharSequence getSummary() {
        final Intent batteryIntent = BatteryUtils.getBatteryIntent(mContext);
        final String technology = batteryIntent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);

        return technology != null
                ? technology
                : mContext.getText(R.string.battery_technology_not_available);
    }
}

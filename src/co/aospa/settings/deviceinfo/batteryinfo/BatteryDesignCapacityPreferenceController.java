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
 * A controller that manages the information about battery design capacity.
 */
public class BatteryDesignCapacityPreferenceController extends BasePreferenceController {

    public BatteryDesignCapacityPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public CharSequence getSummary() {
        Intent batteryIntent = BatteryUtils.getBatteryIntent(mContext);
        final int designCapacityUah =
                batteryIntent.getIntExtra(BatteryManager.EXTRA_DESIGN_CAPACITY, -1);

        if (designCapacityUah > 0) {
            int designCapacity = designCapacityUah / 1_000;
            return mContext.getString(R.string.battery_design_capacity_summary, designCapacity);
        }

        return mContext.getString(R.string.battery_design_capacity_not_available);
    }
}

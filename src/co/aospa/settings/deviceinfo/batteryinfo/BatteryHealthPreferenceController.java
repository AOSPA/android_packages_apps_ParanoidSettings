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
 * A controller that manages the information about battery health.
 */
public class BatteryHealthPreferenceController extends BasePreferenceController {

    public BatteryHealthPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public CharSequence getSummary() {
        final Intent batteryIntent = BatteryUtils.getBatteryIntent(mContext);
        final int health =
                batteryIntent.getIntExtra(
                        BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN);

        switch (health) {
            case BatteryManager.BATTERY_HEALTH_GOOD:
                return mContext.getString(R.string.battery_health_good);
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                return mContext.getString(R.string.battery_health_overheat);
            case BatteryManager.BATTERY_HEALTH_DEAD:
                return mContext.getString(R.string.battery_health_dead);
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                return mContext.getString(R.string.battery_health_over_voltage);
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                return mContext.getString(R.string.battery_health_unspecified_failure);
            case BatteryManager.BATTERY_HEALTH_COLD:
                return mContext.getString(R.string.battery_health_cold);
            default:
                return mContext.getString(R.string.battery_health_unknown);
        }
    }
}

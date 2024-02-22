/*
 * SPDX-FileCopyrightText: 2024 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.deviceinfo.batteryinfo;

import android.content.Context;
import android.content.Intent;
import android.icu.text.MeasureFormat;
import android.icu.util.Measure;
import android.icu.util.MeasureUnit;
import android.os.BatteryManager;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.fuelgauge.BatteryUtils;

import java.util.Locale;

/**
 * A controller that manages the information about battery temperature.
 */
public class BatteryTemperaturePreferenceController extends BasePreferenceController {

    public BatteryTemperaturePreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public CharSequence getSummary() {
        final Intent batteryIntent = BatteryUtils.getBatteryIntent(mContext);
        final int temperatureTenths =
                batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);

        if (temperatureTenths > 0) {
            float temperature = temperatureTenths / 10f;

            return MeasureFormat.getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.SHORT)
                    .format(new Measure(temperature, MeasureUnit.CELSIUS));
        }

        return mContext.getText(R.string.battery_temperature_not_available);
    }
}

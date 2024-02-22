/*
 * SPDX-FileCopyrightText: 2024 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.deviceinfo.batteryinfo

import android.content.Context
import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.os.BatteryManager
import com.android.settings.R
import com.android.settings.core.BasePreferenceController
import com.android.settingslib.fuelgauge.BatteryUtils
import java.util.Locale

/**
 * A controller that manages the information about battery temperature.
 */
class BatteryTemperaturePreferenceController(
    context: Context,
    preferenceKey: String
) : BasePreferenceController(context, preferenceKey) {

    override fun getAvailabilityStatus(): Int = AVAILABLE

    override fun getSummary(): CharSequence {
        val batteryIntent = BatteryUtils.getBatteryIntent(mContext)
        val temperatureTenths = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)

        return if (temperatureTenths > 0) {
            val temperature = temperatureTenths / 10f
            MeasureFormat.getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.SHORT)
                .format(Measure(temperature, MeasureUnit.CELSIUS))
        } else {
            mContext.getText(R.string.battery_temperature_not_available)
        }
    }
}

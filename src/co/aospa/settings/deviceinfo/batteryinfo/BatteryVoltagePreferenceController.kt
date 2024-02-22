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
 * A controller that manages the information about battery voltage.
 */
class BatteryVoltagePreferenceController(
    context: Context,
    preferenceKey: String
) : BasePreferenceController(context, preferenceKey) {

    override fun getAvailabilityStatus(): Int = AVAILABLE

    override fun getSummary(): CharSequence {
        val batteryIntent = BatteryUtils.getBatteryIntent(mContext)
        val voltageMillivolts = batteryIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)

        return if (voltageMillivolts > 0) {
            val voltage = voltageMillivolts / 1_000f
            MeasureFormat.getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.SHORT)
                .format(Measure(voltage, MeasureUnit.VOLT))
        } else {
            mContext.getText(R.string.battery_voltage_not_available)
        }
    }
}

/*
 * SPDX-FileCopyrightText: 2024 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.deviceinfo.firmwareversion

import android.content.Context
import android.os.SystemProperties

import com.android.settings.R

class BasebandVersionPreferenceController(
    context: Context,
    preferenceKey: String
) : com.android.settings.deviceinfo.firmwareversion
        .BasebandVersionPreferenceController(context, preferenceKey) {

    private val mContext = context

    override fun getSummary(): CharSequence  {
        val baseBands = SystemProperties.get(BASEBAND_PROPERTY,
                mContext.getString(R.string.device_info_default)).split(",")
        return baseBands[0]
    }

    companion object {
        private const val BASEBAND_PROPERTY = "gsm.version.baseband"
    }
}

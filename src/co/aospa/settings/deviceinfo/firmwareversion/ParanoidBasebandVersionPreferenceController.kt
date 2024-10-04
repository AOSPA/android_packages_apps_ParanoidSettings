/*
 * SPDX-FileCopyrightText: 2024 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.deviceinfo.firmwareversion

import android.content.Context
import android.os.SystemProperties
import com.android.settings.R
import com.android.settings.deviceinfo.firmwareversion.BasebandVersionPreferenceController

class ParanoidBasebandVersionPreferenceController(
    context: Context,
    preferenceKey: String
) : BasebandVersionPreferenceController(context, preferenceKey) {

    private val mContext = context

    override fun getSummary(): CharSequence {
        return SystemProperties.get(BASEBAND_PROPERTY, "")
                .split(",")
                .firstOrNull()
                ?: mContext.getString(R.string.device_info_default)
    }

    companion object {
        private const val BASEBAND_PROPERTY = "gsm.version.baseband"
    }
}

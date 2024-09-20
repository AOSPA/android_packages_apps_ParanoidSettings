/*
 * SPDX-FileCopyrightText: 2024 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.deviceinfo.firmwareversion

import android.content.Context
import android.os.SystemProperties
import com.android.settings.R
import com.android.settings.core.BasePreferenceController

class ParanoidAndroidVersionPreferenceController(
    context: Context,
    preferenceKey: String
) : BasePreferenceController(context, preferenceKey) {

    override fun getAvailabilityStatus(): Int = AVAILABLE_UNSEARCHABLE

    override fun getSummary(): CharSequence =
        SystemProperties.get(
            PARANOID_ANDROID_VERSION_PROP,
            mContext.getString(R.string.device_info_default)
        )

    companion object {
        private const val PARANOID_ANDROID_VERSION_PROP = "ro.aospa.version"
    }
}

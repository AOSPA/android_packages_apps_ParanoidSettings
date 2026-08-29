/*
 * SPDX-FileCopyrightText: 2026 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.deviceinfo

import android.content.Context
import com.android.settings.Utils
import com.android.settings.core.BasePreferenceController

/**
 * Controller to manage the visibility of the Radio Info (*#*#4636#*#*) preference.
 */
class RadioInfoPreferenceController(
    context: Context,
    preferenceKey: String
) : BasePreferenceController(context, preferenceKey) {

    override fun getAvailabilityStatus(): Int =
        if (Utils.isMobileDataCapable(mContext) || Utils.isVoiceCapable(mContext)) {
            AVAILABLE
        } else {
            UNSUPPORTED_ON_DEVICE
        }
}

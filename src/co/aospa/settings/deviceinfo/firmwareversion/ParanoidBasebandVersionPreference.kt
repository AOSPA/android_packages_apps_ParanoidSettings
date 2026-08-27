/*
 * SPDX-FileCopyrightText: 2024-2026 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.deviceinfo.firmwareversion

import android.content.Context
import android.os.SystemProperties
import androidx.preference.Preference
import com.android.settings.R
import com.android.settings.Utils
import com.android.settingslib.datastore.KeyValueStore
import com.android.settingslib.metadata.PersistentPreference
import com.android.settingslib.metadata.PreferenceAvailabilityProvider
import com.android.settingslib.metadata.PreferenceMetadata
import com.android.settingslib.metadata.PreferenceSummaryProvider
import com.android.settingslib.metadata.SensitivityLevel
import com.android.settingslib.metadata.preferencesapi.preconditions.PreconditionStability
import com.android.settingslib.preference.PreferenceBinding

class ParanoidBasebandVersionPreference :
    PersistentPreference<String>,
    PreferenceMetadata,
    PreferenceSummaryProvider,
    PreferenceAvailabilityProvider,
    PreferenceBinding {

    override val key: String
        get() = "base_band"

    override val purpose: Int
        get() = R.string.base_band_purpose

    override val title: Int
        get() = R.string.baseband_version

    override val supportsWrite = false

    override val valueType = String::class.javaObjectType

    override fun storage(context: Context): KeyValueStore = createSummaryStorage(context, key)

    override fun getSummary(context: Context): CharSequence? =
        SystemProperties.get(BASEBAND_PROPERTY, "")
            .split(",")
            .firstOrNull()
            ?.ifEmpty { null }
            ?: context.getString(R.string.device_info_default)

    override val availabilityDescription =
        "The device must be mobile data capable or voice capable."

    override fun getAvailabilityStability() = PreconditionStability.STABLE_UNTIL_APK_UPDATE

    override fun isAvailable(context: Context) =
        Utils.isMobileDataCapable(context) || Utils.isVoiceCapable(context)

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        preference.isSelectable = false
        preference.isCopyingEnabled = true
    }

    override val sensitivityLevel
        get() = SensitivityLevel.NO_SENSITIVITY

    companion object {
        private const val BASEBAND_PROPERTY: String = "gsm.version.baseband"
    }
}

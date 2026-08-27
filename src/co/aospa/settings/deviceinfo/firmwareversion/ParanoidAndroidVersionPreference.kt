/*
 * SPDX-FileCopyrightText: 2024-2026 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.deviceinfo.firmwareversion

import android.content.Context
import android.os.SystemProperties
import androidx.preference.Preference
import com.android.settings.R
import com.android.settingslib.datastore.KeyValueStore
import com.android.settingslib.metadata.PersistentPreference
import com.android.settingslib.metadata.PreferenceMetadata
import com.android.settingslib.metadata.PreferenceSummaryProvider
import com.android.settingslib.metadata.SensitivityLevel
import com.android.settingslib.preference.PreferenceBinding

class ParanoidAndroidVersionPreference :
    PersistentPreference<String>,
    PreferenceMetadata,
    PreferenceSummaryProvider,
    PreferenceBinding {

    override val key: String
        get() = "aospa_version"

    override val title: Int
        get() = R.string.aospa_version

    override val indexable
        get() = false

    override val supportsWrite = false

    override val valueType = String::class.javaObjectType

    override fun storage(context: Context): KeyValueStore = createSummaryStorage(context, key)

    override fun getSummary(context: Context): CharSequence? =
        SystemProperties.get(
            PARANOID_ANDROID_VERSION_PROP,
            context.getString(R.string.device_info_default)
        )

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        preference.isCopyingEnabled = true
    }

    override val sensitivityLevel
        get() = SensitivityLevel.NO_SENSITIVITY

    companion object {
        private const val PARANOID_ANDROID_VERSION_PROP = "ro.aospa.version"
    }
}

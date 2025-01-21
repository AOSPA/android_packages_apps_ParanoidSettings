/*
 * SPDX-FileCopyrightText: 2022 The Android Open Source Project
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.biometrics.fingerprint;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.Preference;

import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.Utils;
import com.android.settings.biometrics.fingerprint.FingerprintSettings;
import com.android.settings.biometrics.fingerprint.FingerprintSettingsPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

@SearchIndexable
public class FingerprintSettingsScreenOffUnlockUdfpsPreferenceController
        extends FingerprintSettingsPreferenceController {
    private static final String TAG = "FingerprintSettingsScreenOffUnlockUdfpsPreferenceController";

    @VisibleForTesting
    protected FingerprintManager mFingerprintManager;

    public FingerprintSettingsScreenOffUnlockUdfpsPreferenceController(
            Context context, String prefKey) {
        super(context, prefKey);
        mFingerprintManager = Utils.getFingerprintManagerOrNull(context);
    }

    @Override
    public boolean isChecked() {
        if (!FingerprintSettings.isFingerprintHardwareDetected(mContext)) {
            return false;
        } else if (getRestrictingAdmin() != null) {
            return false;
        }

        return Settings.Secure.getIntForUser(
                mContext.getContentResolver(), "screen_off_udfps_enabled", 0, getUserHandle()) == 1;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        Settings.Secure.putIntForUser(
                mContext.getContentResolver(),
                "screen_off_udfps_enabled",
                isChecked ? 1 : 0,
                getUserHandle());
        return true;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (!FingerprintSettings.isFingerprintHardwareDetected(mContext)) {
            preference.setEnabled(false);
        } else if (!mFingerprintManager.hasEnrolledTemplates(getUserId())) {
            preference.setEnabled(false);
        } else if (getRestrictingAdmin() != null) {
            preference.setEnabled(false);
        } else {
            preference.setEnabled(true);
        }
    }

    @Override
    public int getAvailabilityStatus() {
        if (mFingerprintManager != null
                && mFingerprintManager.isHardwareDetected()
                && !mFingerprintManager.isPowerbuttonFps()) {
            return mFingerprintManager.hasEnrolledTemplates(getUserId())
                    ? AVAILABLE : CONDITIONALLY_UNAVAILABLE;
        } else {
            return UNSUPPORTED_ON_DEVICE;
        }
    }

    private int getUserHandle() {
        return UserHandle.of(getUserId()).getIdentifier();
    }

    /**
     * This feature is not directly searchable.
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {};

}

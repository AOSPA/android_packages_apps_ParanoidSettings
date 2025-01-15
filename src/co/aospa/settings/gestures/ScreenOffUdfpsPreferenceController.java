/*
 * SPDX-FileCopyrightText: 2022 The Android Open Source Project
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.gestures;

import android.annotation.UserIdInt;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.hardware.display.AmbientDisplayConfiguration;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import com.android.settingslib.PrimarySwitchPreference;

public class ScreenOffUdfpsPreferenceController extends TogglePreferenceController
        implements LifecycleObserver, OnStart, OnStop {

    private static final int ON = 1;
    private static final int OFF = 0;

    private static final String SECURE_KEY = "screen_off_udfps_enabled";

    private AmbientDisplayConfiguration mAmbientConfig;
    @UserIdInt
    private final int mUserId;

    private PrimarySwitchPreference mPreference;
    private SettingObserver mSettingObserver;

    public ScreenOffUdfpsPreferenceController(Context context, String key) {
        super(context, key);
        mUserId = UserHandle.myUserId();
    }

    public ScreenOffUdfpsPreferenceController setConfig(AmbientDisplayConfiguration config) {
        mAmbientConfig = config;
        return this;
    }

    private static boolean screenOffUdfpsAvailable(AmbientDisplayConfiguration config) {
        return !TextUtils.isEmpty(config.udfpsLongPressSensorType());
    }

    public static boolean isSuggestionComplete(Context context, SharedPreferences prefs) {
        return isSuggestionComplete(new AmbientDisplayConfiguration(context), prefs);
    }

    @VisibleForTesting
    static boolean isSuggestionComplete(AmbientDisplayConfiguration config,
            SharedPreferences prefs) {
        return !screenOffUdfpsAvailable(config)
                || prefs.getBoolean(ScreenOffUdfpsSettings.PREF_KEY_SUGGESTION_COMPLETE, false);
    }

    @Override
    public int getAvailabilityStatus() {
        // No hardware support for Screen-Off UDFPS
        if (!screenOffUdfpsAvailable(mAmbientConfig)) {
            return UNSUPPORTED_ON_DEVICE;
        }

        return AVAILABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
        mSettingObserver = new SettingObserver(mPreference);
        updateState(mPreference);
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        final ContentResolver resolver = mContext.getContentResolver();
        final boolean enabled =
                Settings.Secure.getInt(resolver, SECURE_KEY, isChecked() ? ON : OFF) == ON;
        String summary;
        if (enabled) {
            summary = mContext.getString(R.string.gesture_setting_on);
        } else {
            summary = mContext.getString(R.string.gesture_setting_off);
        }
        preference.setSummary(summary);
    }

    @Override
    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "gesture_screen_off_udfps");
    }

    @Override
    public boolean isPublicSlice() {
        return true;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return Settings.Secure.putInt(mContext.getContentResolver(), SECURE_KEY,
                isChecked ? ON : OFF);
    }

    @Override
    public boolean isChecked() {
        return getAmbientConfig().screenOffUdfpsEnabled(mUserId);
    }

    private AmbientDisplayConfiguration getAmbientConfig() {
        if (mAmbientConfig == null) {
            mAmbientConfig = new AmbientDisplayConfiguration(mContext);
        }
        return mAmbientConfig;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return NO_RES;
    }

    @Override
    public void onStart() {
        if (mSettingObserver != null) {
            mSettingObserver.register(mContext.getContentResolver());
            mSettingObserver.onChange(false, null);
        }
    }

    @Override
    public void onStop() {
        if (mSettingObserver != null) {
            mSettingObserver.unregister(mContext.getContentResolver());
        }
    }

    private class SettingObserver extends ContentObserver {
        private final Uri mUri = Settings.Secure.getUriFor(SECURE_KEY);

        private final Preference mPreference;

        SettingObserver(Preference preference) {
            super(Handler.getMain());
            mPreference = preference;
        }

        public void register(ContentResolver cr) {
            cr.registerContentObserver(mUri, false, this);
        }

        public void unregister(ContentResolver cr) {
            cr.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (uri == null || mUri.equals(uri)) {
                updateState(mPreference);
            }
        }
    }
}

/*
 * SPDX-FileCopyrightText: 2023 Yet Another AOSP Project
 * SPDX-FileCopyrightText: 2024 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.gestures;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import co.aospa.framework.preference.SecureSettingSwitchPreference;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.widget.MainSwitchPreference;

public class DoubleTapScreenInsidePreferenceController extends AbstractPreferenceController
        implements PreferenceControllerMixin, OnCheckedChangeListener {

    private static final String KEY = "gesture_double_tap_screen";

    private final Context mContext;
    private MainSwitchPreference mSwitch;

    public DoubleTapScreenInsidePreferenceController(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public String getPreferenceKey() {
        return KEY;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mSwitch = screen.findPreference(getPreferenceKey());
        mSwitch.setOnPreferenceClickListener(preference -> {
            final boolean enabled = Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.DOZE_DOUBLE_TAP_GESTURE, 1) == 1;
            Settings.Secure.putInt(mContext.getContentResolver(),
                    Settings.Secure.DOZE_DOUBLE_TAP_GESTURE,
                    enabled ? 0 : 1);
            return true;
        });
        mSwitch.addOnSwitchChangeListener(this);
        updateState(mSwitch);
    }

    public void setChecked(boolean isChecked) {
        if (mSwitch != null) {
            mSwitch.updateStatus(isChecked);
        }
    }

    @Override
    public void updateState(Preference preference) {
        final boolean enabled = Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.DOZE_DOUBLE_TAP_GESTURE, 1) == 1;
        setChecked(enabled);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Settings.Secure.putInt(mContext.getContentResolver(),
                Settings.Secure.DOZE_DOUBLE_TAP_GESTURE, isChecked ? 1 : 0);
    }
}

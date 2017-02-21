/*
 * SPDX-FileCopyrightText: 2018 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.display;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import androidx.preference.SwitchPreference;
import androidx.preference.Preference;

import com.android.settings.DisplaySettings;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;

import static android.provider.Settings.System.POCKET_JUDGE;

public class PocketJudgePreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String KEY_POCKET_JUDGE = "pocket_judge";

    public PocketJudgePreferenceController(Context context) {
        super(context);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_POCKET_JUDGE;
    }

    @Override
    public void updateState(Preference preference) {
        int pocketJudgeValue = Settings.System.getInt(mContext.getContentResolver(),
                POCKET_JUDGE, 0);
        ((SwitchPreference) preference).setChecked(pocketJudgeValue != 0);
    }

    @Override
    public boolean isAvailable() {
        return mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_pocketModeSupported);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean pocketJudgeValue = (Boolean) newValue;
        Settings.System.putInt(mContext.getContentResolver(), POCKET_JUDGE, pocketJudgeValue ? 1 : 0);
        return true;
    }
}

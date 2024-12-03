/*
 * SPDX-FileCopyrightText: 2024 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.display;

import static android.hardware.display.DcDimmingManager.MODE_AUTO_OFF;
import static android.hardware.display.DcDimmingManager.MODE_AUTO_TIME;

import android.content.Context;
import android.hardware.display.DcDimmingManager;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import androidx.preference.DropDownPreference;
import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;
import com.android.settingslib.widget.MainSwitchPreference;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings screen for DC Dimming.
 */
@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class DcDimmingSettings extends DashboardFragment
        implements Preference.OnPreferenceChangeListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "DcDimmingSettings";

    private static final String KEY_MAIN_SWITCH = "dc_dimming_activated";
    private static final String KEY_AUTO_MODE = "dc_dimming_auto_mode";

    private DcDimmingManager mDcDimmingManager;

    private DropDownPreference mAutoPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context mContext = getContext();
        mDcDimmingManager = (DcDimmingManager) getSystemService(Context.DC_DIM_SERVICE);

        if (mDcDimmingManager == null || !mDcDimmingManager.isAvailable()) {
            return;
        }

        MainSwitchPreference mPreference = findPreference(KEY_MAIN_SWITCH);
        mPreference.addOnSwitchChangeListener(this);
        mPreference.updateStatus(mDcDimmingManager.isDcDimmingOn());

        mAutoPref = findPreference(KEY_AUTO_MODE);
        mAutoPref.setEntries(new CharSequence[]{
                mContext.getString(R.string.dark_ui_auto_mode_never),
                mContext.getString(R.string.dark_ui_auto_mode_auto)
        });
        mAutoPref.setEntryValues(new CharSequence[]{
                String.valueOf(MODE_AUTO_OFF),
                String.valueOf(MODE_AUTO_TIME)
        });
        mAutoPref.setValue(String.valueOf(mDcDimmingManager.getAutoMode()));
        mAutoPref.setOnPreferenceChangeListener(this);
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.dc_dimming_settings;
    }

    @Override
    public int getMetricsCategory() {
        return -1;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mAutoPref) {
            mDcDimmingManager.setAutoMode(Integer.parseInt((String) newValue));
        }
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mDcDimmingManager.setDcDimming(isChecked);
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    final ArrayList<SearchIndexableResource> result = new ArrayList<>();
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.dc_dimming_settings;
                    result.add(sir);
                    return result;
                }

                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    DcDimmingManager dm = (DcDimmingManager) context
                            .getSystemService(Context.DC_DIM_SERVICE);
                    return dm != null && dm.isAvailable();
                }

                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(
                        Context context) {
                    return new ArrayList<>(1);
                }
            };
}

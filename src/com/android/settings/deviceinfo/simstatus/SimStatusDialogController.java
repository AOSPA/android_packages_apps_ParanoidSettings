/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.deviceinfo.simstatus;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.telephony.Annotation;
import android.telephony.CarrierConfigManager;
import android.telephony.CellBroadcastIntents;
import android.telephony.CellBroadcastService;
import android.telephony.ICellBroadcastService;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyDisplayInfo;
import android.telephony.TelephonyManager;
import android.telephony.euicc.EuiccManager;
import android.text.TextUtils;
import android.util.Log;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TtsSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.android.internal.telephony.PhoneConstants;

import com.android.settings.R;
import com.android.settings.network.SubscriptionUtil;
import com.android.settings.network.telephony.DomesticRoamUtils;
import com.android.settingslib.mobile.MobileMappings;
import com.android.settingslib.mobile.MobileMappings.Config;
import com.android.settingslib.SignalIcon.MobileIconGroup;
import com.android.settingslib.Utils;
import com.android.settingslib.core.lifecycle.Lifecycle;

import static com.android.settings.network.MobileIconGroupExtKt.getSummaryForSub;
import static com.android.settingslib.mobile.MobileMappings.getIconKey;
import static com.android.settingslib.mobile.MobileMappings.mapIconSets;

import kotlin.Unit;

import java.util.List;

/**
 * Controller for Sim Status information within the About Phone Settings page.
 */
public class SimStatusDialogController implements DefaultLifecycleObserver {

    private final static String TAG = "SimStatusDialogCtrl";

    @VisibleForTesting
    final static int NETWORK_PROVIDER_VALUE_ID = R.id.operator_name_value;
    @VisibleForTesting
    final static int PHONE_NUMBER_VALUE_ID = R.id.number_value;
    @VisibleForTesting
    final static int CELLULAR_NETWORK_STATE = R.id.data_state_value;
    @VisibleForTesting
    final static int OPERATOR_INFO_LABEL_ID = R.id.latest_area_info_label;
    @VisibleForTesting
    final static int OPERATOR_INFO_VALUE_ID = R.id.latest_area_info_value;
    @VisibleForTesting
    final static int SERVICE_STATE_VALUE_ID = R.id.service_state_value;
    @VisibleForTesting
    final static int SIGNAL_STRENGTH_LABEL_ID = R.id.signal_strength_label;
    @VisibleForTesting
    final static int SIGNAL_STRENGTH_VALUE_ID = R.id.signal_strength_value;
    @VisibleForTesting
    final static int CELL_VOICE_NETWORK_TYPE_VALUE_ID = R.id.voice_network_type_value;
    @VisibleForTesting
    final static int CELL_DATA_NETWORK_TYPE_VALUE_ID = R.id.data_network_type_value;
    @VisibleForTesting
    final static int ROAMING_INFO_VALUE_ID = R.id.roaming_state_value;
    @VisibleForTesting
    final static int ICCID_INFO_LABEL_ID = R.id.icc_id_label;
    @VisibleForTesting
    final static int ICCID_INFO_VALUE_ID = R.id.icc_id_value;
    @VisibleForTesting
    final static int IMS_REGISTRATION_STATE_LABEL_ID = R.id.ims_reg_state_label;
    @VisibleForTesting
    final static int IMS_REGISTRATION_STATE_VALUE_ID = R.id.ims_reg_state_value;
    @VisibleForTesting
    static final int ID_PRL_VERSION_VALUE = R.id.prl_version_value;
    private static final int ID_MIN_NUMBER_LABEL = R.id.min_number_label;
    @VisibleForTesting
    static final int ID_MIN_NUMBER_VALUE = R.id.min_number_value;
    @VisibleForTesting
    static final int ID_MEID_NUMBER_VALUE = R.id.meid_number_value;
    @VisibleForTesting
    static final int ID_IMEI_VALUE = R.id.imei_value;
    @VisibleForTesting
    static final int ID_IMEI_SV_VALUE = R.id.imei_sv_value;
    @VisibleForTesting
    static final int ID_CDMA_SETTINGS = R.id.cdma_settings;
    @VisibleForTesting
    static final int ID_GSM_SETTINGS = R.id.gsm_settings;

    @VisibleForTesting
    static final int MAX_PHONE_COUNT_SINGLE_SIM = 1;

    private final OnSubscriptionsChangedListener mOnSubscriptionsChangedListener =
            new OnSubscriptionsChangedListener() {
                @Override
                public void onSubscriptionsChanged() {
                    mSubscriptionInfo = getPhoneSubscriptionInfo(mSlotIndex);
                    updateSubscriptionStatus();
                }
            };

    private SubscriptionInfo mSubscriptionInfo;
    private TelephonyDisplayInfo mTelephonyDisplayInfo;

    private final int mSlotIndex;
    private TelephonyManager mTelephonyManager;

    private final SimStatusDialogFragment mDialog;
    private final SubscriptionManager mSubscriptionManager;
    private final CarrierConfigManager mCarrierConfigManager;
    private final EuiccManager mEuiccManager;
    private final int mSlotId;
    private final Resources mRes;
    private final Context mContext;

    private boolean mShowLatestAreaInfo;
    private boolean mIsRegisteredListener = false;

    private final BroadcastReceiver mAreaInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (CellBroadcastIntents.ACTION_AREA_INFO_UPDATED.equals(intent.getAction())
                    && intent.getIntExtra(SubscriptionManager.EXTRA_SLOT_INDEX, 0)
                    == mSlotIndex) {
                updateAreaInfoText();
            }
        }
    };

    @VisibleForTesting
    protected SimStatusDialogTelephonyCallback mTelephonyCallback;

    private CellBroadcastServiceConnection mCellBroadcastServiceConnection;

    private class CellBroadcastServiceConnection implements ServiceConnection {
        private IBinder mService;

        @Nullable
        public IBinder getService() {
            return mService;
        }

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "connected to CellBroadcastService");
            this.mService = service;
            updateAreaInfoText();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            this.mService = null;
            Log.d(TAG, "mICellBroadcastService has disconnected unexpectedly");
        }

        @Override
        public void onBindingDied(ComponentName name) {
            this.mService = null;
            Log.d(TAG, "Binding died");
        }

        @Override
        public void onNullBinding(ComponentName name) {
            this.mService = null;
            Log.d(TAG, "Null binding");
        }
    }

    public SimStatusDialogController(@NonNull SimStatusDialogFragment dialog, Lifecycle lifecycle,
            int slotId) {
        mDialog = dialog;
        mSlotId = slotId;
        mContext = dialog.getContext();
        mSlotIndex = slotId;
        mSubscriptionInfo = getPhoneSubscriptionInfo(slotId);

        mTelephonyManager = mContext.getSystemService(TelephonyManager.class);
        mCarrierConfigManager = mContext.getSystemService(CarrierConfigManager.class);
        mEuiccManager = mContext.getSystemService(EuiccManager.class);
        mSubscriptionManager = mContext.getSystemService(SubscriptionManager.class);

        mRes = mContext.getResources();

        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    @VisibleForTesting
    public TelephonyManager getTelephonyManager() {
        return mTelephonyManager;
    }

    public void initialize() {
        if (mSubscriptionInfo == null) {
            return;
        }
        mTelephonyManager =
            getTelephonyManager().createForSubscriptionId(mSubscriptionInfo.getSubscriptionId());
        mTelephonyCallback = new SimStatusDialogTelephonyCallback();
        updateLatestAreaInfo();
        updateSubscriptionStatus();
    }

    private void updateSubscriptionStatus() {
        updateNetworkProvider();

        // getServiceState() may return null when the subscription is inactive
        // or when there was an error communicating with the phone process.
        final ServiceState serviceState = getTelephonyManager().getServiceState();

        updatePhoneNumber();
        updateServiceState(serviceState);
        updateNetworkType();
        updateRoamingStatus(serviceState);
        updateIccidNumber();
        if (mTelephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
            updateDialogForCdmaPhone();
        } else {
            updateDialogForGsmPhone();
        }
    }

    /**
     * Deinitialization works
     */
    public void deinitialize() {
        if (mShowLatestAreaInfo) {
            if (mCellBroadcastServiceConnection != null
                    && mCellBroadcastServiceConnection.getService() != null) {
                mContext.unbindService(mCellBroadcastServiceConnection);
            }
            mCellBroadcastServiceConnection = null;
        }
    }

    /**
     * OnResume lifecycle event, resume listening for phone state or subscription changes.
     */
    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        if (mSubscriptionInfo == null) {
            return;
        }
        mTelephonyManager = getTelephonyManager().createForSubscriptionId(
                mSubscriptionInfo.getSubscriptionId());
        getTelephonyManager()
                .registerTelephonyCallback(mContext.getMainExecutor(), mTelephonyCallback);
        mSubscriptionManager.addOnSubscriptionsChangedListener(
                mContext.getMainExecutor(), mOnSubscriptionsChangedListener);
        collectSimStatusDialogInfo(owner);

        if (mShowLatestAreaInfo) {
            updateAreaInfoText();
            mContext.registerReceiver(mAreaInfoReceiver,
                    new IntentFilter(CellBroadcastIntents.ACTION_AREA_INFO_UPDATED),
                    Context.RECEIVER_EXPORTED/*UNAUDITED*/);
        }

        mIsRegisteredListener = true;
    }

    /**
     * onPause lifecycle event, no longer listen for phone state or subscription changes.
     */
    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        if (mSubscriptionInfo == null) {
            if (mIsRegisteredListener) {
                mSubscriptionManager.removeOnSubscriptionsChangedListener(
                        mOnSubscriptionsChangedListener);
                getTelephonyManager().unregisterTelephonyCallback(mTelephonyCallback);
                if (mShowLatestAreaInfo) {
                    mContext.unregisterReceiver(mAreaInfoReceiver);
                }
                mIsRegisteredListener = false;
            }
            return;
        }

        mSubscriptionManager.removeOnSubscriptionsChangedListener(mOnSubscriptionsChangedListener);
        getTelephonyManager().unregisterTelephonyCallback(mTelephonyCallback);

        if (mShowLatestAreaInfo) {
            mContext.unregisterReceiver(mAreaInfoReceiver);
        }
    }

    private void updateNetworkProvider() {
        final CharSequence carrierName =
                mSubscriptionInfo != null ? mSubscriptionInfo.getCarrierName() : null;
        if (DomesticRoamUtils.isFeatureEnabled(mContext)) {
            if (mSubscriptionInfo != null) {
                String operatorName = DomesticRoamUtils.getRegisteredOperatorName(
                        mContext, mSubscriptionInfo.getSubscriptionId());
                if (DomesticRoamUtils.EMPTY_OPERATOR_NAME != operatorName) {
                    mDialog.setText(NETWORK_PROVIDER_VALUE_ID, operatorName);
                    return;
                }
            }
        }
        mDialog.setText(NETWORK_PROVIDER_VALUE_ID, carrierName);
    }

    @VisibleForTesting
    public void updatePhoneNumber() {
        // If formattedNumber is null or empty, it'll display as "Unknown".
        mDialog.setText(PHONE_NUMBER_VALUE_ID,
                SubscriptionUtil.getBidiFormattedPhoneNumber(mContext, mSubscriptionInfo));
    }

    private void updateDataState(int state) {
        String networkStateValue;

        switch (state) {
            case TelephonyManager.DATA_CONNECTED:
                networkStateValue = mRes.getString(R.string.radioInfo_data_connected);
                break;
            case TelephonyManager.DATA_SUSPENDED:
                networkStateValue = mRes.getString(R.string.radioInfo_data_suspended);
                break;
            case TelephonyManager.DATA_CONNECTING:
                networkStateValue = mRes.getString(R.string.radioInfo_data_connecting);
                break;
            case TelephonyManager.DATA_DISCONNECTED:
                networkStateValue = mRes.getString(R.string.radioInfo_data_disconnected);
                break;
            default:
                networkStateValue = mRes.getString(R.string.radioInfo_unknown);
                break;
        }

        mDialog.setText(CELLULAR_NETWORK_STATE, networkStateValue);
    }

    /**
     * Update area info text retrieved from
     * {@link CellBroadcastService#getCellBroadcastAreaInfo(int)}
     */
    private void updateAreaInfoText() {
        if (!mShowLatestAreaInfo || mCellBroadcastServiceConnection == null) return;
        ICellBroadcastService cellBroadcastService =
                ICellBroadcastService.Stub.asInterface(
                        mCellBroadcastServiceConnection.getService());
        if (cellBroadcastService == null) return;
        try {
            mDialog.setText(OPERATOR_INFO_VALUE_ID,
                    cellBroadcastService.getCellBroadcastAreaInfo(mSlotIndex));

        } catch (RemoteException e) {
            Log.d(TAG, "Can't get area info. e=" + e);
        }
    }

    /**
     * Bind cell broadcast service.
     */
    private void bindCellBroadcastService() {
        mCellBroadcastServiceConnection = new CellBroadcastServiceConnection();
        Intent intent = new Intent(CellBroadcastService.CELL_BROADCAST_SERVICE_INTERFACE);
        String cbsPackage = getCellBroadcastServicePackage();
        if (TextUtils.isEmpty(cbsPackage)) return;
        intent.setPackage(cbsPackage);
        if (mCellBroadcastServiceConnection != null
                && mCellBroadcastServiceConnection.getService() == null) {
            if (!mContext.bindService(intent, mCellBroadcastServiceConnection,
                    Context.BIND_AUTO_CREATE)) {
                Log.e(TAG, "Unable to bind to service");
            }
        } else {
            Log.d(TAG, "skipping bindService because connection already exists");
        }
    }

    /** Returns the package name of the cell broadcast service, or null if there is none. */
    private String getCellBroadcastServicePackage() {
        PackageManager packageManager = mContext.getPackageManager();
        List<ResolveInfo> cbsPackages = packageManager.queryIntentServices(
                new Intent(CellBroadcastService.CELL_BROADCAST_SERVICE_INTERFACE),
                PackageManager.MATCH_SYSTEM_ONLY);
        if (cbsPackages.size() != 1) {
            Log.e(TAG, "getCellBroadcastServicePackageName: found " + cbsPackages.size()
                    + " CBS packages");
        }
        for (ResolveInfo info : cbsPackages) {
            if (info.serviceInfo == null) continue;
            String packageName = info.serviceInfo.packageName;
            if (!TextUtils.isEmpty(packageName)) {
                if (packageManager.checkPermission(
                        android.Manifest.permission.READ_PRIVILEGED_PHONE_STATE,
                        packageName) == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "getCellBroadcastServicePackageName: " + packageName);
                    return packageName;
                } else {
                    Log.e(TAG, "getCellBroadcastServicePackageName: " + packageName
                            + " does not have READ_PRIVILEGED_PHONE_STATE permission");
                }
            } else {
                Log.e(TAG, "getCellBroadcastServicePackageName: found a CBS package but "
                        + "packageName is null/empty");
            }
        }
        Log.e(TAG, "getCellBroadcastServicePackageName: package name not found");
        return null;
    }

    private void updateLatestAreaInfo() {
        mShowLatestAreaInfo = Resources.getSystem().getBoolean(
                com.android.internal.R.bool.config_showAreaUpdateInfoSettings)
                && getTelephonyManager().getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA;

        if (mShowLatestAreaInfo) {
            // Bind cell broadcast service to get the area info. The info will be updated once
            // the service is connected.
            bindCellBroadcastService();
        } else {
            mDialog.removeSettingFromScreen(OPERATOR_INFO_LABEL_ID);
            mDialog.removeSettingFromScreen(OPERATOR_INFO_VALUE_ID);
        }
    }

    private void updateServiceState(ServiceState serviceState) {
        final int state = Utils.getCombinedServiceState(serviceState);

        String serviceStateValue;

        switch (state) {
            case ServiceState.STATE_IN_SERVICE:
                serviceStateValue = mRes.getString(R.string.radioInfo_service_in);
                break;
            case ServiceState.STATE_OUT_OF_SERVICE:
            case ServiceState.STATE_EMERGENCY_ONLY:
                // Set summary string of service state to radioInfo_service_out when
                // service state is both STATE_OUT_OF_SERVICE & STATE_EMERGENCY_ONLY
                serviceStateValue = mRes.getString(R.string.radioInfo_service_out);
                break;
            case ServiceState.STATE_POWER_OFF:
                serviceStateValue = mRes.getString(R.string.radioInfo_service_off);
                break;
            default:
                serviceStateValue = mRes.getString(R.string.radioInfo_unknown);
                break;
        }

        mDialog.setText(SERVICE_STATE_VALUE_ID, serviceStateValue);
    }

    private void updateSignalStrength(@Nullable String signalStrength) {
        boolean isVisible = signalStrength != null;
        mDialog.setSettingVisibility(SIGNAL_STRENGTH_LABEL_ID, isVisible);
        mDialog.setSettingVisibility(SIGNAL_STRENGTH_VALUE_ID, isVisible);
        mDialog.setText(SIGNAL_STRENGTH_VALUE_ID, signalStrength);
    }

    private void updateNetworkType() {
        // TODO: all of this should be based on TelephonyDisplayInfo instead of just the 5G logic
        if (mSubscriptionInfo == null) {
            final String unknownNetworkType =
                    getNetworkTypeName(TelephonyManager.NETWORK_TYPE_UNKNOWN);
            mDialog.setText(CELL_VOICE_NETWORK_TYPE_VALUE_ID, unknownNetworkType);
            mDialog.setText(CELL_DATA_NETWORK_TYPE_VALUE_ID, unknownNetworkType);
            return;
        }

        // Whether EDGE, UMTS, etc...
        String dataNetworkTypeName = null;
        String voiceNetworkTypeName = null;
        final int subId = mSubscriptionInfo.getSubscriptionId();
        final int actualDataNetworkType = getTelephonyManager().getDataNetworkType();
        final int actualVoiceNetworkType = getTelephonyManager().getVoiceNetworkType();
        final int overrideNetworkType = mTelephonyDisplayInfo == null
                ? TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NONE
                : mTelephonyDisplayInfo.getOverrideNetworkType();

        if (TelephonyManager.NETWORK_TYPE_UNKNOWN != actualDataNetworkType) {
            dataNetworkTypeName = getNetworkTypeName(actualDataNetworkType);
        }
        if (TelephonyManager.NETWORK_TYPE_UNKNOWN != actualVoiceNetworkType) {
            voiceNetworkTypeName = getNetworkTypeName(actualVoiceNetworkType);
        }

        final boolean isOverrideNwTypeNrAdvancedOrNsa =
                overrideNetworkType == TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED
                        || overrideNetworkType == TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA;
        if (actualDataNetworkType == TelephonyManager.NETWORK_TYPE_LTE
                && isOverrideNwTypeNrAdvancedOrNsa) {
            dataNetworkTypeName = "NR NSA";
        }

        boolean isLteVoice = (TelephonyManager.NETWORK_TYPE_LTE == actualVoiceNetworkType);
        boolean isLteData = (TelephonyManager.NETWORK_TYPE_LTE == actualDataNetworkType);
        if (isLteVoice || isLteData) {
            Config mappingConfig = getMappingConfig(mContext);
            if (mTelephonyDisplayInfo == null) {
                boolean isUsingCA = mTelephonyManager.getServiceState().isUsingCarrierAggregation();
                mTelephonyDisplayInfo = new TelephonyDisplayInfo(TelephonyManager.NETWORK_TYPE_LTE,
                        isUsingCA ? TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_CA
                                : TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NONE);
            }
            String mappedTypeName = getMappingNetworkType(mContext, mappingConfig,
                    mTelephonyDisplayInfo, subId);
            if (!mappedTypeName.isEmpty()) {
                voiceNetworkTypeName = isLteVoice ? mappedTypeName : voiceNetworkTypeName;
                dataNetworkTypeName = isLteData ? mappedTypeName : dataNetworkTypeName;
            }
        }

        mDialog.setText(CELL_VOICE_NETWORK_TYPE_VALUE_ID, voiceNetworkTypeName);
        mDialog.setText(CELL_DATA_NETWORK_TYPE_VALUE_ID, dataNetworkTypeName);
    }

    /**
     * Gets config for carrier customization.
     */
    private Config getMappingConfig(Context context) {
        return MobileMappings.Config.readConfig(context);
    }

    /**
     * Gets current network type of Carrier Cellular.
     */
    private String getMappingNetworkType(Context context, Config config,
                                 TelephonyDisplayInfo telephonyDisplayInfo, int subId) {
        String iconKey = getIconKey(telephonyDisplayInfo);
        MobileIconGroup iconGroup = mapIconSets(config).get(iconKey);
        if (iconGroup == null) {
            Log.d(TAG, "Can not get the network's icon and description.");
            return null;
        }
        return getSummaryForSub(iconGroup, context, subId);
    }

    private void updateRoamingStatus(ServiceState serviceState) {
        // If the serviceState is null, we assume that roaming is disabled.
        if (serviceState == null) {
            mDialog.setText(ROAMING_INFO_VALUE_ID, mRes.getString(R.string.radioInfo_unknown));
        } else if (serviceState.getRoaming()) {
            mDialog.setText(ROAMING_INFO_VALUE_ID, mRes.getString(R.string.radioInfo_roaming_in));
        } else {
            mDialog.setText(ROAMING_INFO_VALUE_ID, mRes.getString(R.string.radioInfo_roaming_not));
        }
    }

    private void updateIccidNumber() {
        // do not show iccid by default
        boolean showIccId = false;
        if (mSubscriptionInfo != null) {
            final int subscriptionId = mSubscriptionInfo.getSubscriptionId();
            final PersistableBundle carrierConfig =
                    mCarrierConfigManager.getConfigForSubId(subscriptionId);
            if (carrierConfig != null) {
                showIccId = carrierConfig.getBoolean(
                        CarrierConfigManager.KEY_SHOW_ICCID_IN_SIM_STATUS_BOOL);
            }
        }
        if (!showIccId) {
            mDialog.removeSettingFromScreen(ICCID_INFO_LABEL_ID);
            mDialog.removeSettingFromScreen(ICCID_INFO_VALUE_ID);
        } else {
            mDialog.setText(ICCID_INFO_VALUE_ID, getTelephonyManager().getSimSerialNumber());
        }
    }

    private void updateImsRegistrationState(@Nullable Boolean imsRegistered) {
        boolean isVisible = imsRegistered != null;
        mDialog.setSettingVisibility(IMS_REGISTRATION_STATE_LABEL_ID, isVisible);
        mDialog.setSettingVisibility(IMS_REGISTRATION_STATE_VALUE_ID, isVisible);
        int stringId = Boolean.TRUE.equals(imsRegistered)
                ? com.android.settingslib.R.string.ims_reg_status_registered
                : com.android.settingslib.R.string.ims_reg_status_not_registered;
        mDialog.setText(IMS_REGISTRATION_STATE_VALUE_ID, mRes.getString(stringId));
    }

    private void collectSimStatusDialogInfo(@NonNull LifecycleOwner owner) {
        new SimStatusDialogRepository(mContext).collectSimStatusDialogInfo(
                owner, mSlotIndex, (simStatusDialogInfo) -> {
                    updateSignalStrength(simStatusDialogInfo.getSignalStrength());
                    updateImsRegistrationState(simStatusDialogInfo.getImsRegistered());
                    return Unit.INSTANCE;
                }
        );
    }

    private SubscriptionInfo getPhoneSubscriptionInfo(int slotId) {
        return SubscriptionManager.from(mContext).getActiveSubscriptionInfoForSimSlotIndex(slotId);
    }

    private void updateDialogForCdmaPhone() {
        final Resources res = mDialog.getContext().getResources();
        mDialog.setText(ID_MEID_NUMBER_VALUE, getMeid());
        mDialog.setText(ID_MIN_NUMBER_VALUE,
                mSubscriptionInfo != null ? mTelephonyManager.getCdmaMin(
                        mSubscriptionInfo.getSubscriptionId()) : "");

        if (res.getBoolean(R.bool.config_msid_enable)) {
            mDialog.setText(ID_MIN_NUMBER_LABEL,
                    res.getString(R.string.status_msid_number));
        }

        mDialog.setText(ID_PRL_VERSION_VALUE, getCdmaPrlVersion());

        if (mSubscriptionInfo != null && isCdmaLteEnabled()) {
            // Show IMEI for LTE device
            mDialog.setText(ID_IMEI_VALUE,
                    getTextAsDigits(mTelephonyManager.getImei(mSlotId)));
            mDialog.setText(ID_IMEI_SV_VALUE,
                    getTextAsDigits(mTelephonyManager.getDeviceSoftwareVersion(mSlotId)));
        } else {
            // device is not GSM/UMTS, do not display GSM/UMTS features
            mDialog.removeSettingFromScreen(ID_GSM_SETTINGS);
        }
    }

    private void updateDialogForGsmPhone() {
        mDialog.setText(ID_IMEI_VALUE, getTextAsDigits(mTelephonyManager.getImei(mSlotId)));
        mDialog.setText(ID_IMEI_SV_VALUE,
                getTextAsDigits(mTelephonyManager.getDeviceSoftwareVersion(mSlotId)));
        // device is not CDMA, do not display CDMA features
        mDialog.removeSettingFromScreen(ID_CDMA_SETTINGS);
    }

    @VisibleForTesting
    class SimStatusDialogTelephonyCallback extends TelephonyCallback implements
            TelephonyCallback.DataConnectionStateListener,
            TelephonyCallback.ServiceStateListener,
            TelephonyCallback.DisplayInfoListener {
        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
            updateDataState(state);
            updateNetworkType();
        }

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            updateNetworkProvider();
            updateServiceState(serviceState);
            updateRoamingStatus(serviceState);
        }

        @Override
        public void onDisplayInfoChanged(@NonNull TelephonyDisplayInfo displayInfo) {
            mTelephonyDisplayInfo = displayInfo;
            updateNetworkType();
        }
    }

    @VisibleForTesting
    static String getNetworkTypeName(@Annotation.NetworkType int type) {
        switch (type) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "CDMA - EvDo rev. 0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "CDMA - EvDo rev. A";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "CDMA - EvDo rev. B";
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "CDMA - 1xRTT";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "CDMA - eHRPD";
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "iDEN";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "HSPA+";
            case TelephonyManager.NETWORK_TYPE_GSM:
                return "GSM";
            case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                return "TD_SCDMA";
            case TelephonyManager.NETWORK_TYPE_IWLAN:
                return "IWLAN";
            case TelephonyManager.NETWORK_TYPE_NR:
                return "NR SA";
            default:
                return "UNKNOWN";
        }
    }

    @VisibleForTesting
    String getCdmaPrlVersion() {
        return mTelephonyManager.getCdmaPrlVersion();
    }

    @VisibleForTesting
    boolean isCdmaLteEnabled() {
        return mTelephonyManager.getLteOnCdmaMode(mSubscriptionInfo.getSubscriptionId())
                == PhoneConstants.LTE_ON_CDMA_TRUE;
    }

    @VisibleForTesting
    String getMeid() {
        return mTelephonyManager.getMeid(mSlotId);
    }

    private static CharSequence getTextAsDigits(CharSequence text) {
        if (TextUtils.isDigitsOnly(text)) {
            final Spannable spannable = new SpannableStringBuilder(text);
            final TtsSpan span = new TtsSpan.DigitsBuilder(text.toString()).build();
            spannable.setSpan(span, 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text = spannable;
        }
        return text;
    }
}
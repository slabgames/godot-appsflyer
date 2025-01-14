package ru.mobilap.appsflyer;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.os.Bundle;
import android.view.View;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

import org.godotengine.godot.plugin.UsedByGodot;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import com.appsflyer.AppsFlyerLib;
import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerInAppPurchaseValidatorListener;

import com.appsflyer.adrevenue.AppsFlyerAdRevenue;
import com.appsflyer.adrevenue.adnetworks.AppsFlyerAdNetworkEventType;
import com.appsflyer.adrevenue.adnetworks.generic.MediationNetwork;
import com.appsflyer.adrevenue.adnetworks.generic.Scheme;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;

import org.godotengine.godot.Dictionary;
import org.godotengine.godot.Godot;
import org.godotengine.godot.GodotLib;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;

public class AppsFlyer extends GodotPlugin {

    private final String TAG = AppsFlyer.class.getName();

    public AppsFlyer(Godot godot) 
    {
        super(godot);
    }

    @Override
    public String getPluginName() {
        return "AppsFlyer";
    }

//    @Override
//    public List<String> getPluginMethods() {
//        return Arrays.asList(
//                "init",
//                "track_event",
//                "track_revenue",
//                "appsflyer_id"
//        );
//    }

    /*
    @Override
    public Set<SignalInfo> getPluginSignals() {
        return Collections.singleton(loggedInSignal);
    }
    */

    @Override
    public View onMainCreate(Activity activity) {
        return null;
    }


    // Public methods
    @UsedByGodot
    public void init(final String key, final boolean ProductionMode)
    {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    AppsFlyerConversionListener conversionDataListener = new AppsFlyerConversionListener() {
                            /* Returns the attribution data. Note - the same conversion data is returned every time per install */
                            @Override
                            public void onConversionDataSuccess(Map<String, Object> conversionData) {
                                for (String attrName : conversionData.keySet()) {
                                    Log.d(TAG, "Attribute: " + attrName + " = " + conversionData.get(attrName));
                                }
                            }
                            @Override
                            public void onConversionDataFail(String errorMessage) {
                                Log.e(TAG, "Error getting conversion data: " + errorMessage);
                            }
                            /* Called only when a Deep Link is opened */
                            @Override
                            public void onAppOpenAttribution(Map<String, String> conversionData) {
                                for (String attrName : conversionData.keySet()) {
                                    Log.d(TAG, "Attribute: " + attrName + " = " + conversionData.get(attrName));
                                }
                            }
                            @Override
                            public void onAttributionFailure(String errorMessage) {
                                Log.e(TAG, "Error onAttributionFailure : " + errorMessage);
                            }
                        };
                    AppsFlyerLib.getInstance().init(key, conversionDataListener, getActivity().getApplicationContext());
                    AppsFlyerLib.getInstance().start(getActivity().getApplication());
                    if(!ProductionMode) {
                        AppsFlyerLib.getInstance().setDebugLog(true);
                    }
                    AppsFlyerLib.getInstance().
                        registerValidatorListener(getActivity(),
                                                  new AppsFlyerInAppPurchaseValidatorListener() {
                                                      public void onValidateInApp() {
                                                          Log.d(TAG, "Purchase validated successfully");
                                                      }
                                                      public void onValidateInAppFailure(String error) {
                                                          Log.d(TAG, "onValidateInAppFailure called: " + error);
                                                      }
                                                  });


                    AppsFlyerAdRevenue.Builder afRevenueBuilder = new AppsFlyerAdRevenue.Builder(getActivity().getApplication());     
        
        			AppsFlyerAdRevenue.initialize(afRevenueBuilder.build());
                } catch (Exception e) {
                    Log.e(TAG, "Failed to initialize AppsFlyerSdk: " + e.getMessage()); 
                }
            }
        });
    }

    @UsedByGodot
    public void logAdRevenue(final String adUnit, final String pAdType)
    {
    	Map<String, String> customParams = new HashMap<>();
		customParams.put(Scheme.COUNTRY, "US");
		customParams.put(Scheme.AD_UNIT, adUnit);
        String adType =AppsFlyerAdNetworkEventType.BANNER.toString();

        if(pAdType=="banner")
        {
            adType = AppsFlyerAdNetworkEventType.BANNER.toString();
        }
        else if(pAdType=="interstitial"){
            adType = AppsFlyerAdNetworkEventType.INTERSTITIAL.toString();
        }
        else if(pAdType=="rewarded")
        {
            adType = AppsFlyerAdNetworkEventType.REWARDED.toString();
        }
		customParams.put(Scheme.AD_TYPE, adType);
		customParams.put(Scheme.PLACEMENT, "place");
		customParams.put(Scheme.ECPM_PAYLOAD, "encrypt");
		// customParams.put("foo", "test1");
    	AppsFlyerAdRevenue.logAdRevenue("applovinmax",
    		MediationNetwork.applovinmax,
    		Currency.getInstance(Locale.US),
    		0.00,customParams
    		);
    }

    @UsedByGodot
    public void track_event(final String event, final Dictionary params)
    {
        AppsFlyerLib.getInstance().logEvent(getActivity(), event, params);
    }

    @UsedByGodot
    public void set_uninstall_token(final String token)
    {
    }

    @UsedByGodot
    public void track_revenue(final String revenue, final String currency, final String signature, final String originalJson, final String public_key)
    {
        AppsFlyerLib.getInstance().validateAndLogInAppPurchase(getActivity(), public_key, signature, originalJson, revenue, currency, null);
    }

    @UsedByGodot
    public String appsflyer_id()
    {
        return AppsFlyerLib.getInstance().getAppsFlyerUID(getActivity());
    }

    // Internal methods

    @Override
    public void onMainActivityResult (int requestCode, int resultCode, Intent data)
    {
    }
}

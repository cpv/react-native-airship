/* Copyright Urban Airship and Contributors */

package com.urbanairship.reactnative.preferenceCenter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.preference.PreferenceManager;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.module.annotations.ReactModule;
import com.urbanairship.PendingResult;
import com.urbanairship.ResultCallback;
import com.urbanairship.preferencecenter.PreferenceCenter;
import com.urbanairship.UAirship;
import com.urbanairship.preferencecenter.data.CommonDisplay;
import com.urbanairship.preferencecenter.data.Item;
import com.urbanairship.preferencecenter.data.PreferenceCenterConfig;
import com.urbanairship.preferencecenter.data.Section;
import com.urbanairship.reactnative.Event;
import com.urbanairship.reactnative.EventEmitter;
import com.urbanairship.reactnative.preferenceCenter.events.OpenPreferenceCenterEvent;

import java.util.List;

@ReactModule(name = AirshipPreferenceCenterModule.NAME)
public class AirshipPreferenceCenterModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    public static final String NAME = "AirshipPreferenceCenterModule";

    public AirshipPreferenceCenterModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        PreferenceCenter.shared().setOpenListener(new PreferenceCenter.OnOpenListener() {
            @Override
            public boolean onOpenPreferenceCenter(String preferenceCenterID) {
                if (isCustomPreferenceCenterUIEnabled(preferenceCenterID)) {
                    Event event = new OpenPreferenceCenterEvent(preferenceCenterID);
                    EventEmitter.shared().sendEvent(event);
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    private boolean isCustomPreferenceCenterUIEnabled(String preferenceCenterID) {
        return PreferenceManager.getDefaultSharedPreferences(UAirship.getApplicationContext())
                .getBoolean(preferenceCenterID, false);
    }

    @ReactMethod
    public void open(String preferenceCenterID) {
        PreferenceCenter.shared().open(preferenceCenterID);
    }

    @ReactMethod
    public void getConfiguration(String preferenceCenterID, final Promise promise) {

        PreferenceCenter.shared().getConfig(preferenceCenterID).addResultCallback(new ResultCallback<PreferenceCenterConfig>() {
            @Override
            public void onResult(@Nullable PreferenceCenterConfig configPendingResult) {

                WritableMap configMap = new WritableNativeMap();
                if (configPendingResult != null) {
                    configMap.putString("id", configPendingResult.getId());

                    List<Section> sections = configPendingResult.getSections();
                    if (sections != null) {
                        WritableArray sectionArray = Arguments.createArray();
                        for (Section section : sections) {
                            WritableMap sectionMap = new WritableNativeMap();
                            sectionMap.putString("id", section.getId());

                            List<Item> items = section.getItems();
                            if (items != null) {
                                WritableArray itemArray = Arguments.createArray();
                                for (Item item : items) {
                                    WritableMap itemMap = new WritableNativeMap();
                                    itemMap.putString("id", item.getId());

                                    CommonDisplay commonDisplay = item.getDisplay();
                                    if (commonDisplay != null) {
                                        WritableMap commonDisplayMap = new WritableNativeMap();
                                        commonDisplayMap.putString("name", commonDisplay.getName());
                                        commonDisplayMap.putString("description", commonDisplay.getDescription());
                                        itemMap.putMap("CommonDisplay", (ReadableMap) commonDisplayMap);
                                    }
                                    itemArray.pushMap(itemMap);
                                }
                                sectionMap.putArray("item", itemArray);
                            }

                            CommonDisplay sectionCommonDisplay = section.getDisplay();
                            WritableMap sectionCommonDisplayMap = new WritableNativeMap();
                            if (sectionCommonDisplay != null) {
                                sectionCommonDisplayMap.putString("name", sectionCommonDisplay.getName());
                                sectionCommonDisplayMap.putString("description", sectionCommonDisplay.getDescription());
                                sectionMap.putMap("CommonDisplay", (ReadableMap) sectionCommonDisplayMap);
                            }

                            sectionArray.pushMap(sectionMap);
                        }
                        configMap.putArray("sections", sectionArray);
                    }

                    CommonDisplay configCommonDisplay = configPendingResult.getDisplay();
                    WritableMap configCommonDisplayMap = new WritableNativeMap();
                    if (configCommonDisplay != null) {
                        configCommonDisplayMap.putString("name", configCommonDisplay.getName());
                        configCommonDisplayMap.putString("description", configCommonDisplay.getDescription());
                        configMap.putMap("CommonDisplay", (ReadableMap) configCommonDisplayMap);
                    }
                }

                promise.resolve(configMap);
            }
        });
    }

    @ReactMethod
    public void getConfig(String preferenceCenterID, final Promise promise) {
        PendingResult<PreferenceCenterConfig> configPendingResult = PreferenceCenter.shared().getConfig(preferenceCenterID);
        if (configPendingResult.getResult() != null) {

            WritableMap configMap = new WritableNativeMap();


            promise.resolve(configMap);
        }
    }

    @ReactMethod
    public void setUseCustomPreferenceCenterUI(boolean useCustomUI, String preferenceID) {
      PreferenceManager.getDefaultSharedPreferences(UAirship.getApplicationContext())
      .edit()
      .putBoolean(preferenceID, useCustomUI)
      .apply();
    }

}

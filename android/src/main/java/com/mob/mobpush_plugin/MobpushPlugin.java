package com.mob.mobpush_plugin;

import android.app.Activity;
import android.content.Context;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.mob.MobSDK;
import com.mob.OperationCallback;
import com.mob.mobpush_plugin.req.SimulateRequest;
import com.mob.pushsdk.*;
import com.mob.tools.utils.Hashon;
import com.mob.tools.utils.ResHelper;

import java.lang.ref.WeakReference;
import java.util.*;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * MobpushPlugin
 */
public class MobpushPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
    private static Hashon hashon = new Hashon();
    private static MethodChannel channel;
    private static EventChannel eventChannel;

    private static ArrayList<Result> setAliasCallback = new ArrayList<>();
    private static ArrayList<Result> getAliasCallback = new ArrayList<>();
    private static ArrayList<Result> getTagsCallback = new ArrayList<>();
    private static ArrayList<Result> deleteAliasCallback = new ArrayList<>();
    private static ArrayList<Result> addTagsCallback = new ArrayList<>();
    private static ArrayList<Result> deleteTagsCallback = new ArrayList<>();
    private static ArrayList<Result> cleanTagsCallback = new ArrayList<>();
    private static WeakReference<Activity> activityWeakReference;

    @Override
    public void onMethodCall(MethodCall call, final MethodChannel.Result result) {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals("getSDKVersion")) {
            result.success(MobPush.SDK_VERSION_NAME);
        } else if (call.method.equals("getRegistrationId")) {
            MobPush.getRegistrationId(new MobPushCallback<String>() {
                @Override
                public void onCallback(String data) {
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("res", data);
                    result.success(map);
                }
            });
        } else if (call.method.equals("removePushReceiver")) {
            if (MobpushReceiverPlugin.getMobPushReceiver() != null) {
                MobPush.removePushReceiver(MobpushReceiverPlugin.getMobPushReceiver());
            }
        } else if (call.method.equals("setClickNotificationToLaunchMainActivity")) {
            boolean enable = call.argument("enable");
            MobPush.setClickNotificationToLaunchMainActivity(enable);
        } else if (call.method.equals("stopPush")) {
            MobPush.stopPush();
        } else if (call.method.equals("restartPush")) {
            MobPush.restartPush();
        } else if (call.method.equals("isPushStopped")) {
            result.success(MobPush.isPushStopped());
        } else if (call.method.equals("setAlias")) {
            String alias = call.argument("alias");
            setAliasCallback.add(result);
            MobPush.setAlias(alias);
        } else if (call.method.equals("getAlias")) {
            getAliasCallback.add(result);
            MobPush.getAlias();
        } else if (call.method.equals("deleteAlias")) {
            deleteAliasCallback.add(result);
            MobPush.deleteAlias();
        } else if (call.method.equals("addTags")) {
            ArrayList<String> tags = call.argument("tags");
            addTagsCallback.add(result);
            MobPush.addTags(tags.toArray(new String[tags.size()]));
        } else if (call.method.equals("getTags")) {
            getTagsCallback.add(result);
            MobPush.getTags();
        } else if (call.method.equals("deleteTags")) {
            ArrayList<String> tags = call.argument("tags");
            deleteTagsCallback.add(result);
            MobPush.deleteTags(tags.toArray(new String[tags.size()]));
        } else if (call.method.equals("cleanTags")) {
            cleanTagsCallback.add(result);
            MobPush.cleanTags();
        } else if (call.method.equals("setSilenceTime")) {
            int startHour = call.argument("startHour");
            int startMinute = call.argument("startMinute");
            int endHour = call.argument("endHour");
            int endMinute = call.argument("endMinute");
            MobPush.setSilenceTime(startHour, startMinute, endHour, endMinute);
        } else if (call.method.equals("setTailorNotification")) {

        } else if (call.method.equals("removeLocalNotification")) {
            int notificationId = call.argument("notificationId");
            result.success(MobPush.removeLocalNotification(notificationId));
        } else if (call.method.equals("addLocalNotification")) {
            String json = call.argument("localNotification");
            MobPushLocalNotification notification = hashon.fromJson(json, MobPushLocalNotification.class);
            result.success(MobPush.addLocalNotification(notification));
        } else if (call.method.equals("clearLocalNotifications")) {
            result.success(MobPush.clearLocalNotifications());
        } else if (call.method.equals("setNotifyIcon")) {
            String iconRes = call.argument("iconRes");
            int iconResId = ResHelper.getBitmapRes(MobSDK.getContext(), iconRes);
            if (iconResId > 0) {
                MobPush.setNotifyIcon(iconResId);
            }
        } else if (call.method.equals("setAppForegroundHiddenNotification")) {
            boolean hidden = call.argument("hidden");
            MobPush.setAppForegroundHiddenNotification(hidden);
        } else if (call.method.equals("setShowBadge")) {
            boolean show = call.argument("show");
            MobPush.setShowBadge(show);
        } else if (call.method.equals("bindPhoneNum")) {
            String phoneNum = call.argument("phoneNum");
            MobPush.bindPhoneNum(phoneNum, new MobPushCallback<Boolean>() {
                @Override
                public void onCallback(Boolean data) {
                    if (data != null) {
                        HashMap<String, Object> map = new HashMap<String, Object>();
                        map.put("res", data.booleanValue() ? "success" : "failed");
                        map.put("error", "");
                        result.success(map);
                    }
                }
            });
        } else if (call.method.equals("send")) {
            int type = call.argument("type");
            int space = call.argument("space");
            String content = call.argument("content");
            String extras = call.argument("extrasMap");
            SimulateRequest.sendPush(type, content, space, extras, new MobPushCallback<Boolean>() {
                @Override
                public void onCallback(Boolean aBoolean) {
                    if (aBoolean != null) {
                        HashMap<String, Object> map = new HashMap<String, Object>();
                        map.put("res", aBoolean.booleanValue() ? "success" : "failed");
                        map.put("error", "");
                        result.success(map);
                    }
                }
            });
        } else if (call.method.equals("updatePrivacyPermissionStatus")) {
            boolean status = call.argument("status");
            MobSDK.submitPolicyGrantResult(status, new OperationCallback<Void>() {
                @Override
                public void onComplete(Void aVoid) {
                    result.success(true);
                    System.out.println("updatePrivacyPermissionStatus onComplete");
                }

                @Override
                public void onFailure(Throwable throwable) {
                    result.error(throwable.toString(),null,null);
                    System.out.println("updatePrivacyPermissionStatus onFailure:" + throwable.getMessage());
                }
            });
        } else if (call.method.equals("getIntentData")){
            if (activityWeakReference.get() != null) {
                Intent intent = activityWeakReference.get().getIntent();
                if (intent.getExtras() != null && !intent.getExtras().isEmpty()) {
                    MobPush.notificationClickAck(intent);
                    JSONArray jsonArray = MobPushUtils.parseMainPluginPushIntent(intent);
                    List<Map<String,Object>> listMap = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            listMap.add(hashon.fromJson(jsonArray.get(i).toString()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    result.success(listMap);
                } else {
                    result.success(null);
                }
            } else {
                result.success(null);
            }
        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onAttachedToEngine(@NonNull @NotNull FlutterPluginBinding binding) {
        channel = new MethodChannel(binding.getBinaryMessenger(), "mob.com/mobpush_plugin");
        channel.setMethodCallHandler(new MobpushPlugin());

        eventChannel = new EventChannel(binding.getBinaryMessenger(), "mobpush_receiver");
        eventChannel.setStreamHandler(new MobpushReceiverPlugin());
//        createMobPushReceiver();
//        MobPush.addPushReceiver(mobPushReceiver);
    }

    @Override
    public void onDetachedFromEngine(@NonNull @NotNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    @Override
    public void onAttachedToActivity(@NonNull @NotNull ActivityPluginBinding binding) {
        activityWeakReference = new WeakReference<>(binding.getActivity());
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        activityWeakReference = null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull @NotNull ActivityPluginBinding binding) {
        activityWeakReference = new WeakReference<>(binding.getActivity());
    }

    @Override
    public void onDetachedFromActivity() {
        activityWeakReference = null;
    }
}

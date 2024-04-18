package com.example.util;

import static com.example.videostreamingapp.MyApplication.isInBackground;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.onesignal.OSNotificationReceivedEvent;
import com.onesignal.OneSignal;
import com.example.videostreamingapp.LogoutRemoteActivity;
import com.example.videostreamingapp.MyApplication;
import com.example.videostreamingapp.R;

import org.json.JSONException;
import org.json.JSONObject;

public class NotificationReceivedHandler implements OneSignal.OSRemoteNotificationReceivedHandler {

    @Override
    public void remoteNotificationReceived(Context context, OSNotificationReceivedEvent osNotificationReceivedEvent) {
        JSONObject data = osNotificationReceivedEvent.getNotification().getAdditionalData();
        if (data != null) {
            try {
                boolean isLogout = data.getString("logout_remote").equals("1");
                if (isLogout) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, context.getString(R.string.remote_logout), Toast.LENGTH_SHORT).show();
                            OneSignal.clearOneSignalNotifications();
                            MyApplication.getInstance().saveIsLogin(false);
                            MyApplication.getInstance().saveDeviceLimit(false);
                            OneSignal.sendTag("user_session", "");
                            if (isInBackground) {
                                Log.e("event", "yes");
                                Events.RemoteLogout fullScreen = new Events.RemoteLogout();
                                fullScreen.setLogoutRemote(true);
                                GlobalBus.getBus().post(fullScreen);
                            } else {
                                Log.e("activity", "yes");
                                Intent intent = new Intent(context, LogoutRemoteActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }

                            osNotificationReceivedEvent.complete(null);
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.e("notification-one-data", "" + data);
    }
}
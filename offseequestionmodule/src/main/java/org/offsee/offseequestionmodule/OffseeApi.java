package org.offsee.offseequestionmodule;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.offsee.offseequestionmodule.model.GamePlayInfo;
import org.offsee.offseequestionmodule.utils.App;
import org.offsee.offseequestionmodule.utils.FontManager;
import org.offsee.offseequestionmodule.webservice.ApiManagerInvoke;
import org.offsee.offseequestionmodule.webservice.ConnectionObserver;
import org.offsee.offseequestionmodule.webservice.Core;
import org.offsee.offseequestionmodule.webservice.MessageContract;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import needle.Needle;
import signalgo.client.GoResponseHandler;
import signalgo.client.GoSocketListener;

/**
 * Created by hamed on 5/31/2017.
 */

public class OffseeApi implements ConnectionObserver, Serializable {
    private Activity activity;
    private String apiKey;
    private String passKey;
    private String userId;
    private static OnGameStatusListener onGameStatusListener;
    private String imei = null;

    private static OffseeApi offseeApi;
    private TelephonyManager telephonyManager;
    private boolean isLogin = false;
    private boolean isStarting = false;
    ProgressDialog progressDialog;

    private static ArrayList<Activity> activities;

    public static void init(Activity activity, String apiKey, String passKey, String userId, OnGameStatusListener onGameStatusListener) {
        offseeApi = new OffseeApi(activity, apiKey, passKey, userId);
        OffseeApi.onGameStatusListener = onGameStatusListener;
        Core.destroyAllObservers();
        Core.initObserver(offseeApi);
    }

    public static OffseeApi getInstance() {
        if (offseeApi == null) {
            throw new RuntimeException("you have to call OffseeApi.init first!");
        }
        return offseeApi;
    }

    private OffseeApi(Activity activity, String apiKey, String passKey, String userId) {
        this.apiKey = apiKey;
        this.passKey = passKey;
        this.userId = userId;
        this.activity = activity;
        telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
        FontManager.init(activity, R.string.font_vazir);
    }


    public void startGame(boolean showProgressDialog) {
        if (imei == null) {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, 87);
                    return;
                } else {
                    imei = telephonyManager.getDeviceId();
                }
            } else {
                imei = telephonyManager.getDeviceId();
            }
        }

        isStarting = true;
        if (!App.isMyServiceRunning(activity, Core.class))
            activity.startService(new Intent(activity, Core.class));


        if (showProgressDialog) {
            progressDialog = new ProgressDialog(activity);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("در حال اتصال به سرور!");
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "لغو", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    isStarting = false;
                    dialog.dismiss();
                    onGameStatusListener.onCancel();
                }
            });
            progressDialog.show();
            FontManager.instance().setTypefaceImmediate(progressDialog.getWindow().getDecorView());
        }
        onLogin(isLogin);
    }


    public void stopGame() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        isStarting = false;
        if (App.isMyServiceRunning(activity, Core.class)) {
            activity.stopService(new Intent(activity, Core.class));
        }

    }

    @Override
    public void onServerChange(GoSocketListener.SocketState lState, GoSocketListener.SocketState cState, final boolean isConnected) {
        Needle.onMainThread().execute(new Runnable() {
            @Override
            public void run() {
                onGameStatusListener.onConnect(true);
            }
        });
    }

    @Override
    public void onLogin(final boolean isLogin) {

        Log.e("OffseeApi", "onLogin : " + isLogin);
        this.isLogin = isLogin;
        if (activities != null) {
            for (int i = 0; i < activities.size(); i++) {
                Class cls = activities.get(i).getClass();
                if (cls.equals(QuestionSplashActivity.class) || cls.equals(QuestionActivity.class) || cls.equals(EndQuestionActivity.class)) {
                    return;
                }
            }
        }
        if (isStarting)
            Needle.onMainThread().execute(new Runnable() {
                @Override
                public void run() {
                    if (isLogin) {
                        onGameStatusListener.onPrepare(true, "");
                        Intent intent = new Intent(activity, QuestionSplashActivity.class);
                        activity.startActivity(intent);
                        if (progressDialog != null)
                            progressDialog.dismiss();
                        isStarting = false;
                    } else {
                        onGameStatusListener.onPrepare(false, "خطا در ارتباط با سرور");
                    }
                }
            });
    }

    public static OnGameStatusListener getOnGameStatusListener() {
        return onGameStatusListener;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getPassKey() {
        return passKey;
    }

    public String getUserId() {
        return userId;
    }

    public String getImei() {
        return imei;
    }


    public synchronized static void registerActivity(Activity activity) {
        if (activities == null) {
            activities = new ArrayList<>();
        }
        activities.add(activity);
    }

    public synchronized static void unregisterActivity(final Activity activity) {
        activities.remove(activity);
        if (activities.size() == 0) {
            final Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (activities.size() == 0) {
                        if (App.isMyServiceRunning(activity, Core.class)) {
                            activity.stopService(new Intent(activity, Core.class));
                        }
                        Log.e("core", "stopSevice");
                    }
                    this.cancel();
                    timer.cancel();
                }
            }, 10000, 10);
        }
    }


}

package org.offsee.offseequestionmodule.webservice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import org.joda.time.DateTime;
import org.offsee.offseequestionmodule.utils.App;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import signalgo.client.ClientDuplex;
import signalgo.client.Connector;
import signalgo.client.GoResponseHandler;
import signalgo.client.GoSocketListener;
import signalgo.client.annotations.GoServiceName;

/**
 * Created by white on 2016-08-17.
 */

public class Core extends Service implements NetworkObserver {

    private static Connector connector;
    private static GoSocketListener goSocketListener;
    private static DateTime serviceStartTime, lastSyncTime;
    private static boolean isConnected;
    private static GoSocketListener.SocketState lState, cState;
    private static ApiManagerInvoke apiManagerInvoke;
    private static ApiManagerEmit apiManagerEmit;
    private static ArrayList<ClientDuplex> clientDuplices;
    private static ArrayList<ConnectionObserver> connectionObservers;
    private static boolean shouldTryToReconnect = true;
    private static final String TAG = "Core";
    private boolean hasNet = false;
    private static boolean isCheckNet = false, trying = false;
    private Timer reconnectTimer;

    private static int pingPongPeriod = 2 * 10000;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder() {
            public Core getService() {
                return Core.this;
            }
        };
    }

    /**
     * this method just called first time we start service!
     * if service stopped and we start it again this method
     * calls again its first method that call after
     * {@link Service#startService(Intent)} command
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "create core");
        lState = null;
        cState = null;
        isConnected = false;
        // Fabric.with(this, new Crashlytics());
        firstInit();
        startSignalGo();
        reconnectTimer = new Timer();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("core", "onStartCommand");
        NetworkManager.init(this);
        return super.onStartCommand(intent, flags, startId);

    }

    /**
     * initial connector and start time and states for
     * first time in {@link Service#onCreate()} method
     */
    private void firstInit() {
        if (connector == null) {

            serviceStartTime = new DateTime();
            apiManagerEmit = new ApiManagerEmit();
            apiManagerInvoke = new ApiManagerInvoke();
            clientDuplices = new ArrayList<>();
            shouldTryToReconnect = false;
        }
    }

    /**
     * try to connect to server and register services
     */
    private void startSignalGo() {
        Log.e("Core", "startSignalGo");
        connector = new Connector();
        connector.setTimeout(Constants.SOCKET_TIMEOUT);
        lState = cState = GoSocketListener.SocketState.Disconnected;
        isConnected = false;
        connector.registerService(apiManagerInvoke);
        connector.initForCallback(apiManagerEmit);
        connector.onSocketChangeListener(socketChangeListener());
        connector.connectAsync(Constants.getServerUrl());
    }

    /**
     * return goSocketListener and observe Socket changes
     *
     * @return goSocketListener
     */
    private GoSocketListener socketChangeListener() {
        goSocketListener = new GoSocketListener() {
            @Override
            public void onSocketChange(SocketState socketState, SocketState socketState1) {

                if (socketState == SocketState.Disconnected && socketState1 == SocketState.Connected) {
                    Log.e(TAG, "onConnected!");
                    isConnected = true;
                    onConnected();
                } else if (socketState == SocketState.Connected && socketState1 == SocketState.Disconnected) {
                    Log.e(TAG, "onDisconnected");
                    isConnected = false;
                    onDisconected();
                    tryReconnect();
                }
                shouldTryToReconnect = true;
            }

            @Override
            public void socketExeption(Exception e) {

                e.printStackTrace();
            }
        };
        return goSocketListener;
    }


    private synchronized void tryReconnect() {
        if (!shouldTryToReconnect) {
            return;
        }
        if (!hasNet) {
            Log.e("Core", "tryReconnect !hasnet");
            return;
        }
        if (trying) {
            return;
        }
        reconnectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                trying = true;
                if (cState == GoSocketListener.SocketState.Connected || !hasNet) {
                    Log.e("Core", "tryReconnect connected or no net");
                    trying = false;
                    this.cancel();
                } else {
                    try {
                        connector.forceClose();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //connector = new Connector();
                    //connector.setTimeout(Constants.SOCKET_TIMEOUT);
                    startSignalGo();
                    Log.e("Core", "tryReconnect tryyinnnnng");
                }
            }
        }, 100, 20 * 1000);
    }


//    public static void forceReconnect() {
//        try {
//            connector.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * notify all clients after connected to service successfully
     */
    private static void notifyClientDuplex() {

        apiManagerInvoke.getConnector(connector);
        apiManagerEmit.getConnector(connector);

        for (ClientDuplex cd : clientDuplices) {
            cd.getConnector(connector);
        }
    }

    /**
     * called after connected to server
     */
    private void onConnected() {
        lState = cState;
        cState = GoSocketListener.SocketState.Connected;
//        Pair<String, String> p = App.getUserPass();
//        if (App.isConfirmUser()) {
//            Log.e(TAG, "want to get session");
//
//            ApiManagerInvoke.registerPassengerSession(p.first, p.second, new GoResponseHandler<MessageContract<PassengerInfo>>() {
//                @Override
//                public void onResponse(MessageContract messageContract) {
//                    if (messageContract != null && messageContract.isSuccess) {
//                        Log.e(TAG, "get session");
//                        notifyConnectionObservers();
//                    } else if (messageContract != null && (messageContract.errorCode == 34 || messageContract.errorCode == 35)) {
//                        App.sp.edit().clear().commit();
//                        notifyConnectionObservers();
//                    } else {
//                        Log.e(TAG, "get session try reconnect");
//                        checkNet();
//                    }
//                }
//            });
//        } else {
        notifyConnectionObservers();
        //}
        notifyClientDuplex();
    }

    /**
     * called after disconnected from server
     */
    private void onDisconected() {
        lState = cState;
        cState = GoSocketListener.SocketState.Disconnected;

    }

    private void notifyConnectionObservers() {
        if (connectionObservers == null)
            return;
        for (int i = 0; i < connectionObservers.size(); i++) {
            connectionObservers.get(i).onServerChange(lState, cState, isConnected);
        }
    }

    /**
     * register your service and methods for get permission for calling
     * server methods and receive server callback
     *
     * @param clientDuplex
     */
    public static void registerService(ClientDuplex clientDuplex) {

        GoServiceName goServiceName = clientDuplex.getClass().getAnnotation(GoServiceName.class);
        if (goServiceName.usage() == GoServiceName.GoUsageType.emit)
            connector.initForCallback(clientDuplex);
        else
            connector.registerService(clientDuplex);
        clientDuplices.add(clientDuplex);
    }

    /**
     * remove service from clients
     *
     * @param clientDuplex
     * @return true if remove successfully
     */
    public static boolean removeService(ClientDuplex clientDuplex) {
        return clientDuplices.remove(clientDuplex);
    }

    public static void initObserver(ConnectionObserver connectionObserver) {
        if (connectionObservers == null)
            connectionObservers = new ArrayList<>();
        if (!connectionObservers.contains(connectionObserver))
            connectionObservers.add(connectionObserver);
        connectionObserver.onServerChange(lState, cState, isConnected);
    }

    public static boolean destroyObserver(ConnectionObserver connectionObserver) {
        if (connectionObservers != null) {
            return connectionObservers.remove(connectionObserver);
        }
        return false;
    }

    @Override
    public void onChange(boolean isConnected) {
        hasNet = isConnected;
        Log.e(TAG, "network status changed! : " + isConnected);
        if (isConnected) {
            shouldTryToReconnect = true;
            tryReconnect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NetworkManager.destroy(this);
        try {
            reconnectTimer.cancel();
            connector.onSocketChangeListener(null);
            connector.close();
            connector = null;
            cState = null;
            lState = null;
            isConnected = false;

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, "destroy core");
    }

    public static void checkNet() {
        Log.e(TAG, "check net");
        if (connector == null) {
            return;
        }
        if (!isCheckNet) {
            isCheckNet = true;
            connector.pingPong();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    isCheckNet = false;
                    this.cancel();
                }
            }, 10000, 100);
        }
    }

    public static boolean setPingPongPeriod(int periodMillis) {
        if (connector != null) {
            if (periodMillis != pingPongPeriod) {
                pingPongPeriod = periodMillis;
                connector.setPingpongPeriod(periodMillis);
            }
            return true;
        }
        return false;
    }

}




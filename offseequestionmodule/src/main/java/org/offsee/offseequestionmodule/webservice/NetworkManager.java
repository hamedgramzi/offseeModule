package org.offsee.offseequestionmodule.webservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import org.offsee.offseequestionmodule.utils.App;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by whiteman on 3/21/2016.
 */
public class NetworkManager extends BroadcastReceiver {
    private static List<NetworkObserver> networkObservers;
    private static NetworkInfo activeNetInfo;
    private static NetworkInfo mobNetInfo;
    private static NetworkInfo activeNetwork;
    private static ConnectivityManager connectivityManager;

    public void onReceive( Context context, Intent intent ) {
        pushUpdateMessage(checkInstantly(context));
    }

    private static boolean checkInstantly(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetInfo = connectivityManager.getActiveNetworkInfo();
        mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        WifiManager m= (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiInfo i=m.getConnectionInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public static void init(NetworkObserver networkObserver){
        if(networkObservers==null){
            networkObservers=new ArrayList<>();
        }
        networkObservers.add(networkObserver);
        networkObserver.onChange(checkInstantly(App.getContext()));
    }

    public static void destroy(NetworkObserver networkObserver){
        if(networkObservers!=null){
            if(networkObservers.contains(networkObserver)){
                networkObservers.remove(networkObserver);
            }
        }
    }

    private void pushUpdateMessage(boolean state){
        if(networkObservers!=null){
            for (NetworkObserver n:networkObservers){
                if(n!=null)
                    n.onChange(state);
            }
        }
    }
}

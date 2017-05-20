package org.offsee.offseequestionmodule.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;

import com.example.offseequestionmodule.R;

import org.offsee.offseequestionmodule.webservice.Core;

import java.util.List;

import needle.Needle;

/**
 * Created by hamed on 5/17/2017.
 */

public class App {
    private static Context context;
    public static SharedPreferences sp;
    public static LocationManager locationManager;
    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        App.context = context;
        if (sp == null) {
            sp = context.getApplicationContext().getSharedPreferences("offseeGame", Context.MODE_PRIVATE);
        }
        if(locationManager  == null){
            locationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public static Location getLastKnownLocation() {
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    public static void serverError(final Context context) {
        Core.checkNet();
        Needle.onMainThread().execute(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, R.string.serverError2, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

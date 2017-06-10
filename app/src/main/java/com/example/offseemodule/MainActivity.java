package com.example.offseemodule;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import org.offsee.offseequestionmodule.OffseeApi;
import org.offsee.offseequestionmodule.OnGameStatusListener;
import org.offsee.offseequestionmodule.utils.PermissionUtils;
import org.offsee.offseequestionmodule.webservice.Core;

public class MainActivity extends AppCompatActivity implements OnGameStatusListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OffseeApi.init(this, "a9d05211-39b0-4cf5-8aee-fc989efdcfbb", "a9d05211-39b0-4cf5-8aee-fc989efcdfbb", "hamed test", this);
        OffseeApi.registerActivity(this);
    }


    public void onStartQuestionClick(View v) {
        OffseeApi api = OffseeApi.getInstance();
        api.startGame(true);
    }

    @Override
    public boolean onConnect(boolean isConnected) {
        Toast.makeText(MainActivity.this, "onConnect", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onPrepare(boolean success, String message) {
        Toast.makeText(MainActivity.this, "onPrepare", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart(boolean hasError, String message) {
        Toast.makeText(MainActivity.this, "onStart", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onComplete(boolean isWin, int score) {
        Toast.makeText(MainActivity.this, "onComplete", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onCancel() {
        Toast.makeText(MainActivity.this, "onCancel", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OffseeApi.unregisterActivity(this);
    }
}

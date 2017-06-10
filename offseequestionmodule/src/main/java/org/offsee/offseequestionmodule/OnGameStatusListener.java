package org.offsee.offseequestionmodule;

import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by hamed on 6/1/2017.
 */

public interface OnGameStatusListener extends Serializable{
    boolean onConnect(boolean isConnected);

    void onPrepare(boolean success, String message);

    void onStart(boolean hasError, String message);

    void onComplete(boolean isWin,int score);

    void onCancel();
}

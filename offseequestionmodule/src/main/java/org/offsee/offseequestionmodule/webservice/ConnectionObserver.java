package org.offsee.offseequestionmodule.webservice;


import signalgo.client.GoSocketListener;

/**
 * Created by white on 2016-08-18.
 */
public interface ConnectionObserver {

    void onServerChange(GoSocketListener.SocketState lState, GoSocketListener.SocketState cState, boolean isConnected);
}

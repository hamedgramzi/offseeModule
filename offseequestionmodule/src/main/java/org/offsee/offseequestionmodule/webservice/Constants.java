package org.offsee.offseequestionmodule.webservice;

/**
 * Created by mr.hamed on 03/09/2016.
 */
public final class Constants {
    //public final static String SERVER_URL="http://taxi.atitec.ir:4694/TransportServices/SignalGo";
    //public final static String SERVER_URL="http://192.168.1.96:4694/TransportServices/SignalGo";
    //private static String SERVER_URL="http://taxi.taxcity.ir:4690/TransportServices/SignalGo";
    private static String SERVER_URL = "http://offsee.org:4710/OffSeeServices/SignalGo";
    public final static String SERVICE_INVOKE_NAME = "TransportService";
    public final static String SERVICE_EMIT_NAME = "TransportServiceCallback";
    public final static int SOCKET_TIMEOUT = 20000;

    public static String getServerUrl() {
        return SERVER_URL;
    }

}
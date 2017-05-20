package org.offsee.offseequestionmodule.webservice;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import needle.Needle;
import signalgo.client.ClientDuplex;
import signalgo.client.Connector;
import signalgo.client.annotations.GoMethodName;
import signalgo.client.annotations.GoServiceName;

/**
 * Created by white on 2016-08-17.
 */
@GoServiceName(name = Constants.SERVICE_EMIT_NAME, type = GoServiceName.GoClientType.Android, usage = GoServiceName.GoUsageType.emit)
public class ApiManagerEmit implements ClientDuplex {

    private Connector connector = new Connector();

//    @GoMethodName(name = "OnPaymentDone", type = GoMethodName.MethodType.emit)
//    public void onPaymentDone(final Payment payment) {
//        Log.e("paymentDone", payment.toString());
//        App.updateCreditFromServer();
//    }



    @Override
    public void getConnector(Connector connector) {
        this.connector = connector;
    }
}

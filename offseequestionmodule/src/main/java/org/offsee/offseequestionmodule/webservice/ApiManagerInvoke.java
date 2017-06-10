package org.offsee.offseequestionmodule.webservice;


import android.location.Location;

import com.google.common.reflect.TypeToken;

import org.offsee.offseequestionmodule.model.GamePlayInfo;
import org.offsee.offseequestionmodule.model.MyPair;
import org.offsee.offseequestionmodule.model.Question;

import java.util.List;

import signalgo.client.ClientDuplex;
import signalgo.client.Connector;
import signalgo.client.GoResponseHandler;
import signalgo.client.annotations.GoMethodName;
import signalgo.client.annotations.GoServiceName;

/**
 * Created by white on 2016-08-17.
 */

@GoServiceName(name = Constants.SERVICE_INVOKE_NAME, type = GoServiceName.GoClientType.Android, usage = GoServiceName.GoUsageType.invoke)
public class ApiManagerInvoke implements ClientDuplex {

    private static Connector connector = new Connector();


    @GoMethodName(name = "AddGamePlayQOK", type = GoMethodName.MethodType.invoke)
    public static void addGamePlayQOK(Location location, GoResponseHandler<MessageContract<GamePlayInfo>> handler) {
        handler.setTypeToken(new TypeToken<MessageContract<GamePlayInfo>>(MessageContract.class) {
        });

        connector.autoInvokeAsync(handler, location.getLatitude(), location.getLongitude());
    }


    @GoMethodName(name = "StartGamePlayQOK", type = GoMethodName.MethodType.invoke)
    public static void startGamePlay(int gamePlayId, GoResponseHandler<MessageContract<List<Question>>> handler) {
        handler.setTypeToken(new TypeToken<MessageContract<List<Question>>>(MessageContract.class) {
        });

        connector.autoInvokeAsync(handler, gamePlayId, 0);
    }

    @GoMethodName(name = "SendAnswerQOK", type = GoMethodName.MethodType.invoke)
    public static void sendAnswerQuestion(int gamePlayId, int questionId, int answer, GoResponseHandler<MessageContract<MyPair<Integer, Integer>>> handler) {
        handler.setTypeToken(new TypeToken<MessageContract<MyPair<Integer, Integer>>>(MessageContract.class) {
        });
        connector.autoInvokeAsync(handler, 0, gamePlayId, questionId, answer);
    }

    @GoMethodName(name = "FinishGamePlayQOK", type = GoMethodName.MethodType.invoke)
    public static void finishGamePlay(int gamePlayId, GoResponseHandler<MessageContract<MyPair<Integer, Integer>>> handler) {
        handler.setTypeToken(new TypeToken<MessageContract<MyPair<Integer, Integer>>>(MessageContract.class) {
        });

        connector.autoInvokeAsync(handler, gamePlayId);
    }

    //! output first = score and second = rightAnswer
//    @GoMethodName(name = "FinishGamePlayQOK", type = GoMethodName.MethodType.invoke)
//    public static void finishGamePlay(int gamePlayId, GoResponseHandler<MessageContract<MyPair<Integer, Integer>>> handler) {
//        handler.setTypeToken(new TypeToken<MessageContract<MyPair<Integer, Integer>>>(MessageContract.class) {
//        });
//
//        connector.autoInvokeAsync(handler, gamePlayId);
//    }

    @GoMethodName(name = "LoginAPI", type = GoMethodName.MethodType.invoke)
    public static void loginApi(String key, String pass, String imei, String userId, GoResponseHandler<MessageContract<Integer>> handler) {
        handler.setTypeToken(new TypeToken<MessageContract<Integer>>(MessageContract.class) {
        });
        connector.autoInvokeAsync(handler, key, pass, imei, userId);
    }

    @Override
    public void getConnector(Connector connector) {
        this.connector = connector;
    }

}

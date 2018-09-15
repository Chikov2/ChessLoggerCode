package com.epitel.chesslogger.chesslogger;

import android.os.AsyncTask;
import android.os.NetworkOnMainThreadException;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by PChikov on 5/14/2015.
 */
public class SendErrorsTask extends AsyncTask<String, Integer, String> {
    @Override
    protected String doInBackground(String... params) {
        String sessionID = params[0];
        String message = params[1];
        String messageType = params[2];

        String METHOD_NAME = "StorePossibleErrorMessage";
        String NAMESPACE = "http://tempuri.org/";
        String URL = "http://cherchikonline.azurewebsites.net/FileSend.svc";
        String SOAP_ACTION = "http://tempuri.org/IFileSender/StorePossibleErrorMessage";

        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

        request.addProperty("sessionID", sessionID);
        request.addProperty("message", message);
        request.addProperty("type", messageType);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        new MarshalBase64().register(envelope);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);

        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

        try {
            androidHttpTransport.call(SOAP_ACTION, envelope);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Error", "IOException" + e.getMessage());
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            Log.d("Error", "XmlPullParserException" + e.getMessage());
        } catch (NetworkOnMainThreadException e) {
            e.printStackTrace();
            Log.d("Error", "NetworkOnMainThreadException" + e.getMessage());
        }

        String result = "";

        try {
            result = envelope.getResponse().toString();
        } catch (SoapFault soapFault) {
            soapFault.printStackTrace();
        }

        return result;
    }
}


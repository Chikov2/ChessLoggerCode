package com.epitel.chesslogger.chesslogger;

import android.content.Context;
import android.os.AsyncTask;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.ref.WeakReference;


public class SendLogFileTask extends AsyncTask<String, Integer, String> {

    private WeakReference<Context> mContext;

    SendLogFileTask(final Context context)
    {
        mContext = new WeakReference<>(context);
    }

    @Override
    protected void onPostExecute(String result)  {
        if (result.equals("SUCCESS"))
            Toast.makeText(mContext.get(), "Sent", Toast.LENGTH_SHORT).show();
        else Toast.makeText(mContext.get(), "Error Sending Log: Try again later", Toast.LENGTH_LONG).show();
    }

    @Override
    protected String doInBackground(String... params) {
        String emailAddresses = params[0];
        String subject = params[1];
        String log = params[2];

        String METHOD_NAME = "SendEmail";
        String NAMESPACE = "http://tempuri.org/";
        String URL = "http://cherchikonline.azurewebsites.net/FileSend.svc";
        String SOAP_ACTION = "http://tempuri.org/IFileSender/SendEmail";

        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

        request.addProperty("from", "chessLogger@chessLogger.com");
        request.addProperty("fromDescriptiveName", "Chess Logger");
        request.addProperty("emailAddresses", emailAddresses);
        request.addProperty("subject", subject);
        request.addProperty("body", log);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        new MarshalBase64().register(envelope);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);

        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

        try {
            androidHttpTransport.call(SOAP_ACTION, envelope);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Error", "IOException" + e.getMessage());
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            Log.e("Error", "XmlPullParserException" + e.getMessage());
        } catch (NetworkOnMainThreadException e) {
            e.printStackTrace();
            Log.e("Error", "NetworkOnMainThreadException" + e.getMessage());
        }

        String result = "";

        try {
            result = envelope.getResponse().toString();
        } catch (SoapFault soapFault) {
            soapFault.printStackTrace();
            Log.e("Error", "SoapFault" + soapFault.getMessage());
        }

        return result;
    }
}

package com.example.getcomp04.sendtest;

import android.hardware.SensorEventListener;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by GETComp04 on 04/10/2016.
 */
public class Envio extends AsyncTask<String, Void, Boolean>
{
    private String text;
    Envio(String text){this.text=text;}

    void setText (String text){this.text=text;}
    protected Boolean doInBackground(String... params) {try {

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://10.5.23.193:3000/");   //Configurando para qual servidor o dado será enviado
        UrlEncodedFormEntity form;
        try {
            // Log.i("kirim ke node isitextAsli ",text);
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();


            nameValuePairs.add(new BasicNameValuePair(text,"/")); // O dado é enviado na forma de uma soma de duas strings
           form=new UrlEncodedFormEntity(nameValuePairs,"UTF-8");
            httppost.setEntity(form);

            HttpResponse response = httpclient.execute(httppost);


            // Log.i("HTTP Post", "Response from server node = " + response.getStatusLine().getReasonPhrase() + "  Code = " + response.getStatusLine().getStatusCode());
        } catch (ClientProtocolException e) {
            // Log.e("HTTP Post", "Protocol error = " + e.toString());
        } catch (IOException e) {
            //Log.e("HTTP Post", "IO error = " + e.toString());
        }
    } finally{}
        return true;
}}

package com.example.getcomp04.sendtest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.red5pro.streaming.R5Connection;
import com.red5pro.streaming.R5Stream;
import com.red5pro.streaming.R5StreamProtocol;
import com.red5pro.streaming.config.R5Configuration;
import com.red5pro.streaming.view.R5VideoView;
//import com.google.vr.sdk.widgets.video.VrVideoView;
//import com.google.vr.sdk.widgets.video.VrVideoView.Options;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //Variaveis relacionadas ao trabalho com sensores
    float[] mGravity;
    float[] orientation;
    boolean check = true;
    private SensorManager sManager;
    private int y,z,zi=0,yi=0;

    //Variaveis relacionadas à transmissão de video
    public R5Configuration configuration;
    protected R5Stream stream;
    protected boolean isSubscribing = false;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //get a hook to the sensor service
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        configuration = new R5Configuration(R5StreamProtocol.RTSP, "10.5.23.193",  8554, "live", 1.0f, "live");
        Button publishButton = (Button)findViewById(R.id.subscribeButton);
        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSubscribeToggle();
            }
        });
    }

    private void onSubscribeToggle() {
        Button subscribeButton = (Button)findViewById(R.id.subscribeButton);
        if(isSubscribing) {
            stop();
        }
        else {
            start();
        }
        isSubscribing = !isSubscribing;
        subscribeButton.setText(isSubscribing ? "stop" : "start");
    }

    public void start() {
        R5VideoView videoView = (R5VideoView)findViewById(R.id.subscribeView);

        stream = new R5Stream(new R5Connection(configuration));
        videoView.attachStream(stream);
        stream.play("teste");
    }

    public void stop() {
        if(stream != null) {
            stream.stop();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(isSubscribing) {
            onSubscribeToggle();
        }
    }

    //when this Activity starts
    @Override
    protected void onResume() {
        super.onResume();
        //Registrando sensores para obter orientação
        sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
    }


    //When this Activity isn't visible anymore
    @Override
    protected void onStop() {
        //unregister the sensor listener
        sManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        //Do nothing.
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //if sensor is unreliable, return void
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }
        //Verificando resposta do sensor acelerometro
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        //Verificando resposta dos sensores de orientação
        if(event.sensor.getType() == Sensor.TYPE_ORIENTATION)
            orientation = event.values;


        //Utilizando acelerometro para definir orientação para cima e para baixo
        if(mGravity!=null & orientation!=null)
        {
            if(mGravity[2]<0)
                y=Math.round(180-orientation[2]);
            else
                y=Math.round(orientation[2]);

            z=Math.round(orientation[0]);
            //Os valores sao arredondados pois queremos trabalhar com numeros inteiros
             //Log.d("Accel:", "x:"+mGravity[0]+"z:"+mGravity[2]);
             //Log.d("POS:", "z:"+orientation[0]+"y:"+orientation[2]);

            //Bloqueia a transferencia de dados incorretos durante as oscilações do sensor
            if(!check)
            {
                check=true;
                return;
            }
            else check=false;
            //Limitando a frequencia com que os dados sao transmitidos para evitar repetição de dados identicos
            if ( Math.abs(zi - z) > 2 || Math.abs (yi-y)> 2) {

                zi = z;
                z=z+100;
                yi=y;
                y=y+100;
                //Somando 100 ao valor do angulo para que o valor chegue no arduino de forma esperada.

                //Criando um novo Async Task que enviará os angulos para o servidor node.
                new Envio( z + "Dados" + y ).execute("Start");
            }

        }

    }
}


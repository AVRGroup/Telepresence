/*
 * Copyright 2014 Google Inc. All Rights Reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sveder.cardboardpassthrough;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.rtp.AudioCodec;
import android.net.rtp.AudioGroup;
import android.net.rtp.AudioStream;
import android.net.rtp.RtpStream;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.vrtoolkit.cardboard.*;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Enumeration;
import static android.provider.AlarmClock.EXTRA_MESSAGE;

/**
 * A Cardboard sample application.
 */
public class MainActivity extends CardboardActivity implements CardboardView.StereoRenderer, OnFrameAvailableListener, SensorEventListener {

    //Variaveis relacionadas ao trabalho com sensores
    float[] mGravity;
    float[] orientation;
    private boolean check = true;
    private boolean set = false;
    private boolean controle = false;
    private SensorManager sManager;
    private int y,z,zi=0,yi=0,aux,meio,val;
    private int[] escala = new int[361];
    public Button controlButton;

    private static final String TAG = "MainActivity";
    private static final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
    private Camera camera;

	private final String vertexShaderCode =
	        "attribute vec4 position;" +
	        "attribute vec2 inputTextureCoordinate;" +
	        "varying vec2 textureCoordinate;" +
	        "void main()" +
	        "{"+
	            "gl_Position = position;"+
	            "textureCoordinate = inputTextureCoordinate;" +
	        "}";

	    private final String fragmentShaderCode =
	        "#extension GL_OES_EGL_image_external : require\n"+
	        "precision mediump float;" +
	        "varying vec2 textureCoordinate;                            \n" +
	        "uniform samplerExternalOES s_texture;               \n" +
	        "void main(void) {" +
	        "  gl_FragColor = texture2D( s_texture, textureCoordinate );\n" +
	        //"  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);\n" +
	        "}";

        private FloatBuffer vertexBuffer, textureVerticesBuffer, vertexBuffer2;
        private ShortBuffer drawListBuffer, buf2;
        private int mProgram;
        private int mPositionHandle, mPositionHandle2;
        private int mColorHandle;
        private int mTextureCoordHandle;


    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 2;
    static float squareVertices[] = { // in counterclockwise order:
    	-1.0f, -1.0f,   // 0.left - mid
    	 1.0f, -1.0f,   // 1. right - mid
    	-1.0f, 1.0f,   // 2. left - top
    	 1.0f, 1.0f,   // 3. right - top
//
//    	 -1.0f, -1.0f, //4. left - bottom
//    	 1.0f , -1.0f, //5. right - bottom


//       -1.0f, -1.0f,  // 0. left-bottom
//        0.0f, -1.0f,   // 1. mid-bottom
//       -1.0f,  1.0f,   // 2. left-top
//        0.0f,  1.0f,   // 3. mid-top

        //1.0f, -1.0f,  // 4. right-bottom
        //1.0f, 1.0f,   // 5. right-top

    };




    //, 1, 4, 3, 4, 5, 3
//    private short drawOrder[] =  {0, 1, 2, 1, 3, 2 };//, 4, 5, 0, 5, 0, 1 }; // order to draw vertices
    private short drawOrder[] =  {0, 2, 1, 1, 2, 3 }; // order to draw vertices
    private short drawOrder2[] = {2, 0, 3, 3, 0, 1}; // order to draw vertices

    static float textureVertices[] = {
	 0.0f, 1.0f,  // A. left-bottom
	   1.0f, 1.0f,  // B. right-bottom
	   0.0f, 0.0f,  // C. left-top
	   1.0f, 0.0f   // D. right-top

//        1.0f,  1.0f,
//        1.0f,  0.0f,
//        0.0f,  1.0f,
//        0.0f,  0.0f
   };

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private ByteBuffer indexBuffer;    // Buffer for index-array

    private int texture;


    private CardboardOverlayView mOverlayView;


	private CardboardView cardboardView;
	private SurfaceTexture surface;
	private float[] mView;
	private float[] mCamera;
	private IjkMediaPlayer player;

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://10.5.17.111:3000"); // Endereço do servidor
        } catch (URISyntaxException e) {}
    }

    AudioManager audio;
    AudioGroup audioGroup;
    AudioStream audioStream;

    private boolean participante = false;
    private FrameLayout frameButtons;

	public void startCamera(int texture) {
        surface = new SurfaceTexture(texture);
        surface.setOnFrameAvailableListener(this);
            player = new IjkMediaPlayer();
             //player.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
        //player.setAudioStreamType(AudioManager.MODE_IN_CALL);;
            AssetFileDescriptor afd = null;
            try {
                player.setDataSource("rtmp://10.5.17.111/live/testing"); //Coloque o endereço de sua streaming
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            player.setSurface(new Surface(surface));
            player.setLooping(true);
            try {
                player.prepareAsync();
                player.setVolume(0,0);
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //
        //setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);


    }
	
    static private int createTexture()
    {
        int[] texture = new int[1];

        GLES20.glGenTextures(1,texture, 0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
             GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);        
        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
             GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
     GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES,
             GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
     GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES,
             GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        return texture[0];
    }

	
    /**
     * Converts a raw text file, saved as a resource, into an OpenGL ES shader
     * @param type The type of shader we will be creating.

     * @return
     */
    private int loadGLShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        if (shader == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shader;
    }

    /**
     * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
     * @param func
     */
    private static void checkGLError(String func) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, func + ": glError " + error);
            throw new RuntimeException(func + ": glError " + error);
        }
    }

    /**
     * Sets the view to our CardboardView and initializes the transformation matrices we will use
     * to render our scene.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_ui);

        cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRenderer(this);
        setCardboardView(cardboardView);
        frameButtons = (FrameLayout)findViewById(R.id.frameBtn);
        frameButtons.setVisibility(View.VISIBLE);
         controlButton = (Button) findViewById(R.id.control);
        controlButton.setVisibility(View.VISIBLE);
        controlButton.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v)
            {
                controle = !controle;
                if(controle) controlButton.setText("Control ON");
                else controlButton.setText("Control OFF");

            }
        });


        mCamera = new float[16];
        mView = new float[16];
        mOverlayView = (CardboardOverlayView) findViewById(R.id.overlay);
        mOverlayView.show3DToast("Pull the magnet when you find an object.");
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSocket.connect();
    }

    public void initAudioReturn(){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            audio =  (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audio.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audio.setSpeakerphoneOn(true);
            audioGroup = new AudioGroup();
            audioGroup.setMode(AudioGroup.MODE_NORMAL);
            audioStream = new AudioStream(InetAddress.getByAddress(getLocalIPAddress ()));
            audioStream.setCodec(AudioCodec.PCMU);
            audioStream.setMode(RtpStream.MODE_NORMAL);
            //set receiver(vlc player) machine ip address(please update with your machine ip)10.5.17.111
            audioStream.associate(InetAddress.getByAddress(new byte[] {(byte)10, (byte)5, (byte)17, (byte)111 }), 22222);
            audioStream.join(audioGroup);
        } catch (Exception e) {
            Log.e("----------------------", e.toString());
            e.printStackTrace();
        }
    }

    public void espectadorOnClick(View v){
        frameButtons = (FrameLayout)findViewById(R.id.frameBtn);
        frameButtons.setVisibility(View.GONE);
        player.setVolume(100,100);
    }



    public void participanteOnClick(View v){
        participante = true;
        frameButtons = (FrameLayout)findViewById(R.id.frameBtn);
        frameButtons.setVisibility(View.GONE);
        player.setVolume(100,100);
        initAudioReturn();
    }

    public static byte[] getLocalIPAddress () {
        byte ip[]=null;
        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        ip= inetAddress.getAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.i("SocketException ", ex.toString());
        }
        return ip;

    }

    @Override
    public void onRendererShutdown() {
        Log.i(TAG, "onRendererShutdown");
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.i(TAG, "onSurfaceChanged");
    }

    /**
     * Creates the buffers we use to store information about the 3D world. OpenGL doesn't use Java
     * arrays, but rather needs data in a format it can understand. Hence we use ByteBuffers.
     * @param config The EGL configuration used when creating the surface.
     */
    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated");
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text shows up well
        
        ByteBuffer bb = ByteBuffer.allocateDirect(squareVertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareVertices);
        vertexBuffer.position(0);


        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);
        

        ByteBuffer bb2 = ByteBuffer.allocateDirect(textureVertices.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        textureVerticesBuffer = bb2.asFloatBuffer();
        textureVerticesBuffer.put(textureVertices);
        textureVerticesBuffer.position(0);

        int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);
        
        texture = createTexture();

        startCamera(texture);
    
    }

    
    /**
     * Prepares OpenGL ES before we draw a frame.
     * @param headTransform The head transformation in the new frame.
     */
    @Override
    public void onNewFrame(HeadTransform headTransform) {   	
    	float[] mtx = new float[16];
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        surface.updateTexImage();
        surface.getTransformMatrix(mtx); 
    	
    }
	
    @Override
	public void onFrameAvailable(SurfaceTexture arg0) {
		this.cardboardView.requestRender();
		
	}    	   

    /**
     * Draws a frame for an eye. The transformation for that eye (from the camera) is passed in as
     * a parameter.
     * @param transform The transformations to apply to render this eye.
     */
    @Override
    public void onDrawEye(EyeTransform transform) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        
        GLES20.glUseProgram(mProgram);

        GLES20.glActiveTexture(GL_TEXTURE_EXTERNAL_OES);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, texture);



        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "position");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
        		false,vertexStride, vertexBuffer);
        

        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
        GLES20.glVertexAttribPointer(mTextureCoordHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
        		false,vertexStride, textureVerticesBuffer);

        mColorHandle = GLES20.glGetAttribLocation(mProgram, "s_texture");



        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
        					  GLES20.GL_UNSIGNED_SHORT, drawListBuffer);


        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);
        
        Log.e("status",transform.getEyeView()+"");
        
        Matrix.multiplyMM(mView, 0, transform.getEyeView(), 0, mCamera, 0);

    }

    @Override
    public void onFinishFrame(Viewport viewport) {
    }


    /**
     * Increment the score, hide the object, and give feedback if the user pulls the magnet while
     * looking at the object. Otherwise, remind the user what to do.
     */
    @Override
    public void onCardboardTrigger() {
//        Log.i(TAG, "onCardboardTrigger");
//
//        if (isLookingAtObject()) {
//            mScore++;
//            mOverlayView.show3DToast("Found it! Look around for another one.\nScore = " + mScore);
//            hideObject();
//        } else {
//            mOverlayView.show3DToast("Look around to find the object!");
//        }
//        // Always give user feedback
//        mVibrator.vibrate(50);
    }

    //when this Activity starts
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        player.release();
    }

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

        if(!controle) return;
        //if sensor is unreliable, return void
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }
        //Log.v("TESTE", "Z");
        //CELULAR DO LEANDRO NÃO PASSA NO if A SEGUIR
        //Verificando resposta do sensor acelerometro
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            mGravity = event.values;
            //Log.v("TESTE", "A");
        }

        //Verificando resposta dos sensores de orientação
        if(event.sensor.getType() == Sensor.TYPE_ORIENTATION){
           // Log.v("TESTE", "B");
            orientation = event.values;
        }



        //Utilizando acelerometro para definir orientação para cima e para baixo
        if(mGravity!=null & orientation!=null)
        {
           // Log.v("TESTE", "UM");
            if(mGravity[2]<0) {
                y = Math.round(180 - orientation[2]);
              //  Log.v("TESTE", "DOIS");
            }
            else{
            y = Math.round(orientation[2]);
          //  Log.v("TESTE", "TRES");
            }
            z=Math.round(orientation[0]);

            //if(z>180) z = Math.abs(180-z);// Movimento Correto com mudança abrupta apos 180
            //if(z>180) z = 360 - z; //Ciclo Continuo com movimento oposto apos 180
            //Os valores sao arredondados pois queremos trabalhar com numeros inteiros
            //Log.d("Accel:", "x:"+mGravity[0]+"z:"+mGravity[2]);
            //Log.d("POS:", "z:"+orientation[0]);

            //Bloqueia a transferencia de dados incorretos durante as oscilações do sensor
            if(!check)
            {
                check=true;
                return;
            }
            else check=false;
           // Log.v("TESTE", "QUATRO");
            //Modificando orientações!!
            if(!set)
            {
                set = true;
                escala[z] = 90;
                if(z>270)
                {
                    aux = 90-(360-z);
                    for(int s = z+1; s<=360; s++)
                    {
                        escala[s] = escala[s-1] + 1;
                    }
                    escala[0]=escala[360];
                    for(int s = 1; s <= aux ; s++)
                    {
                        escala[s] = escala[s-1] + 1;
                    }
                    for(int s = z-1; s>=(z-90); s--)
                    {
                        escala[s] = escala[s + 1] - 1;
                    }
                     meio = (aux + 1 + z - 90)/2;
                    for(int s = aux + 1; s< (z-90); s++)
                    {
                        if(s<=meio) escala[s] = 180;
                        else escala[s] = 0;
                    }


                }

              else  if ( z < 90)
                {
                    aux = 90-z;
                    for(int s = z+1; s <= (z+90);s++ )
                        escala[s] = escala[s-1] + 1;
                    for(int s = z-1; s >= 0; s--)
                        escala[s] = escala[s+1] - 1;
                    escala[360] = escala [0];
                    for(int s= 359; s>= (360-aux); s-- )
                        escala[s] = escala[s+1] - 1;
                    meio = (z+90+360-aux)/2;
                    for(int s = z+90+1; s<= (360-aux); s++)
                    {
                        if(s<=meio) escala[s] = 180;
                        else escala[s] = 0;
                    }
                }
                else
                {
                    for(int s=1;s<=90;s++)
                    {
                        escala[z+s] = escala[z+s-1] + 1;
                        escala[z-s] = escala[z-s+1] - 1;
                    }
                    meio = z+180;
                    if(meio > 360 )
                    {
                        meio = meio - 360;
                        for(int s = z+90+1; s<=meio || (s>z+90&&s<=360); s++)
                        {
                            if(s>=360)
                            {
                                escala[s]=180;
                                s=0;
                            }
                            escala[s] = 180;
                        }
                        for(int s = meio +1; s<= (z-90);s++ )
                            escala[s] = 0;
                    }
                    else
                    {
                           for(int s = z+90+1; s<=meio; s++)
                               escala[s]= 180;
                            for(int s = meio+1; s<= (z-90); s++)
                            {
                                if(s>=360)
                                {
                                    escala[s]=0;
                                    s=0;
                                }
                                escala[s]=0;
                            }
                    }

                }


            }
            z=escala[z];
          //  Log.v("TESTE", "CINCO");
            //Limitando a frequencia com que os dados sao transmitidos para evitar repetição de dados identicos
           if ( Math.abs(zi - z) > 1|| Math.abs (yi-y)> 1) {

                zi = z;
                z=z+100;
                yi=y;
                y=y+100;
                //Somando 100 ao valor do angulo para que o valor chegue no arduino de forma esperada.

                //Criando um novo Async Task que enviará os angulos para o servidor node.
                //new Envio( z + "Dados" + y ).execute("Start");
                mSocket.emit("new message", z + "Dados" + y);
              // Log.v("TESTE", "Z " + z + " Y " + y);

           }

            }

        }

    }



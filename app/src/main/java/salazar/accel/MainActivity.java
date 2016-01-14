package salazar.accel;
import android.os.StrictMode;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;



public class MainActivity extends ActionBarActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;

    Button startButton;
    Button stopButton;
    Button sendButton;

    TextView tvX;
    TextView tvY;
    TextView tvZ;
    List<String> entries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }


        startButton = (Button)findViewById(R.id.startButton);
        stopButton = (Button)findViewById(R.id.stopButton);
        sendButton = (Button)findViewById(R.id.sendButton);
        tvX= (TextView)findViewById(R.id.x_axis);
        tvY= (TextView)findViewById(R.id.y_axis);
        tvZ= (TextView)findViewById(R.id.z_axis);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!=null)

        {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        else

        {
            // Failure! No accl.
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startButtonFunc(View view) {
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        entries = new ArrayList<String>();
    }


    public void stopButtonFunc(View view) {

        mSensorManager.unregisterListener(this);

        Boolean success=true;
        CSVWriter writer = null;

        File file;
        file = new File(Environment.getExternalStorageDirectory () +"/map/acclData.csv");
        try {
            file.createNewFile();
        } catch (IOException e) {
            Log.v("create new file", "not working!!!");
        }
        MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
        if(!file.exists()){
            success =file.mkdirs();
        Log.v("mkdirs() failed","directory not created");
        }
        else {
            Log.v("mkdirs()", "successful");
        }
        if(!success){
            Log.v("mkdirs()","unsuccessful");
        }

        if(file.exists() && !file.isDirectory()){
            FileWriter mFileWriter = null;
            try {
                mFileWriter = new FileWriter(file.getAbsolutePath(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            writer = new CSVWriter(mFileWriter);
        }
        else {
            try {
                writer = new CSVWriter(new FileWriter(file.getAbsolutePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for(int i=0;i<entries.size();i++){

            String[] eachLine = entries.get(i).split("#");
            writer.writeNext(eachLine);
        }

        try {
            Toast.makeText(this,"File Created",Toast.LENGTH_LONG).show();

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendButtonFunc(View view) {

        Socket serverSocket;
        try {
            serverSocket = new Socket("192.168.49.145", 8887);
            Toast.makeText(this,"Connecting...",Toast.LENGTH_LONG).show();

            Log.v("Server : ", "Connecting...");

            // sendfile
            File file;
            file = new File(Environment.getExternalStorageDirectory () +"/map/acclData.csv");

            if(file.exists() && !file.isDirectory()) {
                byte[] byteArray = new byte[(int) file.length()];
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                bis.read(byteArray, 0, byteArray.length);
                OutputStream os = serverSocket.getOutputStream();
                Log.v("Server: ","Sending...");
                os.write(byteArray, 0, byteArray.length);
                os.flush();
                BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
                in.ready();
                serverSocket.shutdownOutput();
                String userInput = in.readLine();
                System.out.println("Response from server..." + userInput);
                Toast.makeText(this,"Response from server..."+userInput,Toast.LENGTH_LONG).show();
        serverSocket.close();
            }

            else{

                Log.v("Send","file not found");
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String currentDateandTime = sdf.format(new Date());

        tvX.setText(String.format("%.2f", x));
        tvY.setText(String.format("%.2f", y));
        tvZ.setText(String.format("%.2f", z));
        StringBuilder strBuild=new StringBuilder();
        strBuild.append(currentDateandTime);
        strBuild.append('#');
        strBuild.append(String.format("%.2f", x));
        strBuild.append('#');
        strBuild.append(String.format("%.2f", y));
        strBuild.append('#');
        strBuild.append(String.format("%.2f", z));
        String line=strBuild.toString();

        entries.add(line);


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}





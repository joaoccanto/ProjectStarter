package com.example.projectstarter;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class MainActivity extends WearableActivity implements SensorEventListener  {

    private TextView mTextView;
    private SensorManager sensorManager;
    Sensor heartRateSensor, accelerometer, gyroscope;
    private static final int Request_User_Physical_Code = 1024;
    long startTime = 0;
    static double min,max,var,std, energy, zeroCrossingRate;
    double[] values = new double[100];
    double[] accelerometerMag = new double[500];
    double[] gyroscopeMag = new double[500];
    int heartRateIndex = 0;
    int accelerometerIndex = 0;
    int gyroscopeIndex = 0;
    int currentTimeForHeartRate = 0;
    int currentTimeForAccelerometer = 0;
    int currentTimeForGyroscope = 0;
    int interval = 5; // 5 seconds
    StringBuilder heartRateData, accelerometerData, gyroscopeData;
    TextView status;
    private PowerManager.WakeLock wl;
    String TAG = "tag";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if((ContextCompat.checkSelfPermission(this,
                Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_DENIED) ||
                (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) ||
                (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_DENIED)){
            //ask for permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.BODY_SENSORS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, Request_User_Physical_Code);
            }
        }

        PowerManager pm =  (PowerManager)getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);

        //mTextView = (TextView) findViewById(R.id.status_text_view);
        status = (TextView)findViewById(R.id.status_text_view);

        // Enables Always-on
        setAmbientEnabled();
    }

    public void onStopClick(View view) {
        wl.release();
        status.setText("NOT RECORDING");
        sensorManager.unregisterListener(this);
        writeDataToFile(heartRateData, "heart_rate.csv");
        writeDataToFile(accelerometerData, "accelerometer.csv");
        writeDataToFile(gyroscopeData, "gyroscope.csv");

    }

    public void onStartClick(View view) {
        wl.acquire();
        startTime = System.currentTimeMillis();
        status.setText("RECORDING");

        heartRateData = new StringBuilder();
         accelerometerData = new StringBuilder();
         gyroscopeData = new StringBuilder();

        sensorManager = (SensorManager)  getSystemService(Context.SENSOR_SERVICE);

        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        sensorManager.registerListener(MainActivity.this, heartRateSensor ,sensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(MainActivity.this, accelerometer ,sensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(MainActivity.this, gyroscope ,sensorManager.SENSOR_DELAY_NORMAL);
    }
    private void writeDataToFile(StringBuilder dataToWrite, String fileName) {

        Context context = getApplicationContext();
        try {
            FileWriter out = new FileWriter(new File(context.getFilesDir(), fileName), true);
            out.write(String.valueOf(dataToWrite));
            out.close();
        } catch (IOException e)
        {
            Log.d("error", String.valueOf(e));
        }
    }

    private void onHeartRateSensorChanged(SensorEvent event) {
        long millis = System.currentTimeMillis() - startTime;
        int seconds = (int) (millis / 1000);

        if ((seconds % interval == 0 && currentTimeForHeartRate != seconds) || heartRateIndex == 100 ){

            currentTimeForHeartRate = seconds;
            min = Features.minimum(values);
            max = Features.maximum(values);
            var = Features.variance(values);
            std = Features.stdDev(values);
            energy = Features.energy(values);
            zeroCrossingRate = Features.zeroCrossingRate(values);
            Arrays.fill(values, 0.0);

            String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
            heartRateData.append(timeStamp + "," +String.valueOf(min) + "," + String.valueOf(max) + "," + String.valueOf(var) + "," + String.valueOf(std) + "," + String.valueOf(energy) + "," + String.valueOf(zeroCrossingRate) + "\n");
            Log.d("JOAO1 - HR", " " + timeStamp + " " + min + " " + max + " " + var + " " + std + " " + energy + " " + zeroCrossingRate);
            heartRateIndex = 0;
        } else {
            values[heartRateIndex] = event.values[0];
            heartRateIndex++;
        }
    }

    private void onAccelerometerChanged(SensorEvent event) {
        double magnitude = Features.magnitude(event.values[0], event.values[1], event.values[2]);

        long millis = System.currentTimeMillis() - startTime;
        int seconds = (int) (millis / 1000);

        Log.d("ACC", "  " + currentTimeForAccelerometer + "  " + seconds + "  " + accelerometerIndex + event.sensor.getName());
        if ((seconds % interval == 0 && currentTimeForAccelerometer != seconds) || accelerometerIndex == 500 ){
            currentTimeForAccelerometer = seconds;
            min = Features.minimum(accelerometerMag);
            max = Features.maximum(accelerometerMag);
            var = Features.variance(accelerometerMag);
            std = Features.stdDev(accelerometerMag);
            energy = Features.energy(accelerometerMag);
            zeroCrossingRate = Features.zeroCrossingRate(accelerometerMag);
            Arrays.fill(accelerometerMag, 0.0);

            String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
            accelerometerData.append(timeStamp + "," +String.valueOf(min) + "," + String.valueOf(max) + "," + String.valueOf(var) + "," + String.valueOf(std) + "," + String.valueOf(energy) + "," + String.valueOf(zeroCrossingRate) + "\n");
            Log.d("ACC - ", " " + timeStamp + " " + min + " " + max + " " + var + " " + std + " " + energy + " " + zeroCrossingRate);
            accelerometerIndex = 0;
        } else {
            accelerometerMag[accelerometerIndex] = magnitude;
            accelerometerIndex++;
        }
    }
    private void onGyroscopeChanged(SensorEvent event) {
        double magnitude = Features.magnitude(event.values[0], event.values[1], event.values[2]);

        long millis = System.currentTimeMillis() - startTime;
        int seconds = (int) (millis / 1000);

        Log.d("GYR", "  " + currentTimeForGyroscope + "  " + seconds + "  " + gyroscopeIndex + event.sensor.getName());

        if ((seconds % interval == 0 && currentTimeForGyroscope != seconds) || gyroscopeIndex == 500 ){
            currentTimeForGyroscope = seconds;
            min = Features.minimum(gyroscopeMag);
            max = Features.maximum(gyroscopeMag);
            var = Features.variance(gyroscopeMag);
            std = Features.stdDev(gyroscopeMag);
            energy = Features.energy(gyroscopeMag);
            zeroCrossingRate = Features.zeroCrossingRate(gyroscopeMag);
            Arrays.fill(gyroscopeMag, 0.0);

            String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
            gyroscopeData.append(timeStamp + "," +String.valueOf(min) + "," + String.valueOf(max) + "," + String.valueOf(var) + "," + String.valueOf(std) + "," + String.valueOf(energy) + "," + String.valueOf(zeroCrossingRate) + "\n");
            Log.d("JOAO1 - GYR", " " + timeStamp + " " + min + " " + max + " " + var + " " + std + " " + energy + " " + zeroCrossingRate);
            gyroscopeIndex = 0;
        } else {
            gyroscopeMag[gyroscopeIndex] = magnitude;
            gyroscopeIndex++;
        }
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        //Log.d("JOAO - SENSOR", event.sensor.getName());
        String sensorName = event.sensor.getName();

        switch (sensorName){
            case "pah_hr  Non-wakeup":
                onHeartRateSensorChanged(event);
                break;
            case "BMI120 Accelerometer Non-wakeup":
                onAccelerometerChanged(event);
                break;
            case "BMI120 Gyroscope Non-wakeup":
                onGyroscopeChanged(event);
                break;
            default:
                //do nothing
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
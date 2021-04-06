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
    double[] values = new double[50];
    double[] accelerometerMag = new double[50];
    double[] gyroscopeMag = new double[50];
    int i = 0;
    int currentTimeForHeartRate = 0;
    int currentTimeForAccelerometer = 0;
    int interval = 5; // 5 seconds
    StringBuilder heartRateData = new StringBuilder();
    StringBuilder accelerometerData = new StringBuilder();
    StringBuilder gyroscopeData = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if((ContextCompat.checkSelfPermission(this,
                Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_DENIED) ||
                (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)){
            //ask for permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.BODY_SENSORS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, Request_User_Physical_Code);
            }
        }


        mTextView = (TextView) findViewById(R.id.text);
        sensorManager = (SensorManager)  getSystemService(Context.SENSOR_SERVICE);

        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        sensorManager.registerListener(MainActivity.this, heartRateSensor ,sensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(MainActivity.this, accelerometer ,sensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(MainActivity.this, gyroscope ,sensorManager.SENSOR_DELAY_NORMAL);

        Log.d("JOAO 1", heartRateSensor.toString());
        startTime = System.currentTimeMillis();


        // Enables Always-on
        setAmbientEnabled();
    }

    public void onStopClick(View view) {
        sensorManager.unregisterListener(this);
        writeDataToFile(heartRateData, "heart_rate.csv");
        writeDataToFile(accelerometerData, "accelerometer.csv");
        writeDataToFile(gyroscopeData, "gyroscope.csv");
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

        if ((seconds % interval == 0 && currentTimeForHeartRate != seconds) || i == 50 ){

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
            Log.d("JOAO - HR", " " + timeStamp + " " + min + " " + max + " " + var + " " + std + " " + energy + " " + zeroCrossingRate);
            i = 0;
        } else {
            values[i] = event.values[0];
            i++;
        }
    }

    private void onAccelerometerChanged(SensorEvent event) {
        double magnitude = Features.magnitude(event.values[0], event.values[1], event.values[2]);

        long millis = System.currentTimeMillis() - startTime;
        int seconds = (int) (millis / 1000);

        if ((seconds % interval == 0 && currentTimeForAccelerometer != seconds) || i == 50 ){
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
            Log.d("JOAO - ACC", " " + timeStamp + " " + min + " " + max + " " + var + " " + std + " " + energy + " " + zeroCrossingRate);
            i = 0;
        } else {
            accelerometerMag[i] = magnitude;
            i++;
        }
    }
    private void onGyroscopeChanged(SensorEvent event) {
        Log.d("JOAO - GY", " "+ event.values.length);
        double magnitude = Features.magnitude(event.values[0], event.values[1], event.values[2]);

        long millis = System.currentTimeMillis() - startTime;
        int seconds = (int) (millis / 1000);

        if ((seconds % interval == 0 && currentTimeForAccelerometer != seconds) || i == 50 ){
            currentTimeForAccelerometer = seconds;
            min = Features.minimum(gyroscopeMag);
            max = Features.maximum(gyroscopeMag);
            var = Features.variance(gyroscopeMag);
            std = Features.stdDev(gyroscopeMag);
            energy = Features.energy(gyroscopeMag);
            zeroCrossingRate = Features.zeroCrossingRate(gyroscopeMag);
            Arrays.fill(gyroscopeMag, 0.0);

            String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
            gyroscopeData.append(timeStamp + "," +String.valueOf(min) + "," + String.valueOf(max) + "," + String.valueOf(var) + "," + String.valueOf(std) + "," + String.valueOf(energy) + "," + String.valueOf(zeroCrossingRate) + "\n");
            Log.d("JOAO - ACC", " " + timeStamp + " " + min + " " + max + " " + var + " " + std + " " + energy + " " + zeroCrossingRate);
            i = 0;
        } else {
            gyroscopeMag[i] = magnitude;
            i++;
        }
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("JOAO", event.sensor.getName());
        String sensorName = event.sensor.getName();

        switch (sensorName){
            case "pah_hr  Non-wakeup":
                onHeartRateSensorChanged(event);
                break;
            case "BMI120 Accelerometer Non-wakeup":
                onAccelerometerChanged(event);
            case "BMI120 Gyroscope Non-wakeup":
                onGyroscopeChanged(event);
            default:
                //do nothing
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
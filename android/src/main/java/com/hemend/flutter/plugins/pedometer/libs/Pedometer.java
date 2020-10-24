/**
 * Usage:
 *         Pedometer pedometer = new Pedometer((SensorManager) getSystemService(Context.SENSOR_SERVICE));
 *
 *         Register:
 *          pedometer.register();
 *
 *         Unregister:
 *          pedometer.unregister();
 */

package com.hemend.flutter.plugins.pedometer.libs;

import android.annotation.TargetApi;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class Pedometer implements SensorEventListener, StepListener {
    private StepDetector stepDetector;
    private DataManager dataManager;
    private SensorManager sensorManager;
    private StepCallback stepCallback;
    private Sensor accel;
    private int numSteps;
    private int lastNumSteps = 0;

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public Pedometer(SensorManager sm, DataManager dm, StepCallback sc) {
        // Get an instance of the SensorManager
        sensorManager = sm;
        dataManager = dm;
        stepCallback = sc;

        Sensor countSensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        Sensor detectorSensor = sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        Sensor accelerometerSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (countSensor != null) {
            accel = countSensor;
        } else if(detectorSensor != null) {
            accel = detectorSensor;
        } else {
            accel = accelerometerSensor;
        }

        stepDetector = new StepDetector();
        stepDetector.registerListener(this);
    }

    public void setStepCallback(StepCallback sc) {
        stepCallback = sc;
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public void register() {
        numSteps = 0;
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public void unregister() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    @Override
    public void onSensorChanged(SensorEvent event) {
        long timeNs = event.timestamp;
        Sensor sensor = event.sensor;
        float[] values = event.values;

        if (values.length > 0) {
            if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                step((int) values[0]);
            } else if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                if (values[0] == 1.0) {
                    step(timeNs);
                }
            } else if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                stepDetector.updateAccel(timeNs, values[0], values[1], values[2]);
            }
        }
    }

    @Override
    public void step(int steps) {
        numSteps = steps;

        int num = numSteps - lastNumSteps;

        if(lastNumSteps > 0 || num < 10) {
            JSONObject res = dataManager.write(System.currentTimeMillis(), num);
            try {
                if (stepCallback != null && res.has("date") && res.has("step")) {
                    stepCallback.step(res.getString("date"), res.getInt("step"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        lastNumSteps = numSteps;
    }

    @Override
    public void step(long timeNs) {
        numSteps++;
        JSONObject res = dataManager.write(System.currentTimeMillis(), 1);

        try {
            if (stepCallback != null && res.has("date") && res.has("step")) {
                stepCallback.step(res.getString("date"), res.getInt("step"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

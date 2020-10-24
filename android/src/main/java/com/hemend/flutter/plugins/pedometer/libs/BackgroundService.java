package com.hemend.flutter.plugins.pedometer.libs;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class BackgroundService extends Service {
    private final IBinder binder = new BackgroundServiceBinder();
    Pedometer pedometer;
    static String statusFilename = "pedometer-status.txt";
    static String dataFilename = "pedometer-data.txt";
    private static Boolean _status;
    private static DataManager dataManager;
    private StepCallback _stepCallback;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        dataManager = new DataManager(this.getDirPath() + File.separator + dataFilename);

        if(getStatus()) {
            start();
        }
    }

    public boolean getStatus() {
        return getStatus(false);
    }
    public String getDirPath() {
        return this.getFilesDir().getPath().toString();
    }

    public boolean getStatus(Boolean force) {
        if(_status == null || force == true) {
            File f = new File(this.getDirPath() + File.separator + statusFilename);

            if (!f.exists()) {
                setStatus(f, false);
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(f.getPath()))) {
                _status = Boolean.parseBoolean(reader.readLine());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return _status;
    }

    private void setStatus(Boolean status) {
        File f = new File(this.getDirPath() + File.separator + statusFilename);
        setStatus(f, status);
    }

    private void setStatus(File f, Boolean status) {
        _status = status;

        try (FileWriter writer = new FileWriter(f.getPath(), false)) {
            try {
                writer.write(String.valueOf(status));
            } finally {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setStepCallback(StepCallback stepCallback) {
        _stepCallback = stepCallback;
        if(pedometer != null) {
            pedometer.setStepCallback(_stepCallback);
        }
    }

    public void start() {
        setStatus(true);
        if(pedometer == null) {
            pedometer = new Pedometer((SensorManager) getSystemService(Context.SENSOR_SERVICE), dataManager, _stepCallback);
            pedometer.register();
        }
    }

    public DataManager getDb() {
        return dataManager;
    }

    public void stop() {
        setStatus(false);
        if(pedometer != null) {
            pedometer.unregister();
            pedometer = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    @Override
    public void onDestroy() {
        if(pedometer != null) {
            pedometer.unregister();
            pedometer = null;
        }

        super.onDestroy();
        stopSelf();    //stop the service

        if(getStatus()) {
            Intent broadcastIntent = new Intent(this, BackgroundReceiver.class);
            sendBroadcast(broadcastIntent);
        }
    }

    public class BackgroundServiceBinder extends Binder {
        public BackgroundService getService() {
            return BackgroundService.this;
        }
    }
}

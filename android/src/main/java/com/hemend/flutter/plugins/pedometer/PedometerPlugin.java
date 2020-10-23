package com.hemend.flutter.plugins.pedometer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.hemend.flutter.plugins.pedometer.libs.BackgroundService;
import com.hemend.flutter.plugins.pedometer.libs.StepCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.common.BinaryMessenger;

/** PedometerPlugin */
@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class PedometerPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
  private static final String TAG = "PedometerServicesPlugin";
  private Intent serviceIntent;
  private BackgroundService service;
  private boolean serviceConnected = false;
  private Result keepResult = null;
  private MethodChannel channel;
  private static Activity activity;
  private static Context context;
  private Application.ActivityLifecycleCallbacks activityLifecycleCallbacks;

  private StepCallback stepCallback = (date, step) ->  {
    JSONObject json = new JSONObject();
    json.put("date", date).put("step", step);

    if(channel != null) {
      channel.invokeMethod("onSensorChanged", json.toString());
    }
  };

  @Override
  public void onDetachedFromActivity() {}

  @Override
  public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {}

  @Override
  public void onDetachedFromActivityForConfigChanges() {}

  @Override
  public void onAttachedToActivity(ActivityPluginBinding binding) {
    activity = binding.getActivity();
  }

  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
    context = binding.getApplicationContext();
    BinaryMessenger messenger = (BinaryMessenger) binding.getBinaryMessenger();

    channel = new MethodChannel(messenger, "pedometer");
    channel.setMethodCallHandler(this);

    activityLifecycleCallbacks = getActivityLifecycleCallbacks();

    ((Application) context).registerActivityLifecycleCallbacks(activityLifecycleCallbacks);

    connectToService();
  }

  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
  private Application.ActivityLifecycleCallbacks getActivityLifecycleCallbacks() {
    return new Application.ActivityLifecycleCallbacks() {
      @Override
      public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (activity == PedometerPlugin.this.activity) {
        }
      }

      @Override
      public void onActivityStarted(Activity activity) {
        if (activity == PedometerPlugin.this.activity) {
        }
      }

      @Override
      public void onActivityResumed(Activity activity) {
        if (activity == PedometerPlugin.this.activity) {
        }
      }

      @Override
      public void onActivityPaused(Activity activity) {
        if (activity == PedometerPlugin.this.activity) {
        }
      }

      @Override
      public void onActivityStopped(Activity activity) {
        if (activity == PedometerPlugin.this.activity) {
        }
      }

      @Override
      public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        if (activity == PedometerPlugin.this.activity) {
        }
      }

      @Override
      public void onActivityDestroyed(Activity activity) {
        if (activity == PedometerPlugin.this.activity) {
        }
      }
    };
  }

  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    serviceConnected = false;
    channel.setMethodCallHandler(null);
    channel = null;

    if (activity != null && activityLifecycleCallbacks != null) {
      activity
              .getApplication()
              .unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }
  }

  private ServiceConnection connection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName className, IBinder appService) {
      BackgroundService.BackgroundServiceBinder binder = (BackgroundService.BackgroundServiceBinder) appService;
      service = binder.getService();
      service.setStepCallback(stepCallback);
      serviceConnected = true;
      Log.i(TAG, "Service connected");
      if (keepResult != null) {
//        keepResult.success(true);
        keepResult = null;
      }
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
      serviceConnected = false;
      Log.i(TAG, "Service disconnected");
    }
  };

  private void connectToService() {
    if (!serviceConnected) {
      serviceIntent = new Intent(context, BackgroundService.class);
      context.startService(serviceIntent);
      context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
    } else {
      if (keepResult != null) {
        keepResult.success(null);
        keepResult = null;
      }
    }
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    try {
      keepResult = result;

      switch (call.method) {
        case "start":
        {
          service.start();

          Log.i(TAG, "Service started");

          result.success(true);
          break;
        }
        case "stop":
        {
          service.stop();

          if(serviceConnected) {
            context.unbindService(connection);
            serviceConnected = false;
          }

          Log.i(TAG, "Service stoped");

          result.success(true);
          break;
        }
        case "isStarted":
        {
          result.success(service.getStatus());
          break;
        }
        case "getToday":
        {
          result.success(service.getDb().getData(System.currentTimeMillis()).toString());
          break;
        }
        case "getByDate":
        {
          String dateStr = ((String) call.argument("date")).replace( "/" , "-" );

          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
          Date date = sdf.parse(dateStr);
          long millis = date.getTime();

          result.success(service.getDb().getData(millis));
          break;
        }
        case "getAll":
        {
          result.success((new JSONArray(service.getDb().getData())).toString());
          break;
        }
        case "clear":
        {
          result.success(service.getDb().clearData());
          break;
        }
        default:
          result.notImplemented();
          break;
      }
    } catch (Exception e) {
      result.error(null, e.getMessage(), null);
    }
  }
}

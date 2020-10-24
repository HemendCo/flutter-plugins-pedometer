package com.hemend.flutter.plugins.pedometer.libs;

import org.json.JSONException;

/**
 * Listens for alerts about steps being detected.
 */
public interface StepCallback {
    /**
     * Called when a step has been detected.  Given the time in nanoseconds at
     * which the step was detected.
     */
    public void step(String date, int steps) throws JSONException;
}

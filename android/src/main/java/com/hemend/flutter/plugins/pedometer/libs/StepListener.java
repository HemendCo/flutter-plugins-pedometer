package com.hemend.flutter.plugins.pedometer.libs;

/**
 * Listens for alerts about steps being detected.
 */
public interface StepListener {
    /**
     * Called when a step has been detected.  Given the time in nanoseconds at
     * which the step was detected.
     */
    public void step(int steps);
    public void step(long timeNs);
}

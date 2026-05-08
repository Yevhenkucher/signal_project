package com.alerts;

public class BloodPressureStrategy implements AlertStrategy{
    @Override
    public boolean checkAlert(double value) {
        return value > 140;
    }
}

package com.alerts;

public class HeartRateStrategy implements AlertStrategy{
    @Override
    public boolean checkAlert(double value) {
        return value < 60 || value > 100;
    }
}

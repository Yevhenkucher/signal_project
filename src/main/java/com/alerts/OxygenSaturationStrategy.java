package com.alerts;

public class OxygenSaturationStrategy implements AlertStrategy{
    @Override
    public boolean checkAlert(double value) {
        return value < 90;
    }
}

package com.alerts;

public class PatientMonitor {
    private AlertStrategy strategy;

    public void setStrategy(AlertStrategy strategy){
        this.strategy=strategy;
    }
    public void monitor(double value){
        if(strategy.checkAlert(value)){
            System.out.println("ALERT TRIGGERED!");
        } else {
            System.out.println("Reading is normal.");
        }
    }
}

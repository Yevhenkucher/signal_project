package com.alerts.decorators;

import com.alerts.Alert;

/**
 * Base decorator for Alert objects
 */
public abstract class AlertDecorator extends Alert {

    protected Alert decoratedAlert;

    /**
     * Constructs an AlertDecorator wrapping the given alert.
     *
     * @param decoratedAlert the alert to decorate
     */
    public AlertDecorator(Alert decoratedAlert) {
        super(decoratedAlert.getPatientId(), decoratedAlert.getCondition(), decoratedAlert.getTimestamp());
        this.decoratedAlert = decoratedAlert;
    }

    /** 
     * Gets the condition from the wrapped alert.
     * 
     * @return the condition from the wrapped alert */
    @Override
    public String getCondition() {
        return decoratedAlert.getCondition();
    }
}

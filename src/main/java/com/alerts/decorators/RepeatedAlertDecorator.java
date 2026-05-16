package com.alerts.decorators;

import com.alerts.Alert;

/**
 * Decorator that marks an alert as a repeated alert.
 */
public class RepeatedAlertDecorator extends AlertDecorator {

    private int repeatCount;

    /**
     * Constructs a RepeatedAlertDecorator.
     *
     * @param decoratedAlert the alert to decorate
     * @param repeatCount    how many times the alert has repeated
     */
    public RepeatedAlertDecorator(Alert decoratedAlert, int repeatCount) {
        super(decoratedAlert);
        this.repeatCount = repeatCount;
    }

    /**
     * Returns the condition with a repeated alert tag.
     *
     * @return the condition string with repeat info
     */
    @Override
    public String getCondition() {
        return "[REPEATED x" + repeatCount + "] " + decoratedAlert.getCondition();
    }
}

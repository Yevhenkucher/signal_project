package com.alerts.decorators;

import com.alerts.Alert;

/**
 * Decorator that marks an alert as high priority.
 */
public class PriorityAlertDecorator extends AlertDecorator {

    /**
     * Constructs a PriorityAlertDecorator.
     *
     * @param decoratedAlert the alert to decorate
     */
    public PriorityAlertDecorator(Alert decoratedAlert) {
        super(decoratedAlert);
    }

    /**
     * Returns the condition with a priority tag.
     *
     * @return the condition string with priority prefix
     */
    @Override
    public String getCondition() {
        return "[HIGH PRIORITY] " + decoratedAlert.getCondition();
    }
}

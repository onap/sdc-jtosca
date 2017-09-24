package org.openecomp.sdc.toscaparser.api.utils;

import org.openecomp.sdc.toscaparser.api.common.ValidationIssueCollector;

public class ThreadLocalsHolder {

    private static final ThreadLocal<ValidationIssueCollector> exceptionCollectorThreadLocal = new ThreadLocal<>();

    private ThreadLocalsHolder(){}

    public static ValidationIssueCollector getCollector() {
        return exceptionCollectorThreadLocal.get();
    }

    public static void setCollector(ValidationIssueCollector validationIssueCollector) {
        cleanup();
        exceptionCollectorThreadLocal.set(validationIssueCollector);
    }

    public static void cleanup(){
        exceptionCollectorThreadLocal.remove();
    }

}

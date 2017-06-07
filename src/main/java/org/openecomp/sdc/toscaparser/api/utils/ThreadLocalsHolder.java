package org.openecomp.sdc.toscaparser.api.utils;

import org.openecomp.sdc.toscaparser.api.common.ExceptionCollector;

public class ThreadLocalsHolder {

    private static final ThreadLocal<ExceptionCollector> exceptionCollectorThreadLocal = new ThreadLocal<>();

    private ThreadLocalsHolder(){}

    public static ExceptionCollector getCollector() {
        return exceptionCollectorThreadLocal.get();
    }

    public static void setCollector(ExceptionCollector exceptionCollector) {
        cleanup();
        exceptionCollectorThreadLocal.set(exceptionCollector);
    }

    public static void cleanup(){
        exceptionCollectorThreadLocal.remove();
    }

}

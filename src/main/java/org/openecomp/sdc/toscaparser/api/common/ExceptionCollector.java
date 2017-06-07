package org.openecomp.sdc.toscaparser.api.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Perfectly good enough... 

public class ExceptionCollector {

    private static Logger log = LoggerFactory.getLogger(ExceptionCollector.class.getName());

    private Map<String, String> notAnalyzedExceptions = new HashMap<>();
    private Map<String, String> criticalExceptions = new HashMap<>();
    private Map<String, String> warningExceptions = new HashMap<>();

    private boolean bWantTrace = true;
    private String filePath;

    public enum ReportType {WARNING, CRITICAL, NOT_ANALYZED}

    public ExceptionCollector(String filePath) {
        this.filePath = filePath;
    }

    public void appendException(String exception) {

        addException(exception, ReportType.NOT_ANALYZED);
    }

    public void appendCriticalException(String exception) {

        addException(exception, ReportType.CRITICAL);
    }

    public void appendWarning(String exception) {

        addException(exception, ReportType.WARNING);
    }

    private void addException(String exception, ReportType type) {

        Map<String, String> exceptions = getExceptionCollection(type);

        if (!exceptions.containsKey(exception)) {
            // get stack trace
            StackTraceElement[] ste = Thread.currentThread().getStackTrace();
            StringBuilder sb = new StringBuilder();
            // skip the last 2 (getStackTrace and this)
            for (int i = 2; i < ste.length; i++) {
                sb.append(String.format("  %s(%s:%d)%s", ste[i].getClassName(), ste[i].getFileName(),
                        ste[i].getLineNumber(), i == ste.length - 1 ? " " : "\n"));
            }
            exceptions.put(exception, sb.toString());
        }
    }

    public List<String> getCriticalsReport() {

        return getReport(ReportType.CRITICAL);
    }

    public List<String> getNotAnalyzedExceptionsReport() {

        return getReport(ReportType.NOT_ANALYZED);
    }

    public List<String> getWarningsReport() {

        return getReport(ReportType.WARNING);
    }

    private List<String> getReport(ReportType type) {
        Map<String, String> collectedExceptions = getExceptionCollection(type);

        List<String> report = new ArrayList<>();
        if (collectedExceptions.size() > 0) {
            for (Map.Entry<String, String> exception : collectedExceptions.entrySet()) {
                report.add(exception.getKey());
                if (bWantTrace) {
                    report.add(exception.getValue());
                }
            }
        }

        return report;
    }

    private Map<String, String> getExceptionCollection(ReportType type) {
        switch (type) {
            case WARNING:
                return warningExceptions;
            case CRITICAL:
                return criticalExceptions;
            case NOT_ANALYZED:
                return notAnalyzedExceptions;
            default:
                return notAnalyzedExceptions;
        }
    }

    public int errorsNotAnalyzedCaught() {
        return notAnalyzedExceptions.size();
    }

    public int criticalsCaught() {
        return criticalExceptions.size();
    }

    public int warningsCaught() {
        return warningExceptions.size();
    }

    public void setWantTrace(boolean b) {
        bWantTrace = b;
    }

}

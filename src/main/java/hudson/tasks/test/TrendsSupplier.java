package hudson.tasks.test;

import edu.hm.hafner.echarts.JacksonFacade;
import org.kohsuke.stapler.bind.JavaScriptMethod;

public class TrendsSupplier {
    private final boolean failureOnly;
    private final AbstractTestResultAction<?> latestTestResult;

    public TrendsSupplier(boolean failureOnly, AbstractTestResultAction<?> latestTestResult) {
        this.failureOnly = failureOnly;
        this.latestTestResult = latestTestResult;
    }


    @JavaScriptMethod
    public String getBuildTrendModel() {
        return (new JacksonFacade()).toJson(latestTestResult.createChartModel(failureOnly));
    }

    public boolean isTrendVisible(){
        return latestTestResult.getPreviousResult() != null;
    }

    public String getUrlName(){
        return "testReport";
    }
}

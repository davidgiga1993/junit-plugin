package hudson.tasks.junit.charts;

import edu.hm.hafner.echarts.SeriesBuilder;
import hudson.tasks.test.AbstractTestResultAction;

import java.util.HashMap;
import java.util.Map;

public class ResultsSeriesBuilder extends SeriesBuilder<AbstractTestResultAction<?>> {

    static final String PASSED = "passed";
    static final String FAILED = "failed";
    static final String SKIPPED = "skipped";

    private final boolean failureOnly;

    public ResultsSeriesBuilder(boolean failureOnly) {
        this.failureOnly = failureOnly;
    }

    @Override
    protected Map<String, Integer> computeSeries(AbstractTestResultAction<?> result) {
        Map<String, Integer> series = new HashMap<>();

        int failed = result.getFailCount();
        series.put(FAILED, failed);
        if(failureOnly) {
            return series;
        }

        int skipped = result.getSkipCount();
        series.put(SKIPPED, skipped);
        series.put(PASSED, result.getTotalCount()- failed-skipped);
        return series;
    }
}

package hudson.tasks.test.charts;

import edu.hm.hafner.echarts.SeriesBuilder;
import hudson.tasks.test.TestResult;

import java.util.HashMap;
import java.util.Map;

import static hudson.tasks.test.charts.ResultsSeriesBuilder.*;

public class CaseHistorySeriesBuilder extends SeriesBuilder<TestResult> {

    private final boolean showDuration;

    public CaseHistorySeriesBuilder(boolean showDuration) {
        this.showDuration = showDuration;
    }

    @Override
    protected Map<String, Integer> computeSeries(TestResult result) {
        Map<String, Integer> series = new HashMap<>();

        int failed = result.getFailCount();
        int skipped = result.getSkipCount();
        int passed = result.getPassCount();

        int failedVal = failed;
        int skippedVal = skipped;
        int passedVal = passed;

        if (showDuration) {
            int value = Math.round(result.getDuration() * 1000);

            if (failed > 0) {
                failedVal = value;
                skippedVal = 0;
                passedVal = 0;
            } else if (skipped > 0) {
                failedVal = 0;
                skippedVal = value;
                passedVal = 0;
            } else {
                failedVal = 0;
                skippedVal = 0;
                passedVal = value;
            }
        }

        // Fallback to count
        series.put(FAILED, failedVal);
        series.put(SKIPPED, skippedVal);
        series.put(PASSED, passedVal);
        return series;
    }
}

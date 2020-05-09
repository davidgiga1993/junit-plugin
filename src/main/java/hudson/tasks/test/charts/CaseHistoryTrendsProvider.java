package hudson.tasks.test.charts;

import edu.hm.hafner.echarts.Build;
import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.LinesChartModel;
import hudson.model.Job;
import hudson.model.Run;
import hudson.tasks.junit.Helper;
import hudson.tasks.test.TestObject;
import hudson.tasks.test.TestResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CaseHistoryTrendsProvider extends AbstractTrendsProvider {

    private final Job<?, ?> job;
    private final TestObject testObject;


    private final int min;
    private final int max;
    private final boolean showDuration;

    public CaseHistoryTrendsProvider(TestObject testObject, int min, int max, boolean showDuration) {
        this.job = testObject.getRun().getParent();
        this.testObject = testObject;
        this.min = min;
        this.max = max;
        this.showDuration = showDuration;
    }

    @Override
    public boolean isTrendVisible() {
        Iterator<?> iterable = job.getBuilds().iterator();
        for (int X = 0; iterable.hasNext(); X++) {
            iterable.next();
            if (X >= 1)
                return true;
        }
        return false;
    }

    @Override
    protected LinesChartModel createChartModel() {
        CaseHistoryTrendChart trendChart = new CaseHistoryTrendChart(showDuration);
        List<BuildResult<TestResult>> results = getList(min, max);
        return trendChart.create(results, new ChartModelConfiguration(ChartModelConfiguration.AxisType.BUILD));
    }

    @Override
    public String getUrlName() {
        String buildLink = job.getUrl();
        String actionUrl = testObject.getTestResultAction().getUrlName();
        final String rootUrl = Helper.getActiveInstance().getRootUrl();
        if (rootUrl == null) {
            throw new IllegalStateException("Jenkins root URL not available");
        }
        return rootUrl + buildLink + actionUrl + job.getUrl();
    }

    private List<BuildResult<TestResult>> getList(int start, int end) {
        List<BuildResult<TestResult>> results = new ArrayList<>();

        Iterator<Run<?, ?>> itr = (Iterator<Run<?, ?>>) job.getBuilds().iterator();
        hudson.util.Iterators.skip(itr, start);

        int delta = end - start;
        while (delta > 0 && itr.hasNext()) {
            Run<?, ?> run = itr.next();
            if (run.isBuilding())
                continue;

            TestResult result = testObject.getResultInRun(run);
            if (result == null)
                continue;

            results.add(new BuildResult<>(new Build(run.getNumber()), result));
        }

        return results;
    }
}

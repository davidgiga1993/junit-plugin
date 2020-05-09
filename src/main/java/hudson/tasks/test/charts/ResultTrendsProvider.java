package hudson.tasks.test.charts;

import edu.hm.hafner.echarts.Build;
import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.LinesChartModel;
import hudson.tasks.test.AbstractTestResultAction;

import java.util.ArrayList;
import java.util.List;

public class ResultTrendsProvider extends AbstractTrendsProvider {
    private final boolean failureOnly;
    private final AbstractTestResultAction<?> latestTestResult;

    public ResultTrendsProvider(boolean failureOnly, AbstractTestResultAction<?> latestTestResult) {
        this.failureOnly = failureOnly;
        this.latestTestResult = latestTestResult;
    }

    @Override
    public boolean isTrendVisible() {
        return latestTestResult.getPreviousResult() != null;
    }

    @Override
    protected LinesChartModel createChartModel() {
        int cap = Integer.getInteger(AbstractTestResultAction.class.getName() + ".test.trend.max", Integer.MAX_VALUE);
        int count = 0;
        List<BuildResult<AbstractTestResultAction<?>>> results = new ArrayList<>();

        for (AbstractTestResultAction<?> a = latestTestResult; a != null; a = a.getPreviousResult(AbstractTestResultAction.class, false)) {
            if (++count > cap) {
                break;
            }

            Build build = new Build(a.run.number);
            results.add(new BuildResult<>(build, a));
        }

        ResultTrendChart trendChart = new ResultTrendChart(failureOnly);
        return trendChart.create(results, new ChartModelConfiguration(ChartModelConfiguration.AxisType.BUILD));
    }

    @Override
    public String getUrlName() {
        return "testReport";
    }
}

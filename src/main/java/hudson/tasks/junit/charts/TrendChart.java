package hudson.tasks.junit.charts;

import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.LinesChartModel;
import hudson.tasks.test.AbstractTestResultAction;

/**
 * Creates a model for a trend chart of a given number of static analysis build results.
 */
public interface TrendChart {
    /**
     * Creates the chart for the specified results.
     *
     * @param results       the analysis results to render
     * @param configuration the chart configuration to be used
     * @return the chart model
     */
    LinesChartModel create(Iterable<? extends BuildResult<AbstractTestResultAction<?>>> results,
                           ChartModelConfiguration configuration);
}
package hudson.tasks.junit.charts;


import edu.hm.hafner.echarts.*;
import edu.hm.hafner.echarts.LineSeries.FilledMode;
import edu.hm.hafner.echarts.LineSeries.StackedMode;
import hudson.tasks.test.AbstractTestResultAction;

/**
 * Builds the model for a trend chart showing all issues for a given number of builds. The issues are colored according
 * to the specified health report.
 *
 * @author Ullrich Hafner
 */
public class ResultTrendChart implements TrendChart {

    private final boolean failedOnly;

    public ResultTrendChart(boolean failedOnly) {
        this.failedOnly = failedOnly;
    }

    @Override
    public LinesChartModel create(final Iterable<? extends BuildResult<AbstractTestResultAction<?>>> results,
                                  final ChartModelConfiguration configuration) {
        
        ResultsSeriesBuilder builder = new ResultsSeriesBuilder(false);
        LinesDataSet dataSet = builder.createDataSet(configuration, results);


        LinesChartModel model = new LinesChartModel();
        model.setBuildNumbers(dataSet.getBuildNumbers());
        model.setDomainAxisLabels(dataSet.getDomainAxisLabels());

        LineSeries unhealthy = createSeries("Failed", Palette.RED);
        unhealthy.addAll(dataSet.getSeries(ResultsSeriesBuilder.FAILED));

        if (failedOnly) {
            model.addSeries(unhealthy);
            return model;
        }

        LineSeries healthy = createSeries("Passed", Palette.GREEN);
        healthy.addAll(dataSet.getSeries(ResultsSeriesBuilder.PASSED));

        LineSeries intermediate = createSeries("Skipped", Palette.YELLOW);
        intermediate.addAll(dataSet.getSeries(ResultsSeriesBuilder.SKIPPED));

        model.addSeries(healthy, intermediate, unhealthy);
        return model;
    }

    private LineSeries createSeries(final String name, final Palette color) {
        return new LineSeries(name, color.getNormal(), StackedMode.SEPARATE_LINES, FilledMode.FILLED);
    }
}
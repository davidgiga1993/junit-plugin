package hudson.tasks.test.charts;


import edu.hm.hafner.echarts.*;
import edu.hm.hafner.echarts.LineSeries.FilledMode;
import edu.hm.hafner.echarts.LineSeries.StackedMode;
import hudson.tasks.test.TestResult;

import java.util.List;

/**
 * Builds the model for a trend chart showing all issues for a given number of builds. The issues are colored according
 * to the specified health report.
 */
public class CaseHistoryTrendChart {

    private final boolean showDuration;

    public CaseHistoryTrendChart(boolean showDuration) {
        this.showDuration = showDuration;
    }

    public LinesChartModel create(List<BuildResult<TestResult>> results,
                                  final ChartModelConfiguration configuration) {

        CaseHistorySeriesBuilder builder = new CaseHistorySeriesBuilder(showDuration);
        LinesDataSet dataSet = builder.createDataSet(configuration, results);


        LinesChartModel model = new LinesChartModel();
        model.setBuildNumbers(dataSet.getBuildNumbers());
        model.setDomainAxisLabels(dataSet.getDomainAxisLabels());

        LineSeries unhealthy = createSeries("Failed", Palette.RED);
        unhealthy.addAll(dataSet.getSeries(ResultsSeriesBuilder.FAILED));

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
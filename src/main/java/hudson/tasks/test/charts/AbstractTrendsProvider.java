package hudson.tasks.test.charts;

import edu.hm.hafner.echarts.JacksonFacade;
import edu.hm.hafner.echarts.LinesChartModel;
import org.kohsuke.stapler.bind.JavaScriptMethod;

/**
 * Provides data for trends diagrams
 */
public abstract class AbstractTrendsProvider {

    /**
     * Gets called by jelly of {@code trend-chart} to indicate if the trend is visible
     *
     * @return True if visible
     */
    @SuppressWarnings("unused")
    public abstract boolean isTrendVisible();

    @SuppressWarnings("unused")
    @JavaScriptMethod
    public String getBuildTrendModel()
    {
        return (new JacksonFacade()).toJson(createChartModel());
    }

    /**
     * Creates the chart data that should be shown
     *
     * @return Model
     */
    protected abstract LinesChartModel createChartModel();

    /**
     * Gets called by jelly of {@code trend-chart} for defining the diagram click target
     *
     * @return Url name
     */
    @SuppressWarnings("unused")
    public abstract String getUrlName();
}

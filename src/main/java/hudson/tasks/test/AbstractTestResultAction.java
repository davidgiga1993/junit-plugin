/*
 * The MIT License
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi, Daniel Dyer, Red Hat, Inc., Stephen Connolly, id:cactusman, Yahoo!, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.tasks.test;

import hudson.Extension;
import hudson.Functions;
import hudson.model.*;
import jenkins.model.RunAction2;
import jenkins.model.lazy.LazyBuildMixIn;
import org.jvnet.localizer.Localizable;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Common base class for recording test result.
 *
 * <p>
 * {@link Project} and {@link Build} recognizes {@link Action}s that derive from this,
 * and displays it nicely (regardless of the underlying implementation.)
 *
 * @author Kohsuke Kawaguchi
 */
@ExportedBean
public abstract class AbstractTestResultAction<T extends AbstractTestResultAction> implements HealthReportingAction, RunAction2 {

    /**
     * @since 1.2-beta-1
     */
    public transient Run<?, ?> run;
    @Deprecated
    public transient AbstractBuild<?, ?> owner;

    private Map<String, String> descriptions = new ConcurrentHashMap<String, String>();

    /**
     * @since 1.545
     */
    protected AbstractTestResultAction() {
    }

    /**
     * @since 1.2-beta-1
     * @deprecated Use the default constructor and just call {@link Run#addAction} to associate the build with the action.
     */
    @Deprecated
    protected AbstractTestResultAction(Run owner) {
        onAttached(owner);
    }

    @Deprecated
    protected AbstractTestResultAction(AbstractBuild owner) {
        this((Run) owner);
    }

    @Override
    public void onAttached(Run<?, ?> r) {
        this.run = r;
        this.owner = r instanceof AbstractBuild ? (AbstractBuild<?, ?>) r : null;
    }

    @Override
    public void onLoad(Run<?, ?> r) {
        this.run = r;
        this.owner = r instanceof AbstractBuild ? (AbstractBuild<?, ?>) r : null;
    }

    /**
     * Gets the number of failed tests.
     */
    @Exported(visibility = 2)
    public abstract int getFailCount();

    /**
     * Gets the number of skipped tests.
     */
    @Exported(visibility = 2)
    public int getSkipCount() {
        // Not all sub-classes will understand the concept of skipped tests.
        // This default implementation is for them, so that they don't have
        // to implement it (this avoids breaking existing plug-ins - i.e. those
        // written before this method was added in 1.178).
        // Sub-classes that do support skipped tests should over-ride this method.
        return 0;
    }

    /**
     * Gets the total number of tests.
     */
    @Exported(visibility = 2)
    public abstract int getTotalCount();

    /**
     * Gets the diff string of failures.
     */
    public final String getFailureDiffString() {
        T prev = getPreviousResult();
        if (prev == null) return "";  // no record

        return " / " + Functions.getDiffString(this.getFailCount() - prev.getFailCount());
    }

    public String getDisplayName() {
        return Messages.AbstractTestResultAction_getDisplayName();
    }

    @Exported(visibility = 2)
    public String getUrlName() {
        return "testReport";
    }

    public String getIconFileName() {
        return "clipboard.png";
    }

    public HealthReport getBuildHealth() {
        final double scaleFactor = getHealthScaleFactor();
        if (scaleFactor < 1e-7) {
            return null;
        }
        final int totalCount = getTotalCount();
        final int failCount = getFailCount();
        int score = (totalCount == 0)
                ? 100
                : (int) (100.0 * Math.max(0.0, Math.min(1.0, 1.0 - (scaleFactor * failCount) / totalCount)));
        Localizable description, displayName = Messages._AbstractTestResultAction_getDisplayName();
        if (totalCount == 0) {
            description = Messages._AbstractTestResultAction_zeroTestDescription(displayName);
        } else {
            description = Messages._AbstractTestResultAction_TestsDescription(displayName, failCount, totalCount);
        }
        return new HealthReport(score, description);
    }

    /**
     * Returns how much to scale the test related health by.
     *
     * @return a factor of {@code 1.0} to have the test health be the percentage of tests passing so 20% of tests
     * failing will report as 80% health. A factor of {@code 2.0} will mean that 20% of tests failing will report as 60%
     * health. A factor of {@code 2.5} will mean that 20% of test failing will report as 50% health. A factor of
     * {@code 4.0} will mean that 20% of tests failing will report as 20% health. A factor of {@code 5.0} will mean
     * that 20% (or more) of tests failing will report as 0% health. A factor of {@code 0.0} will disable test health
     * reporting.
     */
    public double getHealthScaleFactor() {
        return 1.0;
    }

    /**
     * Exposes this object to the remote API.
     */
    public Api getApi() {
        return new Api(this);
    }

    /**
     * Returns the object that represents the actual test result.
     * This method is used by the remote API so that the XML/JSON
     * that we are sending won't contain unnecessary indirection
     * (that is, {@link AbstractTestResultAction} in between.
     *
     * <p>
     * If such a concept doesn't make sense for a particular subtype,
     * return <code>this</code>.
     */
    public abstract Object getResult();

    /**
     * Gets the test result of the previous build, if it's recorded, or null.
     */
    public T getPreviousResult() {
        return (T) getPreviousResult(getClass(), true);
    }

    public <U extends AbstractTestResultAction> U getPreviousResult(Class<U> type, boolean eager) {
        Run<?, ?> b = run;
        Set<Integer> loadedBuilds;
        if (!eager && run.getParent() instanceof LazyBuildMixIn.LazyLoadingJob) {
            loadedBuilds = ((LazyBuildMixIn.LazyLoadingJob<?, ?>) run.getParent()).getLazyBuildMixIn()._getRuns().getLoadedBuilds().keySet();
        } else {
            loadedBuilds = null;
        }
        while (true) {
            b = loadedBuilds == null || loadedBuilds.contains(b.number - /* assuming there are no gaps */1) ? b.getPreviousBuild() : null;
            if (b == null)
                return null;
            U r = b.getAction(type);
            if (r != null) {
                if (r == this) {
                    throw new IllegalStateException(this + " was attached to both " + b + " and " + run);
                }
                if (r.run.number != b.number) {
                    throw new IllegalStateException(r + " was attached to both " + b + " and " + r.run);
                }
                return r;
            }
        }
    }

    public TestResult findPreviousCorresponding(TestResult test) {
        T previousResult = getPreviousResult();
        if (previousResult != null) {
            TestResult testResult = (TestResult) getResult();
            return testResult.findCorrespondingResult(test.getId());
        }

        return null;
    }

    public TestResult findCorrespondingResult(String id) {
        final Object testResult = getResult();
        if (!(testResult instanceof TestResult)) {
            return null;
        }
        return ((TestResult) testResult).findCorrespondingResult(id);
    }

    /**
     * A shortcut for summary.jelly
     *
     * @return List of failed tests from associated test result.
     */
    public List<? extends TestResult> getFailedTests() {
        return Collections.emptyList();
    }

    /**
     * A shortcut for scripting
     *
     * @return List of passed tests from associated test result.
     * @since 1.10
     */
    @Nonnull
    public List<? extends TestResult> getPassedTests() {
        return Collections.emptyList();
    }

    /**
     * A shortcut for scripting
     *
     * @return List of skipped tests from associated test result.
     * @since 1.10
     */
    @Nonnull
    public List<? extends TestResult> getSkippedTests() {
        return Collections.emptyList();
    }

    /**
     * Returns a full path down to a test result
     */
    public String getTestResultPath(TestResult it) {
        return getUrlName() + "/" + it.getRelativePathFrom(null);
    }

    /**
     * {@link TestObject}s do not have their own persistence mechanism, so updatable data of {@link TestObject}s
     * need to be persisted by the owning {@link AbstractTestResultAction}, and this method and
     * {@link #setDescription(TestObject, String)} provides that logic.
     *
     * <p>
     * The default implementation stores information in the 'this' object.
     *
     * @see TestObject#getDescription()
     */
    protected String getDescription(TestObject object) {
        return descriptions.get(object.getId());
    }

    protected void setDescription(TestObject object, String description) {
        descriptions.put(object.getId(), description);
    }

    public Object readResolve() {
        if (descriptions == null) {
            descriptions = new ConcurrentHashMap<String, String>();
        }

        return this;
    }

    @Extension
    public static final class Summarizer extends Run.StatusSummarizer {
        @Override
        public Run.Summary summarize(Run<?, ?> run, ResultTrend trend) {
            AbstractTestResultAction<?> trN = run.getAction(AbstractTestResultAction.class);
            if (trN == null) {
                return null;
            }
            Boolean worseOverride;
            switch (trend) {
                case NOW_UNSTABLE:
                    worseOverride = false;
                    break;
                case UNSTABLE:
                    worseOverride = true;
                    break;
                case STILL_UNSTABLE:
                    worseOverride = null;
                    break;
                default:
                    return null;
            }
            Run prev = run.getPreviousBuild();
            AbstractTestResultAction<?> trP = prev == null ? null : prev.getAction(AbstractTestResultAction.class);
            if (trP == null) {
                if (trN.getFailCount() > 0) {
                    return new Run.Summary(worseOverride != null ? worseOverride : true, Messages.Run_Summary_TestFailures(trN.getFailCount()));
                }
            } else {
                if (trN.getFailCount() != 0) {
                    if (trP.getFailCount() == 0) {
                        return new Run.Summary(worseOverride != null ? worseOverride : true, Messages.Run_Summary_TestsStartedToFail(trN.getFailCount()));
                    }
                    if (trP.getFailCount() < trN.getFailCount()) {
                        return new Run.Summary(worseOverride != null ? worseOverride : true, Messages.Run_Summary_MoreTestsFailing(trN.getFailCount() - trP.getFailCount(), trN.getFailCount()));
                    }
                    if (trP.getFailCount() > trN.getFailCount()) {
                        return new Run.Summary(worseOverride != null ? worseOverride : false, Messages.Run_Summary_LessTestsFailing(trP.getFailCount() - trN.getFailCount(), trN.getFailCount()));
                    }

                    return new Run.Summary(worseOverride != null ? worseOverride : false, Messages.Run_Summary_TestsStillFailing(trN.getFailCount()));
                }
            }
            return null;
        }
    }

}

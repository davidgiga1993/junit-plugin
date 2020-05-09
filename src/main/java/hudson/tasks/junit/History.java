/*
 * The MIT License
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., Tom Huybrechts, Yahoo!, Inc., Seiji Sogabe
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
package hudson.tasks.junit;

import hudson.model.Run;
import hudson.tasks.test.TestObject;
import hudson.tasks.test.TestResult;
import hudson.tasks.test.charts.CaseHistoryTrendsProvider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * History of {@link hudson.tasks.test.TestObject} over time.
 *
 * @since 1.320
 */
public class History {
    private final TestObject testObject;

    public History(TestObject testObject) {
        this.testObject = testObject;
    }

    public TestObject getTestObject() {
        return testObject;
    }

    public boolean historyAvailable() {
        Iterator<?> iterable = testObject.getRun().getParent().getBuilds().iterator();
        for (int X = 0; iterable.hasNext(); X++) {
            iterable.next();
            if (X >= 1)
                return true;
        }
        return false;
    }

    /**
     * Returns a new supplier instance which provides the API required
     * for the trend charts
     *
     * @param min          Minimum build index
     * @param max          Maximum build index
     * @param showDuration True if the duration should be included instead of the test count
     * @return Supplier
     */
    @SuppressWarnings("unused")
    public CaseHistoryTrendsProvider getTrendsProvider(int min, int max, boolean showDuration) {
        return new CaseHistoryTrendsProvider(testObject, min, max, showDuration);
    }

    public List<TestResult> getList(int start, int end) {
        List<TestResult> list = new ArrayList<>();
        end = Math.min(end, testObject.getRun().getParent().getBuilds().size());
        for (Run<?, ?> b : testObject.getRun().getParent().getBuilds().subList(start, end)) {
            if (b.isBuilding()) continue;
            TestResult o = testObject.getResultInRun(b);
            if (o != null) {
                list.add(o);
            }
        }
        return list;
    }

    public List<TestResult> getList() {
        return getList(0, testObject.getRun().getParent().getBuilds().size());
    }

    @SuppressWarnings("unused")
    public static int asInt(String s, int defalutValue) {
        if (s == null) return defalutValue;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defalutValue;
        }
    }
}

package com.dukescript.junit.chartsui;

import java.io.Flushable;
import java.io.IOException;
import java.util.List;
import java.util.ServiceLoader;
import net.java.html.BrwsrCtx;
import net.java.html.charts.Chart;
import net.java.html.charts.ChartEvent;
import net.java.html.charts.ChartListener;
import net.java.html.charts.Color;
import net.java.html.charts.Config;
import net.java.html.charts.Values;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.openide.util.lookup.ServiceProvider;

/*
 * #%L
 * DukeScript JUnit Runner - a library from the DukeScript project.
 * Visit http://dukescript.com for support and commercial license.
 * %%
 * Copyright (C) 2015 - 2016 Dukehoff GmbH
 * %%
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * #L%
 */

/** Sample {@link RunListener}. It is registered via {@link ServiceProvider} so that
 * {@link ServiceLoader} can find it and use it when executing tests
 * via {@link RunWith}({BrowserRunner.class).
 */
@ServiceProvider(service = RunListener.class)
public final class JUnitChartsUI extends RunListener
implements ChartListener, Flushable {
    private boolean skip;
    private Chart<Values,Config> chart;
    private boolean stopCountDown;
    private BrwsrCtx ctx;

    public JUnitChartsUI() {
        // no op constructor is best
    }

    private Chart<Values, Config> getChart() {
        if (chart == null) {
            try {
                Chart<Values, Config> tmp = Chart.createLine(
                    new Values.Set("Running", Color.rgba(0, 128, 0, 0.3), Color.rgba(0, 255, 0, 0.9)),
                    new Values.Set("OK", Color.rgba(0, 0, 128, 0.3), Color.rgba(0, 0, 255, 0.9)),
                    new Values.Set("Fail", Color.rgba(128, 0, 0, 0.3), Color.rgba(255, 0, 0, 0.9))
                );
                tmp.applyTo("chart");
                tmp.addChartListener(this);
                chart = tmp;
            } catch (Exception e) {
                skip = true;
            }
            ctx = BrwsrCtx.findDefault(JUnitChartsUI.class);
        }
        return chart;
    }

    @Override
    public void testStarted(final Description description) throws Exception {
        if (skip) {
            return;
        }
        Values current = new Values(display(description), 1, 0, 0);
        getChart().getData().add(current);
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        if (skip) {
            return;
        }
        final Description description = failure.getDescription();
        Values current = new Values(display(description), 0, 0, 1);
        replace(description, current);
    }

    @Override
    public void testFinished(Description description) throws Exception {
        if (skip) {
            return;
        }
        Values current = new Values(display(description), 0, 0.5, 0);
        replace(description, current);
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
    }

    @Override
    public void flush() throws IOException {
        if (skip) {
            return;
        }

        class CountDownOnErrors implements Runnable {
            int index;
            int at = -1;

            @Override
            public void run() {
                final String msg = "Errors - Click to Stop";
                final List<Values> data = chart.getData();
                if (at == -1) {
                    Values errors = new Values(msg, 0, 10, 0);
                    at = data.size();
                    data.add(errors);
                    index = 10;
                } else {
                    if (stopCountDown) {
                        Values errors = new Values("Re-run tests by click", 0, 0, 0);
                        data.set(at, errors);
                    }
                    if (--index >= 0) {
                        Values errors = new Values(msg + "(" + index + "s)", 0, index, 0);
                        data.set(at, errors);
                    }
                }
            }
        }

        CountDownOnErrors run = new CountDownOnErrors();
        for (int i = 0;; i++) {
            synchronized (this) {
                try {
                    wait(1000);
                } catch (InterruptedException ex) {
                }
                if (stopCountDown) {
                    continue;
                }
                if (i > 13) {
                    break;
                }
            }
            ctx.execute(run);
        }
    }

    private void replace(Description description, Values values) {
        final List<Values> data = getChart().getData();
        for (int i = 0; i < data.size(); i++) {
            Values v = data.get(i);
            String label = v.getLabel();
            if (label.equals(display(description))) {
                data.set(i, values);
                return;
            }
        }
        data.add(values);
    }

    @Override
    public void chartClick(ChartEvent ce) {
        stopCountDown();
    }

    private synchronized void stopCountDown() {
        stopCountDown = true;
        notifyAll();
    }

    private String display(Description description) {
        return description.getMethodName();
    }
}

package com.dukescript.junit.chartsui;

import java.io.Flushable;
import java.io.IOException;
import java.util.List;
import net.java.html.BrwsrCtx;
import net.java.html.charts.Chart;
import net.java.html.charts.ChartEvent;
import net.java.html.charts.ChartListener;
import net.java.html.charts.Color;
import net.java.html.charts.Config;
import net.java.html.charts.Values;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.openide.util.lookup.ServiceProvider;

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
    public synchronized void chartClick(ChartEvent ce) {
        stopCountDown = true;
        notifyAll();
    }

    private String display(Description description) {
        String name = description.getMethodName();
        int rest = name.indexOf('[');
        return rest == -1 ? name : name.substring(0, rest);
    }
}

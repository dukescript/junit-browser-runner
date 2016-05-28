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
implements Runnable, ChartListener, Flushable {
    private boolean skip;
    private Chart<Values,Config> chart;
    private boolean animationComplete;
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
                tmp.getConfig().callback("onAnimationComplete", this);
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
        animationComplete = false;
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        if (skip) {
            return;
        }
        final Description description = failure.getDescription();
        Values current = new Values(display(description), 0, 0, 1);
        replace(description, current);
        animationComplete = false;
    }

    @Override
    public void testFinished(Description description) throws Exception {
        if (skip) {
            return;
        }
        Values current = new Values(display(description), 0, 0.5, 0);
        replace(description, current);
        animationComplete = false;
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
    }

    @Override
    public void flush() throws IOException {
        if (skip) {
            return;
        }

        final List<Values> data = chart.getData();
        final int at = data.size();
        final String msg = "Errors - Click to Stop";
        class CountDownOnErrors implements Runnable {
            int index = 10;

            @Override
            public void run() {
                if (index == 10) {
                    Values errors = new Values(msg, 0, 10, 0);
                    data.add(errors);
                    index = 9;
                    return;
                } else {
                    if (--index >= 0) {
                        Values errors = new Values(msg + "(" + index + "s)", 0, index, 0);
                        data.set(at, errors);
                    }
                }
            }
        }

        CountDownOnErrors run = new CountDownOnErrors();
        for (int i = 0; i >= 13; i++) {
            ctx.execute(run);
            synchronized (this) {
                try {
                    wait(1000);
                } catch (InterruptedException ex) {
                }
            }
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
    public synchronized void run() {
        animationComplete = true;
        notifyAll();
    }

    private synchronized void waitForAnimationImpl(int count) throws Exception {
        for (int i = 0; i < count * 10; i++) {
            if (animationComplete) {
                break;
            }
            wait(1000);
        }
    }

    @Override
    public synchronized void chartClick(ChartEvent ce) {
        animationComplete = true;
        notifyAll();
    }

    private String display(Description description) {
        String name = description.getMethodName();
        int rest = name.indexOf('[');
        return rest == -1 ? name : name.substring(0, rest);
    }
}

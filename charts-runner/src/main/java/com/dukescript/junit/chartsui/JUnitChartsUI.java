package com.dukescript.junit.chartsui;

import java.util.List;
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
public final class JUnitChartsUI extends RunListener implements Runnable, ChartListener {
    private boolean skip;
    private Chart<Values,Config> chart;
    private boolean animationComplete;

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
        if (skip) {
            return;
        }
        waitForAnimationImpl(result.getFailureCount());
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

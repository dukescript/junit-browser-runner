package net.java.html.junit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.MultipleFailureException;

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

final class MultiNotifier extends RunNotifier {
    private final RunNotifier notifier;

    private final Description description;
    private final Set<Description> remaining;

    private MultiNotifier(RunNotifier notifier, Description description) {
        this.notifier = notifier;
        this.description = description;
        this.remaining = new HashSet<>();
        this.remaining.add(null);
    }

    MultiNotifier(RunListener listener, Description description) {
        RunNotifier run = new RunNotifier();
        run.addListener(listener);
        this.notifier = run;
        this.description = description;
        this.remaining = new HashSet<>();
        this.remaining.add(null);
    }

    static MultiNotifier wrap(List<AbstractTestRunner> runners, RunNotifier notifier, Description description) {
        if (notifier instanceof MultiNotifier) {
            MultiNotifier prev = (MultiNotifier) notifier;
            return prev;
        }
        for (AbstractTestRunner r : runners) {
            notifier.addListener(r.listener());
        }
        return new MultiNotifier(notifier, description);
    }

    public void addFailure(Throwable targetException) {
        if (targetException instanceof MultipleFailureException) {
            addMultipleFailureException((MultipleFailureException) targetException);
        } else {
            notifier.fireTestFailure(new Failure(description, targetException));
        }
    }

    private void addMultipleFailureException(MultipleFailureException mfe) {
        for (Throwable each : mfe.getFailures()) {
            addFailure(each);
        }
    }

    public void addFailedAssumption(AssumptionViolatedException e) {
        notifier.fireTestAssumptionFailed(new Failure(description, e));
        finishTest(description);
    }

    @Override
    public void fireTestStarted(Description descr) {
        registerTest(descr);
        notifier.fireTestStarted(descr);
    }

    @Override
    public void fireTestFinished(Description descr) {
        if (remaining.contains(descr)) {
            notifier.fireTestFinished(descr);
        }
        finishTest(descr);
    }

    @Override
    public void fireTestIgnored(Description descr) {
        notifier.fireTestIgnored(descr);
        finishTest(descr);
    }

    @Override
    public void fireTestAssumptionFailed(Failure failure) {
        notifier.fireTestAssumptionFailed(failure);
        finishTest(description);
    }

    @Override
    public void fireTestFailure(Failure failure) {
        notifier.fireTestFailure(failure);
        finishTest(failure.getDescription());
    }

    @Override
    public void fireTestRunFinished(Result result) {
        notifier.fireTestRunFinished(result);
    }

    @Override
    public void fireTestRunStarted(Description description) {
        notifier.fireTestRunStarted(description);
        registerTest(description);
    }


    private synchronized void registerTest(Description descr) {
        remaining.add(descr);
    }

    synchronized void waitForAll() throws InterruptedException {
        while (!remaining.isEmpty()) {
            wait();
        }
    }

   private synchronized void finishTest(Description descr) {
        remaining.remove(null);
        remaining.remove(descr);
        if (remaining.isEmpty()) {
            notifyAll();
        }
    }
}

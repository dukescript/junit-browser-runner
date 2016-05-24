package net.java.html.junit;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

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

final class InvokeNowListener extends RunListener {
    private final AbstractTestRunner schedule;
    private final RunListener delegate;

    InvokeNowListener(AbstractTestRunner schedule) {
        this.delegate = UIListener.create().getListener();
        this.schedule = schedule;
    }

    @Override
    public void testIgnored(final Description description) throws Exception {
        fire(1, description);
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        fire(2, failure);
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        fire(3, failure);
    }

    @Override
    public void testFinished(Description description) throws Exception {
        fire(4, description);
    }

    @Override
    public void testStarted(Description description) throws Exception {
        fire(5, description);
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        fire(6, result);
    }

    @Override
    public void testRunStarted(Description description) throws Exception {
        fire(7, description);
    }

    private void fire(final int type, final Object parameter) {
        schedule.invokeNow(new Runnable() {
            @Override
            public void run() {
                try {
                    switch (type) {
                        case 1:
                            delegate.testIgnored((Description) parameter);
                            break;
                        case 2:
                            delegate.testAssumptionFailure((Failure) parameter);
                            break;
                        case 3:
                            delegate.testFailure((Failure) parameter);
                            break;
                        case 4:
                            delegate.testFinished((Description) parameter);
                            break;
                        case 5:
                            delegate.testStarted((Description) parameter);
                            break;
                        case 6:
                            delegate.testRunFinished((Result) parameter);
                            break;
                        case 7:
                            delegate.testRunStarted((Description) parameter);
                            break;
                        default:
                            throw new IllegalStateException("cannot dispatch type: " + type);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}

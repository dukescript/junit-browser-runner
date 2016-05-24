package net.java.html.junit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

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

final class SingleBrowserRunner extends BlockJUnit4ClassRunner {
    private final AbstractTestRunner schedule;
    private final String browser;
    private final String html;
    SingleBrowserRunner(String browser, AbstractTestRunner run, Class<?> klass) throws InitializationError {
        super(klass);
        this.browser = browser;
        this.schedule = run;
        HTMLContent content = klass.getAnnotation(HTMLContent.class);
        if (content != null) {
            this.html = content.value();
        } else {
            this.html = null;
        }
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        List<FrameworkMethod> methods = super.computeTestMethods();
        List<FrameworkMethod> clone = new ArrayList<>(methods.size());
        for (int i = 0; i < methods.size(); i++) {
            FrameworkMethod m = methods.get(i);
            clone.add(new InBrowserMethod(m.getMethod()));
        }
        return clone;
    }

    @Override
    public void run(final RunNotifier notifier) {
        MultiNotifier testNotifier = MultiNotifier.wrap(
            Collections.nCopies(1, schedule),
            notifier,
            getDescription()
        );
        try {
            for (FrameworkMethod frameworkMethod : getChildren()) {
                runChild(frameworkMethod, notifier);
            }
        } catch (AssumptionViolatedException e) {
            testNotifier.addFailedAssumption(e);
        } catch (StoppedByUserException e) {
            throw e;
        } catch (Throwable e) {
            testNotifier.addFailure(e);
        }
    }

    @Override
    protected void runChild(FrameworkMethod m, RunNotifier notifier) {
        InBrowserMethod method = (InBrowserMethod) m;
        Description description = describeChild(method);
        if (isIgnored(method)) {
            notifier.fireTestIgnored(description);
        } else {
            try {
                Object test = createTest(method);
                Statement before = withBefores(method, test, EmptyStatement.EMPTY);
                Statement after = withAfters(method, test, EmptyStatement.EMPTY);
                method.invokeInSteps(notifier, description, before, test, new Object[0], after);
            } catch (Throwable ex) {
                notifier.fireTestFailure(new Failure(description, ex));
            }
        }
    }
    @Override
    protected void validateTestMethods(List<Throwable> errors) {
    }

    private class InBrowserMethod extends FrameworkMethod {
        public InBrowserMethod(Method method) {
            super(method);
        }

        @Override
        public String getName() {
            return super.getName() + '[' + browser + ']';
        }

        final Object explosive(Object target, Object[] params) throws Throwable {
            if (html != null) {
                AbstractTestRunner.exposeHTML(html);
            }
            return getMethod().invoke(target, params);
        }

        void invokeInSteps(
            final RunNotifier notifier,
            final Description description,
            final Statement before,
            final Object target,
            final Object[] params,
            final Statement after
        ) {
            notifier.fireTestStarted(description);
            if (before != null) {
                schedule.invokeNow(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            before.evaluate();
                        } catch (Throwable ex) {
                            notifier.fireTestFailure(new Failure(description, ex));
                        }
                    }
                });
            }


            class DelayedAndAfter implements Runnable {
                private final List<Runnable> delayed;
                private int at;
                private Statement atEnd = after;
                DelayedAndAfter(List<Runnable> delayed) {
                    this.delayed = delayed;
                    this.at = 0;
                }

                @Override
                public void run() {
                    if (at < delayed.size()) {
                        try {
                            delayed.get(at++).run();
                        } catch (Throwable t) {
                            at = delayed.size();
                            notifier.fireTestFailure(new Failure(description, t));
                        } finally {
                            schedule.invokeLater(this);
                        }
                        return;
                    }
                    if (atEnd != null) {
                        schedule.invokeLater(this);
                        try {
                            atEnd.evaluate();
                        } catch (Throwable t) {
                            notifier.fireTestAssumptionFailed(new Failure(description, t));
                        } finally {
                            atEnd = null;
                        }
                        return;
                    }
                    notifier.fireTestFinished(description);
                }
            }

            final Object[] ex = { null };
            schedule.invokeLater(new Runnable() {
                @Override
                public void run() {
                    final List<Runnable> delayed = new ArrayList<>();
                    try {
                        Object ret = explosive(target, params);
                        if (ret instanceof Runnable) {
                            delayed.add((Runnable) ret);
                        }
                        if (ret instanceof Runnable[]) {
                            delayed.addAll(Arrays.asList(((Runnable[]) ret)));
                        }
                    } catch (Throwable t) {
                        if (t instanceof InvocationTargetException) {
                            t = t.getCause();
                        }
                        notifier.fireTestFailure(new Failure(description, t));
                    }
                    schedule.invokeLater(new DelayedAndAfter(delayed));
                }
            });
        }
    }

    private static final class EmptyStatement extends Statement {
        public static final Statement EMPTY = new EmptyStatement();

        @Override
        public void evaluate() throws Throwable {
        }
    }
}

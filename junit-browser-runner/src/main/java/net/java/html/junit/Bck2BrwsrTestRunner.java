package net.java.html.junit;

import java.io.IOException;
import java.util.List;
import junit.framework.AssertionFailedError;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestListener;
import junit.framework.TestResult;
import net.java.html.js.JavaScriptBody;
import org.apidesign.bck2brwsr.launcher.InvocationContext;
import org.apidesign.bck2brwsr.launcher.Launcher;
import static org.junit.Assert.fail;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
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

final class Bck2BrwsrTestRunner extends AbstractTestRunner {
    @Override
    String name() {
        return "Bck2Brwsr";
    }

    @Override
    void execute(Runnable run) {
        run.run();
    }

    @JavaScriptBody(args = {}, body = "return document && document.getElementById ? true : false;")
    private static boolean isInBrowser() {
        return false;
    }


    static boolean register(List<AbstractTestRunner> ctxs, Class<?> clazzToTest) throws IOException {
        if (isInBrowser()) {
            ctxs.add(new Bck2BrwsrTestRunner());
            return true;
        }
        return false;
    }

    static void registerRunner(List<Runner> runners, Class<?> clazzToTest) {
        if (isInBrowser()) {
            return;
        }

        try {
            Class.forName("org.apidesign.bck2brwsr.launcher.Bck2BrwsrLauncher");
        } catch (ClassNotFoundException ex) {
            return;
        }
        runners.add(createRunner(clazzToTest));
    }

    private static Object sharedLauncher;
    private static Runner createRunner(final Class<?> clazzToTest) {
        final Description description = Description.createSuiteDescription(clazzToTest.getSimpleName());
        Runner runner = new Runner() {
            @Override
            public Description getDescription() {
                return description;
            }

            @Override
            public void run(RunNotifier notifier) {
                try {
                    notifier.fireTestRunStarted(description);
                    Launcher launcher;
                    if (sharedLauncher instanceof Launcher) {
                        launcher = (Launcher) sharedLauncher;
                    } else {
                        sharedLauncher = launcher = Launcher.createBrowser(System.getProperty("junit.browser"));
                        launcher.initialize();
                    }
                    InvocationContext invocation = launcher.createInvocation(BrowserRunner.class, "execute");
                    invocation.setArguments(clazzToTest.getName());
                    String result = invocation.invoke();
                    if (result.contains("error:")) {
                        notifier.fireTestFailure(new Failure(description, new Throwable(result)));
                    }
                } catch (IOException ex) {
                    notifier.fireTestFailure(new Failure(description, ex));
                } finally {
                    notifier.fireTestFinished(description);
                }
            }
        };
        return runner;
    }

    static void runAsJUnit(String className) throws ClassNotFoundException {
        TestResult tr = new TestResult();
        class L implements TestListener {

            boolean error;
            StringBuilder sb = new StringBuilder();

            @Override
            public void addError(Test test, Throwable e) {
                sb.append("error: ").append(test.toString());
                sb.append(" message: ").append(e.getMessage());
                sb.append("\n");
                error = true;
            }

            @Override
            public void addFailure(Test test, AssertionFailedError e) {
                sb.append("error: ").append(test.toString());
                sb.append(" message: ").append(e.getMessage());
                sb.append("\n");
                error = true;
            }

            @Override
            public void endTest(Test test) {
            }

            @Override
            public void startTest(Test test) {
            }
        }
        L listener = new L();
        tr.addListener(listener);
        listener.sb.append("Searching for ").append(className).append("\n");
        Class<?> clazz = Class.forName(className);
        listener.sb.append("Starting the test ").append(clazz).append("\n");
        JUnit4TestAdapter suite = new JUnit4TestAdapter(clazz);
        suite.run(tr);
        listener.sb.append("End of test run\n");

        if (listener.error) {
            fail(listener.sb.toString());
        }
    }

}

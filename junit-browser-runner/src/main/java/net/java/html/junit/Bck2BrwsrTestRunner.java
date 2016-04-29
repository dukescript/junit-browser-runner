package net.java.html.junit;

import java.io.IOException;
import java.util.List;
import net.java.html.js.JavaScriptBody;
import org.apidesign.bck2brwsr.launcher.InvocationContext;
import org.apidesign.bck2brwsr.launcher.Launcher;
import static org.junit.Assert.fail;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
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
                        sharedLauncher = launcher = Launcher.createBrowser(System.getProperty("junit.browser"), null, "/net/java/html/junit/runner.html");
                        launcher.initialize();
                    }
                    InvocationContext invocation = launcher.createInvocation(BrowserRunner.class, "execute");
                    invocation.setArguments(clazzToTest.getName());
                    String result = invocation.invoke();
                    if (result == null || result.contains("error:")) {
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

    private static final class TestListener extends RunListener {
        private final StringBuilder sb = new StringBuilder();
        private final Class<?> clazz;
        boolean error;

        private TestListener(String className) throws ClassNotFoundException {
            log("Searching for", className);
            clazz = Class.forName(className);
            log("Starting the test", clazz);
        }

        private final void log(String msg, Object param) {
            System.err.println(msg + param);
            sb.append(msg).append(" ").append(param).append("\n");
        }

        @Override
        public void testIgnored(Description description) throws Exception {
            log("testIgnored", description);
        }

        @Override
        public void testAssumptionFailure(Failure failure) {
            log("testAssumptionFailure", failure);
            error = true;
        }

        @Override
        public void testFailure(Failure failure) throws Exception {
            log("testFailure", failure);
            error = true;
        }

        @Override
        public void testFinished(Description description) throws Exception {
            log("testFinished", description);
        }

        @Override
        public void testStarted(Description description) throws Exception {
            log("testStarted", description);
        }

        @Override
        public void testRunFinished(Result result) throws Exception {
            log("testRunFinished", result);
        }

        @Override
        public void testRunStarted(Description description) throws Exception {
            log("testRunStarted", description);
        }

        public void start() {
            Request request = Request.aClass(clazz);
            Runner runner = request.getRunner();
            RunNotifier notifier = new RunNotifier();
            notifier.addListener(this);
            runner.run(notifier);

            log("End of test run", clazz);

            if (error) {
                fail(sb.toString());
            }
        }
    }
    static void runAsJUnit(String className) throws ClassNotFoundException {
        TestListener listener = new TestListener(className);
        listener.start();
    }

}

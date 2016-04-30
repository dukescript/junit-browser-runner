package net.java.html.junit;

import java.io.IOException;
import java.util.List;
import net.java.html.js.JavaScriptBody;
import org.apidesign.bck2brwsr.launcher.InvocationContext;
import org.apidesign.bck2brwsr.launcher.Launcher;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import static org.junit.Assert.fail;
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

    @JavaScriptBody(args = { "run", "timeout" }, javacall = true, body = ""
        + "window.setTimeout(function() {\n"
        + "  run.@java.lang.Runnable::run()();\n"
        + "}, timeout);\n"
        + ""
    )
    private static native void setTimeout(Runnable run, int timeout);

    @Override
    void invokeLater(Runnable run) {
        setTimeout(run, 100);
    }

    @Override
    void invokeNow(Runnable run) {
        setTimeout(run, 1);
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
        private MultiNotifier notifier;
        boolean error;

        private TestListener(String className) throws ClassNotFoundException {
            log("Searching for", className);
            clazz = Class.forName(className);
            log("Starting the test", clazz);
        }

        private final void log(String msg, Object... param) {
            StringBuilder text = new StringBuilder(msg);
            for (Object p : param) {
                text.append(' ').append(p);
            }
            text.append('\n');
            System.err.println(text.toString());
            sb.append(text.toString());
        }

        @Override
        public void testIgnored(Description description) throws Exception {
            log("ignored:", description);
        }

        @Override
        public void testAssumptionFailure(Failure failure) {
            log("error:", failure);
            error = true;
        }

        @Override
        public void testFailure(Failure failure) throws Exception {
            log("error:", failure.getDescription(), failure.getMessage());
            error = true;
        }

        @Override
        public void testFinished(Description description) throws Exception {
            log("finished:", description);
        }

        @Override
        public void testStarted(Description description) throws Exception {
            log("started:", description.getClassName(), description.getMethodName());
        }

        @Override
        public void testRunFinished(Result result) throws Exception {
            log("executed", result.getRunCount(), "tests", "in", result.getRunTime(), "ms", result.getFailureCount(), "failures");
        }

        @Override
        public void testRunStarted(Description description) throws Exception {
            log("starting:", description.getDisplayName());
        }

        public void start() throws InterruptedException {
            Request request = Request.aClass(clazz);
            Runner runner = request.getRunner();
            notifier = new MultiNotifier(this, runner.getDescription());
            runner.run(notifier);
            waitForAll();
        }

        public void waitForAll() throws InterruptedException {
            if (notifier != null) {
                notifier.waitForAll();
            }
            log("End of test run", clazz);
        }
        
        public void assertResult() {
            if (error) {
                fail(sb.toString());
            }
        }
    }
    private static TestListener listener;
    static void runAsJUnit(String className) throws ClassNotFoundException, InterruptedException {
        TestListener l = listener;
        if (l == null) {
            listener = l = new TestListener(className);
            l.start();
        } else {
            l.waitForAll();
        }
        listener = null;
        l.assertResult();
    }

}

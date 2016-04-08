package net.java.html.junit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.AssertionFailedError;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestListener;
import junit.framework.TestResult;
import net.java.html.js.JavaScriptBody;
import static net.java.html.junit.AbstractTestRunner.exposeHTML;
import net.java.html.junit.JQuery2_2_2.SetupUI;
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
    static boolean isInBrowser() {
        return false;
    }


    static boolean register(List<AbstractTestRunner> ctxs, Class<?> clazzToTest) throws IOException {
        if (isInBrowser()) {
            SetupUI.init();
            HTMLContent content = clazzToTest.getAnnotation(HTMLContent.class);
            if (content != null) {
                exposeHTML(content.value());
            }
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
                        sharedLauncher = launcher = Launcher.createBrowser(null);
                        launcher.initialize();
                    }
                    InvocationContext invocation = launcher.createInvocation(BrowserRunner.class, "execute");
                    invocation.setArguments(clazzToTest.getName());
                    invocation.invoke();
                } catch (IOException ex) {
                    notifier.fireTestFailure(new Failure(description, ex));
                } finally {
                    notifier.fireTestFinished(description);
                }
            }
        };
        return runner;
    }

    static void runAsJUnit(final String className) throws ClassNotFoundException {
        TestResult tr = new TestResult();
        Class<?> clazz = Class.forName(className);
        JUnit4TestAdapter suite = new JUnit4TestAdapter(clazz); 
        Description description = suite.getDescription();
        Description parent = description.getChildren().get(0);
        ArrayList<Description> children1 = parent.getChildren();
        String [] names = new String[children1.size()];
        for (int i = 0; i < names.length; i++) {
           names[i] = children1.get(i).getMethodName(); 
        }
        final TreeView treeview = TreeView.create(className, names);

        treeview.setHandler_impl("rerun"+className, new Runnable() {
            @Override
            public void run() {
                try {
                    BrowserRunner.execute(className);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(Bck2BrwsrTestRunner.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        class L implements TestListener {

            int finished = 0;

            boolean error;
            StringBuilder sb = new StringBuilder();

            @Override
            public void addError(Test test, Throwable e) {
                sb.append("error: ").append(test.toString()).append("\n");
                error = true;
            }

            @Override
            public void addFailure(Test test, AssertionFailedError e) {
                sb.append("failure: ").append(test.toString()).append("\n");;
                error = true;
            }

            @Override
            public void endTest(Test test) {
                sb.append("finished: ").append(test.toString()).append("\n");;
                treeview.setValue(++finished);
            }

            @Override
            public void startTest(Test test) {
                sb.append("started: ").append(test.toString()).append("\n");;
            }
        }
        L listener = new L();
        tr.addListener(listener);
        listener.sb.append("Searching for ").append(className).append("\n");
        listener.sb.append("Starting the test ").append(clazz).append("\n");
        suite.run(tr);
        listener.sb.append("End of test run\n");

        if (listener.error) {
            fail(listener.sb.toString());
        }
    }


}

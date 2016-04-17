package net.java.html.junit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

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

public final class BrowserRunner extends Suite {
    private final List<Runner> cases;

    public BrowserRunner(Class<?> klass) throws InitializationError {
        super(klass, Collections.<Runner>emptyList());
        cases = new ArrayList<>();
        for (AbstractTestRunner info : create(klass)) {
            cases.add(new SingleBrowserRunner(info.name(), info, klass));
        }
        Bck2BrwsrTestRunner.registerRunner(cases, klass);
        if (cases.isEmpty()) {
            throw new InitializationError("No presenter found. Add net.java.html.fx or script JAR on classpath!");
        }
    }
    @Override
    protected List<Runner> getChildren() {
        return cases;
    }

    /** Loads given class and executes its tests.
     *
     * @param className the class name
     * @throws java.lang.ClassNotFoundException if the class cannot be loaded
     */
    public static void execute(String className) throws ClassNotFoundException {
        Bck2BrwsrTestRunner.runAsJUnit(className);
    }

    private static AbstractTestRunner[] contexts;
    private static AbstractTestRunner[] create(Class<?> klass) throws InitializationError {
        if (contexts != null) {
            return contexts;
        }
        final List<AbstractTestRunner> ctxs = new ArrayList<>();
        final String url;
        try {
            boolean bck2brwsrRunner = Bck2BrwsrTestRunner.register(ctxs, klass);
            if (bck2brwsrRunner) {
                contexts = ctxs.toArray(new AbstractTestRunner[ctxs.size()]);
                return contexts;
            }

            final File page = File.createTempFile("test", ".html");
            HTMLContent content = klass.getAnnotation(HTMLContent.class);
            if (content != null) {
                try (FileWriter w = new FileWriter(page)) {
                    w.write("<html>\n");
                    w.write("<body>\n");
                    w.write("<h1>DukeScript JUnit Browser Runner</h1>\n");
                    w.write("</body>\n");
                    w.write("</html>\n");
                }
            }
            url = page.toURI().toASCIIString();
            page.deleteOnExit();
        } catch (IOException ex) {
            throw new InitializationError(ex);
        }
        PresenterTestRunner.registerPresenters(ctxs, url, klass);
        ScriptTestRunner.register(ctxs, url, klass);
        contexts = ctxs.toArray(new AbstractTestRunner[ctxs.size()]);
        return contexts;
    }

}

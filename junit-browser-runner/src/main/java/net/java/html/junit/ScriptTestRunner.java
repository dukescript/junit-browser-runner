package net.java.html.junit;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.junit.runners.model.InitializationError;
import org.netbeans.html.boot.spi.Fn;

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

final class ScriptTestRunner  {
    ScriptTestRunner() {
    }

    static void register(List<AbstractTestRunner> ctxs, String url, Class<?> klass) throws InitializationError {
        try {
            Class<?> scripts = klass.getClassLoader().loadClass("net.java.html.boot.script.Scripts");
            Fn.Presenter p = (Fn.Presenter) scripts.getMethod("createPresenter").invoke(null);
            ctxs.add(new PresenterTestRunner(url, url, p, klass));
        } catch (ClassNotFoundException ex) {
            // OK, go on
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new InitializationError(ex);
        }
    }

}

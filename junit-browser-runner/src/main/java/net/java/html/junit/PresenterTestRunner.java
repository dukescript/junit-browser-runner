package net.java.html.junit;

import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import net.java.html.BrwsrCtx;
import net.java.html.boot.BrowserBuilder;
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

final class PresenterTestRunner extends AbstractTestRunner {
    final String name;
    final BrwsrCtx ctx;

    PresenterTestRunner(String name, String url, Fn.Presenter p, Class<?> klass) throws InitializationError {
        this.name = name;
        this.ctx = initPresenter(url, p, klass);
    }

    static void registerPresenters(List<? super AbstractTestRunner> ctxs, String url, Class<?> klass) throws InitializationError {
        for (Fn.Presenter p : ServiceLoader.load(Fn.Presenter.class)) {
            ctxs.add(new PresenterTestRunner(p.getClass().getSimpleName(), url, p, klass));
        }
    }

    private static BrwsrCtx initPresenter(String url, Fn.Presenter p, final Class<?> klass) throws InitializationError {
        final BrwsrCtx[] ret = {null};
        final CountDownLatch cdl = new CountDownLatch(1);
        final BrowserBuilder bb = BrowserBuilder.newBrowser(p).loadFinished(new Runnable() {
            @Override
            public void run() {
                ret[0] = BrwsrCtx.findDefault(klass);
                JQuery2_2_2.SetupUI.init();
                cdl.countDown();
            }
        }).loadPage(url);
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                bb.showAndWait();
            }
        });
        try {
            cdl.await();
        } catch (InterruptedException ex) {
            throw new InitializationError(ex);
        }
        return ret[0];
    }

    @Override
    void execute(Runnable run) {
        ctx.execute(run);
    }

    @Override
    String name() {
        return name;
    }
}

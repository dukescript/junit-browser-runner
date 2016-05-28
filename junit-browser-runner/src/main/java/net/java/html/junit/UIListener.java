package net.java.html.junit;

import java.net.URL;
import java.util.ServiceLoader;
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

final class UIListener {
    private final RunListener listener;
    private final URL page;
    private final String resource;

    private UIListener(RunListener listener, URL page, String resource) {
        this.listener = listener;
        this.page = page;
        this.resource = resource;
    }

    RunListener getListener() {
        return listener;
    }

    URL getPage() {
        return page;
    }

    String getResource() {
        return resource;
    }

    static UIListener create() {
        try {
            for (RunListener listener : ServiceLoader.load(RunListener.class)) {
                final Class<? extends RunListener> listenerClass = listener.getClass();
                String fqnSlash = listenerClass.getName().replace('.', '/');
                int last = fqnSlash.lastIndexOf('/') + 1;
                final String name = fqnSlash.substring(last) + ".html";
                URL dynamicURL = listenerClass.getResource(name);
                if (dynamicURL != null) {
                    String resource = fqnSlash.substring(0, last) + name;
                    return new UIListener(listener, dynamicURL, resource);
                }
            }
        } catch (Exception | Error ex) {
            ex.printStackTrace();
        }
        String resource = "/net/java/html/junit/runner.html";
        URL url = UIListener.class.getResource("runner.html");
        return new UIListener(new RunListener(),  url, resource);
    }
}

package net.java.html.junit;

import net.java.html.js.JavaScriptBody;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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
@RunWith(BrowserRunner.class)
public class TimeoutTest implements Runnable {
    private int count = -10;

    public TimeoutTest() {
    }

    @Before
    public void initialize() {
        count = 0;
    }

    @JavaScriptBody(args = { "r", "time" }, javacall = true, body = ""
        + "window.setTimeout(function() {\n"
        + "  r.@java.lang.Runnable::run()();\n"
        + "}, time);\n"
    )
    private static native void setTimeout(Runnable r, int time);

    @Override
    public void run() {
        count += 5;
    }

    @Test
    public Runnable testRunnable() {
        setTimeout(this, 10);
        return new Runnable() {
            @Override
            public void run() {
                assertEquals("Delayed check", 5, count);
                count += 3;
            }
        };
    }

    @After
    public void afterTest() {
        assertEquals("After test check", 8, count);
    }

}

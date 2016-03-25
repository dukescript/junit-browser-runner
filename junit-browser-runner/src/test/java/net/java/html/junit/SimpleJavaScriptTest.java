package net.java.html.junit;

import net.java.html.js.JavaScriptBody;
import static org.junit.Assert.assertEquals;
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
@HTMLContent("<span id='intest'>Value</span>")
public class SimpleJavaScriptTest {
    public SimpleJavaScriptTest() {
    }

    @Test
    public void testInvocations() {
        assertEquals(33, constant());
    }

    @Test
    public void testElementPresent() {
        assertEquals("Value", findElementText("intest"));
    }

    @JavaScriptBody(args = {  }, body = "return 33;")
    private static native int constant();

    @JavaScriptBody(args = { "id" }, body = "return document.getElementById(id).innerHTML;")
    private static native String findElementText(String id);
}

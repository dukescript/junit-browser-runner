package net.java.html.junit;

import net.java.html.js.JavaScriptBody;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;

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
@HTMLContent("<span id='intest'>AnotherValue</span>")
public class DestroyContentTest {
    public DestroyContentTest() {
    }

    @Test
    public void testElementPresent() {
        assertEquals("AnotherValue", getChangeElement("intest", "3rdValue"));
        assertEquals("3rdValue", getChangeElement("intest", null));
    }

    @Test
    public void testElementPresentUnmodified() {
        assertEquals("AnotherValue", getChangeElement("intest", "4thValue"));
        assertEquals("4thValue", getChangeElement("intest", null));
    }

    @JavaScriptBody(args = { "id", "newValue" }, body = "\n"
        + "if (!document) return 'no document';\n"
        + "var e = document.getElementById(id);\n"
        + "if (!e) return 'no element for ' + id + ' body: ' + document.body.innerHTML;\n"
        + "var result = e.innerHTML;\n"
        + "if (newValue) e.innerHTML = newValue;\n"
        + "return result;\n"
    )
    private static native String getChangeElement(String id, String newValue);
}

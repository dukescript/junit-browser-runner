package com.dukescript.junit.chartsui;

import net.java.html.junit.BrowserRunner;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertFalse;
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

/** Sample failing tests executed by {@link BrowserRunner runners}
 * specified in your <code>pom.xml</code> dependency section.
 */
@RunWith(BrowserRunner.class)
public class FailuresAndAssertsTest {
    @Test public void testAdd() {
        int x = 1;
        for (int i = 0; i < 10; i++) {
            x = x + i;
        }
        assertEquals("Value is correct", 46, x);
    }
    @Test public void mathMin() {
        assertEquals("Min is 3", 3, Math.min(3, 5));
    }
    @Test public void trueIsntFalse() {
        assertFalse("isn't false", true);
    }
    @Test public void falseIsFalse() {
        assertFalse("it is false", false);
    }
    @Test public void throwException() {
        throw new IllegalStateException("HI");
    }
    @Test public void failingTest() {
        fail("bade");
    }
}

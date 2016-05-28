package com.dukescript.junit.chartsui;

import net.java.html.junit.BrowserRunner;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Sample failing tests executed by {@link BrowserRunner runners}
 * specified in your <code>pom.xml</code> dependency section.
 */
@RunWith(BrowserRunner.class)
public class WorksOKTest {
    @Test public void testFactorial() {
        int x = 1;
        for (int i = 1; i <= 5; i++) {
            x = x * i;
        }
        assertEquals("Value is correct", 120, x);
    }
    @Test public void mathMath() {
        assertEquals("Max", 5, Math.max(3, 5));
    }
    @Test public void emptyTest() {
    }
}

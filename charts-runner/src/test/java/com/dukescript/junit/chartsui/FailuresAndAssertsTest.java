package com.dukescript.junit.chartsui;

import net.java.html.junit.BrowserRunner;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/** Sample failing tests executed by {@link BrowserRunner runners}
 * specified in your <code>pom.xml</code> dependency section.
 */
@RunWith(BrowserRunner.class)
public class FailuresAndAssertsTest {
    @Test public void x1() {
    }
    @Test public void x2() {
    }
    @Test public void x3() {
    }
    @Test public void x4() {
    }
    @Test public void x5() {
        assertFalse("isn't false", true);
    }
    @Test public void x6() {
    }
    @Test public void x7() {
    }
    @Test public void x8() {
    }
    @Test public void x9() {
        throw new IllegalStateException("HI");
    }
    @Test public void x10() {
        fail("bade");
    }
}

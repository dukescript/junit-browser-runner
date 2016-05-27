package com.dukescript.junit.chartsui;

import java.io.File;
import java.io.IOException;
import net.java.html.junit.BrowserRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Sample failing tests executed by {@link BrowserRunner runners}
 * specified in your <code>pom.xml</code> dependency section.
 */
@RunWith(BrowserRunner.class)
public class FailOnlyInBck2BrwsrTest {
    @Test public void createAFile() throws IOException {
        File file = File.createTempFile("cant", ".exists");
        Assert.assertNotNull("Can we create temporary file?", file);
        file.delete();
    }

    @Test public void x1() {
    }
    @Test public void x2() {
    }
    @Test public void x3() {
    }
    @Test public void x4() {
    }
    @Test public void x5() {
    }
    @Test public void x6() {
    }
    @Test public void x7() {
    }
    @Test public void x8() {
    }
    @Test public void x9() {
    }
    @Test public void x10() {
    }
}

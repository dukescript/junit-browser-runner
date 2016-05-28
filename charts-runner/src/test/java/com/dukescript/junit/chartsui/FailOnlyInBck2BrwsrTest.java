package com.dukescript.junit.chartsui;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import net.java.html.junit.BrowserRunner;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Sample failing tests executed by {@link BrowserRunner runners}
 * specified in your <code>pom.xml</code> dependency section.
 */
@RunWith(BrowserRunner.class)
public class FailOnlyInBck2BrwsrTest {
    @Test
    public void createAFile() throws IOException {
        File file = File.createTempFile("cant", ".exists");
        Assert.assertNotNull("Can we create temporary file?", file);
        file.delete();
    }

    @Test
    public void useBuffers() {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(2, (byte)10);
        assertEquals("Nothing at 0", 0, buffer.get());
        assertEquals("Nothing at 1", 0, buffer.get());
        assertEquals("Ten at 2", 10, buffer.get());
    }

    @Test
    public void streamsWorkOK() throws IOException {
        try (ByteArrayInputStream is = new ByteArrayInputStream(new byte[] {
            (byte)0, (byte)0, (byte)10
        })) {
            assertEquals("Nothing at 0", 0, is.read());
            assertEquals("Nothing at 1", 0, is.read());
            assertEquals("Ten at 2", 10, is.read());
        }
    }
}

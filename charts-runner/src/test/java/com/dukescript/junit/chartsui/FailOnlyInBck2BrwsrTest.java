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

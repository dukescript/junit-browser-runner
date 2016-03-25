package net.java.html.junit;

import org.junit.After;
import org.junit.Assert;
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
public class ChainingTest {
    private int count;

    public ChainingTest() {
    }

    @Before
    public void initialize() {
        count = 0;
    }

    @After
    public void assertFive() {
        Assert.assertEquals(5, count);
    }

    @Test
    public Runnable testRunnable() {
        count += 2;
        return new Runnable() {
            @Override
            public void run() {
                count += 3;
            }
        };
    }

    @Test
    public Runnable[] testArrayOfRunnables() {
        count++;
        return new Runnable[] {
            new Runnable() {
                @Override
                public void run() {
                    count++;
                }
            },
            new Runnable() {
                @Override
                public void run() {
                    count += 2;
                }
            },
            new Runnable() {
                @Override
                public void run() {
                    count++;
                }
            },
        };
    }

}

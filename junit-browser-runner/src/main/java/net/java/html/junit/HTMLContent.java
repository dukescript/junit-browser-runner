package net.java.html.junit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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

/**
 * Provide content of a page to be displayed when running a test in
 * the browser.
 * @see BrowserRunner
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface HTMLContent {
    /** The content of the page before starting each {@link BrowserRunner} test.
     * @return the HTML text to be embedded in the page
     */
    public String value();
}

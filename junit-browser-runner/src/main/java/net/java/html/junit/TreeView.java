/*
 * Copyright (c) 2016 Eppleton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eppleton - initial API and implementation and/or initial documentation
 */
package net.java.html.junit;

import net.java.html.js.JavaScriptBody;
import net.java.html.js.JavaScriptResource;

/**
 *
 * @author Eppleton
 */
@JavaScriptResource("progressbar.js")
public class TreeView {
    Object delegate;

    public static TreeView create(String id, String[] methods){
        TreeView progressBar = new TreeView(createProgressBar_impl(id, methods));
        progressBar.setMax(methods.length);
        return progressBar;
    }
    
    @JavaScriptBody(args = { "id", "methods" },body = "return window.junitui.createTreeView(id, methods);" )
    private static native Object createProgressBar_impl(String id, String[] methods);
    
   private TreeView(Object delegate) {
        this.delegate = delegate;
    }
    
    public void setValue(int val){
        setValue_impl(delegate, val);
    }
    
    public void setMax(int val){
        setMax_impl(delegate, val);
    }
    
    @JavaScriptBody(args = { "delegate", "val" },body = "delegate.attr('value',val);")
    public static native void setValue_impl(Object delegate, int val);
    
    @JavaScriptBody(args = { "delegate", "val" },body = "delegate.attr('max',val);")
    public static native void setMax_impl(Object delegate, int val);

    @JavaScriptBody(args = { "id" , "r"},body = "var ide = id.split(\".\").join(\"_\");"
            + "$('#'+ide).unbind('click');"
            + "$('#'+ide).click(function (event) {\n" +
"            event.preventDefault();\n" +
"            r.@java.lang.Runnable::run()();\n" +
"        });", javacall = true)
    public static native void setHandler_impl(String id, Runnable r);
}

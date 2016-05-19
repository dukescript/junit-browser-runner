/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package com.dukescript.app.junitonline.javac;

import java.io.IOException;
import java.util.Arrays;
import net.java.html.junit.BrowserRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BrowserRunner.class)
public class ModelClassTest  {
    @Test
    public void canGenerateCallback() throws IOException {
        String html = "";
        String java = "package x.y.z;\n"
            + "import net.java.html.js.JavaScriptBody;\n"
            + "public class X {\n"
            + "   @JavaScriptBody(args = \"r\", javacall = true, body = \"r.@java.lang.Runnable::run()()\")\n"
            + "   public static native void call(Runnable r);"
            + "}\n";

        Compile result = Compile.create(html, java);

        final byte[] bytes = result.get("x/y/z/X.class");
        assertNotNull(bytes, "Class X is compiled: " + result);
        final byte[] bytes2 = result.get("x/y/z/$JsCallbacks$.class");
        assertNotNull(bytes2, "Class for callbacks is compiled: " + result);
    }

    @Test
    public String testAnnotationProcessorCompile() throws IOException {
        String html = "";
        String java = "package x.y.z;"
            + "@net.java.html.json.Model(className=\"Y\", properties={})\n"
            + "class X {\n"
            + "   static void main(String... args) { Y y = new Y(); }\n"
            + "}\n";
        Compile result = Compile.create(html, java);

        final byte[] bytes = result.get("x/y/z/Y.class");
        assertNotNull(bytes, "Class Y is compiled: " + result);

        byte[] out = new byte[256];
        System.arraycopy(bytes, 0, out, 0, Math.min(out.length, bytes.length));
        return Arrays.toString(out);
    }

//    @Test
    public String modelReferencesClass() throws IOException {
        String html = "";
        String java = "package x.y.z;"
            + "@net.java.html.json.Model(className=\"Y\", properties={\n"
            + "  @net.java.html.json.Property(name=\"x\",type=X.class, array = true)\n"
            + "})\n"
            + "class YImpl {\n"
            + "  @net.java.html.json.Model(className=\"X\", properties={})\n"
            + "  static class XImpl {\n"
            + "  }\n"
            + "  static void main(String... args) {\n"
            + "     Y y = new Y(new X(), new X());\n"
            + "     y.applyBindings();\n"
            + "  }\n"
            + "}\n";
        Compile result = Compile.create(html, java);

        final byte[] bytes = result.get("x/y/z/Y.class");
        assertNotNull(bytes, "Class Y is compiled: " + result);

        byte[] out = new byte[256];
        System.arraycopy(bytes, 0, out, 0, Math.min(out.length, bytes.length));
        return Arrays.toString(out);
    }

    static void assertNotNull(Object obj, String msg) {
        assert obj != null : msg;
    }

    static void assertEquals(Object real, Object exp, String msg) {
        if (real == exp) {
            return;
        }
        assert real != null && real.equals(exp) : msg + ". Expected: " + exp + " but was: " + real;
    }
}

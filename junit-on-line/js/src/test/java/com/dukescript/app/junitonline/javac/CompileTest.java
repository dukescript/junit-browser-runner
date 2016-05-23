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
import net.java.html.junit.BrowserRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BrowserRunner.class)
public class CompileTest  {
    @Test
    public void testCompile() throws IOException {
        String java = "package x.y.z;"
            + "class X { "
            + "   static void main(String... args) { throw new RuntimeException(\"Hello brwsr!\"); }"
            + "}";
        Compile result = Compile.create(new JavacSource().putFileName("X.java").putText(java));

        final byte[] bytes = result.get("x/y/z/X.class");
        assertNotNull(bytes, "Class X is compiled: " + result);
    }

    @Test
    public void canCompilePublicClass() throws IOException {
        String java = "package x.y.z;"
            + "public class X {\n"
            + "   static void main(String... args) { throw new RuntimeException(\"Hello brwsr!\"); }\n"
            + "}\n";
        Compile result = Compile.create(new JavacSource().putFileName("X.java").putText(java));

        final byte[] bytes = result.get("x/y/z/X.class");
        assertNotNull(bytes, "Class X is compiled: " + result);
    }

    @Test
    public void mainClassIsFirst() throws IOException {
        String java = "package x.y.z;"
            + "public class X {\n"
            + "   class I1 {}\n"
            + "   class I2 {}\n"
            + "   class I3 {}\n"
            + "   class I4 {}\n"
            + "   class I5 {}\n"
            + "}\n";
        JavacResult result = JavacEndpoint.newCompiler().doCompile(
            new JavacQuery()
            .putType(JavacEndpoint.MsgType.compile)
            .putSources(new JavacSource().putFileName("X.java").putText(java))
        );
        assertEquals(result.getClasses().size(), 6, "Six classes generated");
        assertEquals(result.getClasses().get(0).getClassName(), "x/y/z/X.class", "Main class is the first one");
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

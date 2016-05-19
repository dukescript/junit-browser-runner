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

import com.dukescript.app.junitonline.nbjava.JavaCompletionItem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import net.java.html.BrwsrCtx;
import net.java.html.js.JavaScriptBody;
import net.java.html.json.Model;
import net.java.html.json.Models;
import net.java.html.json.Property;


public final class JavacEndpoint {
    private static final Logger LOG = Logger.getLogger(JavacEndpoint.class.getName());
    private Compile c = null;

    private JavacEndpoint() {
    }

    static {
        LOG.info("Registering Javac");
        registerJavacService();
        LOG.info("Javac service is available!");
    }

    static JavacEndpoint newCompiler() {
        return new JavacEndpoint();
    }


    @JavaScriptBody(args = {}, javacall = true, body =
        "window.createJavac = function() {\n"
      + "  var compiler = @com.dukescript.app.junitonline.javac.JavacEndpoint::newCompiler()();\n"
      + "  this.compile = function(q) {\n"
      + "    return compiler.@com.dukescript.app.junitonline.javac.JavacEndpoint::doCompile(Ljava/lang/Object;)(q);\n"
      + "  };\n"
      + "  return this;\n"
      + "}\n"
    )
    private static void registerJavacService() {
    }

    JavacResult doCompile(Object query) throws IOException {
        JavacQuery q = Models.fromRaw(BrwsrCtx.findDefault(JavacQuery.class), JavacQuery.class, query);
        return doCompile(q);
    }

    public JavacResult doCompile(JavacQuery query) throws IOException {
        JavacResult res = new JavacResult();
        res.setType(query.getType());
        res.setState(query.getState());

        String java = query.getJava();
        String html = query.getHtml();
        int offset = query.getOffset();

        if (c == null || !java.equals(c.getJava())) {
            c = Compile.create(html, java);
        }
        switch (query.getType()) {
            case autocomplete:
                LOG.info("Autocomplete");
                for (JavaCompletionItem jci : c.getCompletions(offset)) {
                    res.getCompletions().add(jci.toCompletionItem());
                }
                res.setStatus("Autocomplete finished.");
                return res;
            case checkForErrors:
                for (Diagnostic<? extends JavaFileObject> d : c.getErrors()) {
                    res.getErrors().add(JavacErrorModel.create(d));
                }
                res.setStatus(res.getErrors().isEmpty() ? "OK. No errors found." : "There are errors!");
                return res;
            case compile:
                LOG.log(Level.INFO, "Compiled {0}", c);
                for (Map.Entry<String, byte[]> e : c.getClasses().entrySet()) {
                    List<Byte> arr = new ArrayList<>(e.getValue().length);
                    for (byte b : e.getValue()) {
                        arr.add(b);
                    }
                    final JavacClass jc = new JavacClass(e.getKey());
                    jc.getByteCode().addAll(arr);
                    if (c.isMainClass(e.getKey())) {
                        res.getClasses().add(0, jc);
                    } else {
                        res.getClasses().add(jc);
                    }
                }
                res.setStatus(res.getClasses().isEmpty() ? "No bytecode has been generated!" : "OK");
                return res;
        }
        res.setStatus("Nothing to do!");
        return res;
    }


    //
    // protocol interfaces
    //

    enum MsgType {
        autocomplete, checkForErrors, compile;
    }

    @Model(className = "JavacQuery", properties = {
        @Property(name = "type", type = MsgType.class),
        @Property(name = "state", type = String.class),
        @Property(name = "html", type = String.class),
        @Property(name = "java", type = String.class),
        @Property(name = "offset", type = int.class)
    })
    static final class JavacQueryModel {
    }

    @Model(className = "JavacResult", properties = {
        @Property(name = "type", type = MsgType.class),
        @Property(name = "state", type = String.class),
        @Property(name = "status", type = String.class),
        @Property(name = "errors", type = JavacError.class, array = true),
        @Property(name = "classes", type = JavacClass.class, array = true),
        @Property(name = "completions", type = CompletionItem.class, array = true)
    })
    static final class JavacResultModel {
    }

    @Model(className = "JavacError", properties = {
        @Property(name = "col", type = long.class),
        @Property(name = "line", type = long.class),
        @Property(name = "kind", type = Diagnostic.Kind.class),
        @Property(name = "msg", type = String.class)
    })
    static final class JavacErrorModel {
        static JavacError create(Diagnostic<? extends JavaFileObject> d) {
            return new JavacError(
                    d.getColumnNumber(),
                    d.getLineNumber(),
                    d.getKind(),
                    d.getMessage(Locale.ENGLISH)
            );
        }
    }

    @Model(className = "JavacClass", properties = {
        @Property(name = "className", type = String.class),
        @Property(name = "byteCode", type = byte.class, array = true)
    })
    static final class JavacClassModel {
    }

    @Model(className = "CompletionItem", properties = {
        @Property(name = "text", type = String.class),
        @Property(name = "displayText", type = String.class),
        @Property(name = "className", type = String.class),
    })
    static final class CompletionItemModel {
    }

}

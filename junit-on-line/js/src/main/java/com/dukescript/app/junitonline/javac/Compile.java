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

import com.dukescript.app.junitonline.nbjava.CompilationInfo;
import com.dukescript.app.junitonline.nbjava.JavaCompletionItem;
import com.dukescript.app.junitonline.nbjava.JavaCompletionQuery;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;


final class Compile {

    private final String pkg;
    private final String cls;
    private final String html;
    private final URLFileManager clfm;
    private final CompilationInfo info;
    private Map<String, byte[]> classes = null;
    private List<Diagnostic<? extends JavaFileObject>> errors;

    private Compile(String html, String code, URL[] path) throws IOException {
        this.pkg = find("package", ';', code);
        this.cls = find("class ", ' ', code);
        this.html = html;
        this.clfm = new URLFileManager(path);

        final JavaFileObject file = clfm.createMemoryFileObject(
                URLFileManager.convertFQNToResource(pkg.isEmpty() ? cls : pkg + "." + cls) + Kind.SOURCE.extension,
                Kind.SOURCE,
                code.getBytes());
        final JavaFileObject htmlFile = clfm.createMemoryFileObject(
            URLFileManager.convertFQNToResource(pkg),
            Kind.OTHER,
            html.getBytes());

        JavaFileManager jfm = new ForwardingJavaFileManager<JavaFileManager>(clfm) {
            @Override
            public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
                if (location == StandardLocation.SOURCE_PATH) {
                    if (packageName.equals(pkg)) {
                        return htmlFile;
                    }
                }
                return null;
            }
        };

        this.info = new CompilationInfo(file, jfm);
    }

    /** Performs compilation of given HTML page and associated Java code
     */
    public static Compile create(String html, String code) throws IOException {
        URL[] urls = new URL[] {
            findURL(org.apidesign.bck2brwsr.emul.lang.System.class)
        };
        return new Compile(html, code, urls);
    }

    private static URL findURL(Class<?> c) {
        return c.getProtectionDomain().getCodeSource().getLocation();
    }

    public List<? extends JavaCompletionItem> getCompletions(int offset) {
        try {
            return JavaCompletionQuery.query(info, JavaCompletionQuery.COMPLETION_QUERY_TYPE, offset);
        } catch (Exception e) {}
        return Collections.emptyList();
    }

    /** Checks for given class among compiled resources */
    public byte[] get(String res) {
        return getClasses().get(res);
    }

    public Map<String, byte[]> getClasses() {
        if (classes == null) {
            classes = new HashMap<>();
            try {
                info.toPhase(CompilationInfo.Phase.GENERATED);
            } catch (IOException ioe) {}
            for (MemoryFileObject generated : clfm.getGeneratedFiles(Kind.CLASS)) {
                classes.put(generated.getName(), generated.getContent());
            }
        }
        return classes;
    }

    public boolean isMainClass(String name) {
        return name.endsWith('/' + cls + ".class");
    }

    /** Obtains errors created during compilation.
     */
    public List<Diagnostic<? extends JavaFileObject>> getErrors() {
        if (errors == null) {
            errors = new ArrayList<>();
            try {
                info.toPhase(CompilationInfo.Phase.RESOLVED);
            } catch (IOException ioe) {}
            for (Diagnostic<? extends JavaFileObject> diagnostic : info.getDiagnostics()) {
                if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                    errors.add(diagnostic);
                }
            }
        }
        return errors;
    }

    private static String find(String pref, char term, String java) throws IOException {
        int pkg = java.indexOf(pref);
        if (pkg != -1) {
            pkg += pref.length();
            while (Character.isWhitespace(java.charAt(pkg))) {
                pkg++;
            }
            int semicolon = java.indexOf(term, pkg);
            if (semicolon != -1) {
                String pkgName = java.substring(pkg, semicolon).trim();
                return pkgName;
            }
        }
        throw new IOException("Can't find " + pref + " declaration in the java file");
    }

    String getHtml() {
        String fqn = "'" + pkg + '.' + cls + "'";
        return html.replace("'${fqn}'", fqn);
    }

    String getJava() {
        return info != null ? info.getText() : null;
    }
}

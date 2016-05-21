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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

import static javax.tools.StandardLocation.*;

/**
 *
 * @author Tomas Zezula
 */
public class URLFileManager implements JavaFileManager {

    private static final Location[] READ_LOCATIONS = {
        PLATFORM_CLASS_PATH,
        CLASS_PATH,
        SOURCE_PATH
    };

    private static final Location[] WRITE_LOCATIONS = {
        CLASS_OUTPUT,
        SOURCE_OUTPUT
    };

    private static final Location[] CLASS_LOADER_LOCATIONS = {
        ANNOTATION_PROCESSOR_PATH
    };

    private Map<Location, Map<String,List<MemoryFileObject>>> generated;
    private static Map<String,byte[]> classPath;
    static {
        try {
            classPath = readAll();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }


    URLFileManager() throws IOException {
        generated = new HashMap<>();
        for (Location l : WRITE_LOCATIONS) {
            generated.put(l, new HashMap<String, List<MemoryFileObject>>());
        }
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        if (canClassLoad(location)) {
            return getClass().getClassLoader();
        } else {
            return null;
        }
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
        /* Correctly canRead(location) should be used. However in the dew the rsources are loaded
         * from the CLFM classloader so PLATFORM_CLASS_PATH and CLASSPATH are duplicates (javac allways
         * calls list for both PLATFORM_CLASS_PATH and CLASSPATH.
         * Also SOURCE_PATH is ignored as in dew there is no source path, just a single source file
         * and SOURCE_OUTPUT for AnnotationProcessors
         *
         */
        if (location == PLATFORM_CLASS_PATH /*canRead(location)*/) {
            final List<JavaFileObject> res = new ArrayList<JavaFileObject>();
            for (String resource : getResources(convertFQNToResource(packageName))) {
                final JavaFileObject jfo = new ClassLoaderJavaFileObject(this, resource);
                if (kinds.contains(jfo.getKind())) {
                    res.add(jfo);
                }
            }
            return res;
        } else if (canWrite(location)) {
            Map<String,List<MemoryFileObject>> folders = generated.get(location);
            List<MemoryFileObject> files = folders.get(convertFQNToResource(packageName));
            if (files != null) {
                final List<JavaFileObject> res = new ArrayList<JavaFileObject>();
                for (JavaFileObject file : files) {
                    if (kinds.contains(file.getKind()) && file.getLastModified() >= 0) {
                        res.add(file);
                    }
                }
                return res;
            }
        }
        return Collections.<JavaFileObject>emptyList();
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        return ((BaseFileObject)file).infer();
    }

    @Override
    public boolean isSameFile(FileObject a, FileObject b) {
        return a.toUri().equals(b.toUri());
    }

    @Override
    public boolean handleOption(String current, Iterator<String> remaining) {
        return false;
    }

    @Override
    public boolean hasLocation(Location location) {
        return
            location == CLASS_OUTPUT ||
            location == CLASS_PATH ||
            location == SOURCE_PATH ||
            location == ANNOTATION_PROCESSOR_PATH ||
            location == PLATFORM_CLASS_PATH;
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws IOException {
        if (canRead(location)) {
            return new ClassLoaderJavaFileObject(this, convertFQNToResource(className) + kind.extension);
        } else {
            throw new UnsupportedOperationException("Unsupported location for reading java file: " + location);   //NOI18N
        }
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        if (canWrite(location)) {
            final String resource = convertFQNToResource(className) + kind.extension;
            final MemoryFileObject res = new MemoryFileObject(resource, null);
            register(location, resource, res);
            return res;
        } else {
            throw new UnsupportedOperationException("Unsupported location for writing java : " + location);   //NOI18N
        }
    }

    @Override
    public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
        if (canRead(location)) {
            StringBuilder resource = new StringBuilder(convertFQNToResource(packageName));
            if (resource.length() > 0) {
                resource.append('/');   //NOI18N
            }
            resource.append(relativeName);
            return new ClassLoaderJavaFileObject(this, resource.toString());
        } else {
            throw new UnsupportedOperationException("Unsupported location for reading file: " + location);   //NOI18N
        }
    }

    @Override
    public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
        if (canWrite(location)) {
            StringBuilder resource = new StringBuilder(convertFQNToResource(packageName));
            if (resource.length() > 0) {
                resource.append('/');   //NOI18N
            }
            resource.append(relativeName);
            String resourceStr = resource.toString();
            final MemoryFileObject res = new MemoryFileObject(resourceStr, null);
            register(location, resourceStr, res);
            return res;
        } else {
            throw new UnsupportedOperationException("Unsupported location for writing file: " + location);   //NOI18N
        }
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public int isSupportedOption(String option) {
        return -1;
    }


    private List<String> getResources(String folder) throws IOException {
        if (classPathContent == null) {
            classPathContent = new HashMap<>();
        }
        List<String> content = classPathContent.get(folder);
        if (content == null) {
            List<String> arr = new ArrayList<>();
            classPathContent.put(folder, arr);
            InputStream in = URLFileManager.class.getResourceAsStream("pkg." + folder.replace('/', '.'));
            if (in != null) {
                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                for (;;) {
                    String l = r.readLine();
                    if (l == null) {
                        break;
                    }
                    arr.add(l);
                }
                content = arr;
            }
        }
        return content == null ? Collections.<String>emptyList() : content;
    }

    public static void main(String... args) throws Exception {
        File dir = new File(args[0]);
        assert dir.isDirectory() : "Should be a directory " + dir;
        File libs = new File(args[1]);
        assert libs.isDirectory() : "Should be a directory " + libs;

        Map<String,List<String>> cntent = new HashMap<>();

        for (File f : libs.listFiles()) {
            if (f.canRead()) {
                if (f.isFile()) {
                    ZipFile zf = new ZipFile(f);
                    try {
                        Enumeration<? extends ZipEntry> entries = zf.entries();
                        while (entries.hasMoreElements()) {
                            ZipEntry e = entries.nextElement();
                            if (e.isDirectory()) {
                                continue;
                            }
                            final String name = e.getName();
                            final String owner = getOwner(name);
                            List<String> content = cntent.get(owner);
                            if (content == null) {
                                content = new ArrayList<>();
                                cntent.put(owner, content);
                            }
                            content.add(name);
                        }
                    } finally {
                        zf.close();
                    }
                } else if (f.isDirectory()) {
                    addFiles(f, "", cntent);
                }
            }
        }

        for (Map.Entry<String, List<String>> en : cntent.entrySet()) {
            String pkg = en.getKey();
            List<String> classes = en.getValue();
            File f = new File(dir, "pkg." + pkg.replace('/', '.'));
            FileWriter w = new FileWriter(f);
            for (String c : classes) {
                w.append(c).append("\n");
            }
            w.close();
        }
        try (FileWriter w = new FileWriter(new File(dir, "pkgs"))) {
            for (File f : libs.listFiles()) {
                w.write(f.getName());
                w.write("\n");
            }
        }
    }

    private static void addFiles(File folder, String path, Map<String,List<String>> into) {
        String prefix = path;
        if (!prefix.isEmpty()) {
            prefix = prefix + "/";  //NOI18N
        }
        for (File f : folder.listFiles()) {
            String fname = prefix + f.getName();
            if (f.isDirectory()) {
                addFiles(f, fname, into);
            } else {
                List<String> content = into.get(path);
                if (content == null) {
                    content = new ArrayList<>();
                    into.put(path, content);
                }
                content.add(fname);
            }
        }
    }

    private static Map<String,List<String>> classPathContent;

    private void register(Location loc, String resource, MemoryFileObject jfo) {
        Map<String,List<MemoryFileObject>> folders = generated.get(loc);
        final String folder = getOwner(resource);
        List<MemoryFileObject> content = folders.get(folder);
        if (content == null) {
            content = new ArrayList<>();
            folders.put(folder, content);
        }
        content.add(jfo);
    }

    private static String getOwner(String resource) {
        int lastSlash = resource.lastIndexOf('/');  //NOI18N
        assert lastSlash != 0;
        return lastSlash < 0 ?
            resource :
            resource.substring(0, lastSlash);
    }

    private static boolean canRead(Location loc) {
        for (Location rl : READ_LOCATIONS) {
            if (rl.equals(loc)) {
                return true;
            }
        }
        return false;
    }

    private static boolean canWrite(Location loc) {
        for (Location wl : WRITE_LOCATIONS) {
            if (wl.equals(loc)) {
                return true;
            }
        }
        return false;
    }

    private static boolean canClassLoad(Location loc) {
        for (Location cll : CLASS_LOADER_LOCATIONS) {
            if (cll.equals(loc)) {
                return true;
            }
        }
        return false;
    }

    static String convertFQNToResource(String fqn) {
        return fqn.replace('.', '/');   //NOI18N
    }

    static String convertResourceToFQN(String resource) {
        assert !resource.startsWith("/");    //NOI18N
        int lastSlash = resource.lastIndexOf('/');  //NOI18N
        int lastDot = resource.lastIndexOf('.');    //NOI18N
        if (lastSlash < lastDot) {
            resource = resource.substring(0, lastDot);
        }
        return resource.replace('/', '.');    //NOI18N
    }


    JavaFileObject createMemoryFileObject (String resourceName, JavaFileObject.Kind kind, byte[] content) {
        final BaseFileObject jfo  = new MemoryFileObject(resourceName, kind, content);
        return jfo;
    }

    Iterable<? extends MemoryFileObject> getGeneratedFiles(JavaFileObject.Kind... kinds) {
        final Set<JavaFileObject.Kind> ks = EnumSet.noneOf(JavaFileObject.Kind.class);
        Collections.addAll(ks, kinds);
        final List<MemoryFileObject> res = new ArrayList<>();
        for (Map<String,List<MemoryFileObject>> folders : generated.values()) {
            for (List<MemoryFileObject> content : folders.values()) {
                for (MemoryFileObject fo : content) {
                    if (ks.contains(fo.getKind()) && fo.getLastModified() >= 0) {
                        res.add(fo);
                    }
                }
            }
        }
        return res;
    }

    InputStream openInputStream(String path) throws IOException {
        byte[] arr = classPath.get(path);
        if (arr != null) {
            return new ByteArrayInputStream(arr);
        }
        throw new FileNotFoundException("Cannot find " + path + " in:\n" + classPath);
    }
    
    private static Map<String,byte[]> readAll() throws IOException {
        List<URL> path = new ArrayList<>();
        final InputStream pkgs = URLFileManager.class.getResourceAsStream("pkgs");
        if (pkgs == null) {
            return Collections.emptyMap();
        }
        try (BufferedReader r = new BufferedReader(new InputStreamReader(pkgs))) {
            for (;;) {
                String element = r.readLine();
                if (element == null) {
                    break;
                }
                final String resource = "com/dukescript/app/junitonline/libs/" + element;
                URL found = Compile.class.getClassLoader().getResource(resource);
                assert found != null : "ClassPath element " + element + " found as " + resource;
                path.add(found);
            }
        }
        Map<String,byte[]> classes = new HashMap<>();
        for (URL jar : path) {
            try (ZipInputStream zip = new ZipInputStream(jar.openStream())) {
                for (;;) {
                    ZipEntry entry = zip.getNextEntry();
                    if (entry == null) {
                        break;
                    }
                    final String name = entry.getName();
                    if (name.endsWith("/")) {
                        continue;
                    }
                    byte[] arr = new byte[4096];
                    int offset = 0;
                    for (;;) {
                        if (arr.length == offset) {
                            byte[] old = arr;
                            arr = new byte[arr.length * 2];
                            System.arraycopy(old, 0, arr, 0, old.length);
                        }
                        int remaining = arr.length - offset;
                        int read = zip.read(arr, offset, remaining);
                        if (read == -1) {
                            break;
                        }
                        offset += read;
                    }
                    byte[] copy = new byte[offset];
                    System.arraycopy(arr, 0, copy, 0, offset);
                    classes.put(name, copy);
                }
            }
        }
        return classes;
    }
}

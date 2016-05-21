package com.dukescript.app.junitonline.javac;

import java.net.URI;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;

/**
 *
 * @author Tomas Zezula
 */
public abstract class BaseFileObject implements JavaFileObject {

    protected final String path;
    protected final Kind kind;

    BaseFileObject(
        String path,
        Kind kind) {
        if (path.startsWith("/")) {    //NOI18N
            throw new IllegalArgumentException("Path cannot start with /"); //NOI18N
        }
        this.path = path;
        this.kind = kind;
    }


    public String infer() {
        return URLFileManager.convertResourceToFQN(path);
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    public boolean isNameCompatible(String simpleName, Kind kind) {
        return this.kind == kind && getSimpleName(path, true).equals(simpleName);
    }

    @Override
    public NestingKind getNestingKind() {
        return null;
    }

    @Override
    public Modifier getAccessLevel() {
        return null;
    }

    @Override
    public URI toUri() {
        return URI.create(escape(path));
    }

    @Override
    public String getName() {
        return path;
    }



    protected static String getSimpleName(String path, boolean removeExtension) {
        int slashIndex = path.lastIndexOf('/'); //NOI18N
        assert slashIndex != 0;
        String name;
        if (slashIndex < 0) {
            name = path;
        } else {
            assert slashIndex + 1 < path.length();
            name = path.substring(slashIndex + 1);
        }
        if (removeExtension) {
            int indx = name.lastIndexOf('.');
            if (indx > 0) {
                return name.substring(0, indx);
            }
        }
        return name;
    }

    protected static Kind getKind(final String path) {
        final String simpleName = getSimpleName(path, false);
        final int dotIndex = simpleName.lastIndexOf('.'); //NOI18N
        final String ext = dotIndex > 0 ?
            simpleName.substring(dotIndex) :
            "";
        for (Kind k : Kind.values()) {
            if (k.extension.equals(ext)) {
                return k;
            }
        }
        return Kind.OTHER;
    }

    private String escape(String path) {
        return path;
    }


}

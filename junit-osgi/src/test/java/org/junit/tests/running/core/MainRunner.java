package org.junit.tests.running.core;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.security.Permission;

public class MainRunner {
    private static class ExitException extends SecurityException {
        private static final long serialVersionUID = -9104651568237766642L;

        private final int status;

        public ExitException(int status) {
            super("");
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }

    /**
     * A {@code NoExitSecurityManager} throws a {@link ExitException} exception
     * whenever {@link #checkExit(int)} is called; all other permissions are allowed.
     */
    public class NoExitSecurityManager extends SecurityManager {

        @Override
        public void checkExit(int status) {
            throw new ExitException(status);
        }

        @Override
        public void checkPermission(Permission perm) {
            if (perm.getName().startsWith("exitVM")) {
                super.checkPermission(perm);
            }
        }
    }

    /**
     * Execute runnable.run(), preventing System.exit(). If System.exit() is called
     * in runnable.run(), the value is returned. If System.exit()
     * is not called, null is returned.
     *
     * @return null if System.exit() is not called, Integer.valueof(status) if not
     */
    public Integer runWithCheckForSystemExit(Runnable runnable) {
        Object oldSecurityManager = null;
        try {
            oldSecurityManager = installSecurityManager(null);
        } catch (UnsupportedOperationException | LinkageError err) {
            // cannot install security manager
        }
        PrintStream oldOut = System.out;

        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        try {
            runnable.run();
            System.out.println("System.exit() not called, return null");
            return null;
        } catch (ExitException e) {
            System.out.println("System.exit() called, value=" + e.getStatus());
            return e.getStatus();
        } finally {
            installSecurityManager(oldSecurityManager);
            System.setOut(oldOut);
        }
    }

    private Object installSecurityManager(Object sm) {
        SecurityManager osm = System.getSecurityManager();
        if (sm == null) {
            sm = new NoExitSecurityManager();
        }
        System.setSecurityManager((SecurityManager) sm);
        return osm;
    }
}

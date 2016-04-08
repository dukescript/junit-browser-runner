package net.java.html.junit;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import net.java.html.js.JavaScriptBody;
import net.java.html.js.JavaScriptResource;

@JavaScriptResource("jquery-2.2.2.min.js")
public class JQuery2_2_2 {

    @JavaScriptBody(args = {}, body = "")
    static native void init();

    @JavaScriptResource("setupUi.js")
    public static class SetupUI {
        private static String getImageURL(String name) {
            try {
                URL u = Bck2BrwsrTestRunner.class.getResource(name);
                URLConnection conn = u.openConnection();
                return conn.getURL().toExternalForm();
            } catch (IOException ex) {
            }
            return null;
        }

        public static void init() {
            init(
                getImageURL("document.png"),
                getImageURL("toggle-small-expand.png"),
                getImageURL("folder-horizontal.png"),
                getImageURL("toggle-small.png")
            );
        }

        private static void init(String i1, String i2, String i3, String i4) {
            JQuery2_2_2.init();
            init_impl(i1, i2, i3, i4);
        }

        @JavaScriptBody(args = { "i1", "i2", "i3", "i4" }, body = "window.junitui.setImageURLs(i1,i2,i3,i4)")
        public static native void init_impl(String i1, String i2, String i3, String i4);
        
    }

}

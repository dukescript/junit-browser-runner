package net.java.html.junit;

import net.java.html.js.JavaScriptBody;
import net.java.html.js.JavaScriptResource;

@JavaScriptResource("jquery-2.2.2.min.js")
public class JQuery2_2_2 {

    @JavaScriptBody(args = {}, body = "")
    static native void init();

    @JavaScriptResource("setupUi.js")
    public static class SetupUI {

        public static void init(String i1, String i2, String i3, String i4) {
            JQuery2_2_2.init();
            init_impl(i1, i2, i3, i4);
        }

        @JavaScriptBody(args = { "i1", "i2", "i3", "i4" }, body = "window.junitui.setImageURLs(i1,i2,i3,i4)")
        public static native void init_impl(String i1, String i2, String i3, String i4);
        
    }

}

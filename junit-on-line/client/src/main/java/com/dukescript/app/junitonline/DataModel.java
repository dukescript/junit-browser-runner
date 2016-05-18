package com.dukescript.app.junitonline;

import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.Property;
import com.dukescript.app.junitonline.js.Dialogs;

@Model(className = "Data", targetId="", properties = {
})
final class DataModel {
    private static Data ui;
    /**
     * Called when the page is ready.
     */
    static void onPageLoad() throws Exception {
        ui = new Data();
        ui.applyBindings();
    }
}

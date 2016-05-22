package com.dukescript.app.junitonline;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.java.html.json.Model;
import net.java.html.json.OnPropertyChange;
import net.java.html.json.Property;

@Model(className = "Data", targetId="", properties = {
})
final class DataModel {
    private static final Pattern FIND_CLASS = Pattern.compile("class\\W*(\\w+)\\W", Pattern.MULTILINE);
    private static Data ui;

    @Model(className = "Source", properties = {
        @Property(name = "name", type = String.class),
        @Property(name = "code", type = String.class),
    }, builder = "put")
    static final class SourceCntrl {
        @OnPropertyChange("code")
        static void assignNameIfNotPresent(Source model) {
            if (model.getName() == null) {
                final String className = findClassName(model.getCode());
                if (className != null) {
                    model.setName(className + ".java");
                }
            }
        }
        private static String findClassName(String code) {
            Matcher matcher = FIND_CLASS.matcher(code);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return null;
        }

    }

    /**
     * Called when the page is ready.
     */
    static void onPageLoad() throws Exception {
        ui = new Data();
        ui.applyBindings();
    }
}

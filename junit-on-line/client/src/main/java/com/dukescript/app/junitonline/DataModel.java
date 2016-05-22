package com.dukescript.app.junitonline;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.OnPropertyChange;
import net.java.html.json.Property;

@Model(className = "Data", targetId="", properties = {
    @Property(name = "sources", type = Source.class, array = true)
})
final class DataModel {
    private static final Pattern FIND_CLASS = Pattern.compile("class\\W*(\\w+)\\W", Pattern.MULTILINE);
    private static Data ui;

    @Model(className = "Source", properties = {
        @Property(name = "name", type = String.class),
        @Property(name = "code", type = String.class),
    }, builder = "put")
    static final class SourceCntrl {
        @OnPropertyChange("name")
        static void updateCode(Source model) {
            String className = model.getName();
            if (!className.endsWith(".java")) {
                return;
            } else {
                className = className.substring(0, className.length() - 5);
            }

            final String currentCode = model.getCode();
            if (currentCode != null) {
                int[] range = { 0, 0 };
                if (findClassName(currentCode, range) != null) {
                    String before = currentCode.substring(0, range[0]);
                    String after = currentCode.substring(range[1]);
                    model.setCode(before + className + after);
                }
            }
        }

        @OnPropertyChange("code")
        static void assignNameIfNotPresent(Source model) {
            if (model.getName() == null) {
                final String className = findClassName(model.getCode(), null);
                if (className != null) {
                    model.setName(className + ".java");
                }
            }
        }
        private static String findClassName(String code, int[] fromTo) {
            Matcher matcher = FIND_CLASS.matcher(code);
            if (matcher.find()) {
                if (fromTo != null) {
                    fromTo[0] = matcher.start(1);
                    fromTo[1] = matcher.end(1);
                }
                return matcher.group(1);
            }
            return null;
        }

    }

    @Function
    static void addSource(Data model) {
        int cnt = model.getSources().size() + 1;
        final String className = "Unknown" + cnt;
        model.getSources().add(new Source().putCode("public class " + className + " {\n\n}\n"));
    }

    @Function
    static void compileRun(Data model) {
    }

    /**
     * Called when the page is ready.
     */
    static void onPageLoad() throws Exception {
        ui = new Data();
        ui.getSources().add(new Source().putCode("public class Main {\n"
            + "  public int add(int one, int two) {\n"
            + "    return one + two;"
            + "  }\n"
            + "}\n"
            + ""
        ));
        ui.getSources().add(new Source().putCode("\n"
            + "import org.junit.Test;\n"
            + "import static org.junit.Assert.*;\n"
            + "public class MainTest {\n"
            + "  @Test\n"
            + "  public void onePlusOne() {\n"
            + "    Main main = new Main();\n"
            + "    int res = main.plus(1, 1);\n"
            + "    assertEquals(\"One plus one is three\", 3, res);\n"
            + "  }\n"
            + "}\n"
            + ""
        ));
        ui.applyBindings();
    }
}

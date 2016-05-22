package com.dukescript.app.junitonline;

import net.java.html.junit.BrowserRunner;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for behavior of your application in real systems. The {@link BrowserRunner}
 * selects all possible presenters from your <code>pom.xml</code> and
 * runs the tests inside of them.
 *
 * See your <code>pom.xml</code> dependency section for details.
 */
@RunWith(BrowserRunner.class)
public class DataModelTest {
    @Test public void testUIModelWithoutUI() {
        Data model = new Data();
    }

    @Test
    public void sourceInitializedAndChanged() {
        Source src = new Source().putCode("\n\nclass MyTest {\n\n}\n");
        assertEquals("MyTest.java", src.getName());
        src.putName("BetterTest.java");
        assertEquals("\n\nclass BetterTest {\n\n}\n", src.getCode());
    }
}

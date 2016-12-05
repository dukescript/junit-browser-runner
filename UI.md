Design Your own JUnit Runner UI
===============================

[JUnit Browser Runner](README.md) allows anyone to design own UI to display progress of
test execution in a browser and to re-run already executed tests. Designing such UI is
easy and requires just two bits of knowledge: know what is `RunListener` (a standard
JUnit class to observe progress of test execution) and know what is `HTML` (so one
create the web page and describe the UI). Here is an example:

* [JUnitChartsUI.html](charts-runner/src/main/resources/com/dukescript/junit/chartsui/JUnitChartsUI.html)
* [JUnitChartsUI.java](https://github.com/dukescript/junit-browser-runner/blob/master/charts-runner/src/main/java/com/dukescript/junit/chartsui/JUnitChartsUI.java)

The HTML page and its associated `.class` file need to be named with the same prefix and
packaged in the same package of the same `JAR` file. The class needs to be registered using 
the `java.util.ServiceLoader` mechanism (in the example done by using `@ServiceProvider` annotation).

Once [JUnit Browser Runner](README.md) (version `0.8` or newer) is started, it uses `ServiceLoader` to discover registered
`RunListener` implementations with appropriate HTML page. It instantiates the first one and registers
it as a listener to current JUnit execution. It is up to the listener to update the UI accordingly.

The easiest way to start is to modify/copy the [charts-runner project](charts-runner) and use for example [Java JQuery API](https://dukescript.com/javadoc/libs/net/java/html/lib/jquery/Exports.html):

```java
import static net.java.html.lib.jquery.Exports.$;

@ServiceProvider(service = RunListener.class)
public final class JUnitChartsUI extends RunListener {
    @Override
    public void testStarted(final Description description) throws Exception {
        $("ui").text("Running " + description.getMethodName());
    }
}
```

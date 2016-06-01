JUnit Browser Runner
====================

JUnit Browser Runner is the easiest way to execute your (not only) [DukeScript](http://dukescript.com) based 
code in various testing environments. Simply annotate your test with `@RunWith` annotation and the runner will
handle the rest:

```java
@RunWith(BrowserRunner.class)
public class CodeTest {
  @Test
  public void mathMin() {
    int min = Math.min(3, 5);
    assertEquals("Three is lower", 3, min);
  }
}
```

The code looks like a regular [JUnit](http://junit.org) testing code - one doesn't have to learn any special concepts.
Yet, it is the easiest way to run your tests in browser (without any Java plugin), or test your mixed 
[Java/JavaScript code](http://bits.netbeans.org/html+java/1.3/net/java/html/js/package-summary.html) inside of
**JavaFX** web view or inside of **Nashorn** scripting engine.

Getting Started
---------------

The easiest way to get a working environment is to follow the steps described in [the tutorial](http://dukescript.com/best/practices/2016/05/30/test-in-browser.html) - e.g. generate the skeletal project from a [Maven](http://maven.apache.org) archetype:

```bash
$ mvn archetype:generate \
  -DarchetypeGroupId=com.dukescript.archetype \
  -DarchetypeArtifactId=knockout4j-archetype \
  -DarchetypeVersion=0.13 \
  -Dwebpath=client-web \
  -DgroupId=org.your.test \
  -DartifactId=yesican \
  -Dversion=1.0-SNAPSHOT
$ cd yesican
$ mvn install
```

The above creates necessary files and executes the `client/src/test/java/org/your/test/DataModelTest.java` test in  a browser. You can edit the file and re-run the tests then.

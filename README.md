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
[Java/JavaScript code](http://bits.netbeans.org/html+java/1.4/net/java/html/js/package-summary.html) inside of
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

Configuring
-----------

The **JUnit Browser Runner** is configured via `java.util.ServiceLoader` mechanism - by including more libraries on classpath of your test, you enable more *test runs*. That is why you should take a look at your `pom.xml` file if you want to configure your runner. For example, to enable **Nashorn** from **JDK8** you can add following dependency:

```xml
<dependency>
  <groupId>org.netbeans.html</groupId>
  <artifactId>net.java.html.boot.script</artifactId>
  <version>1.4</version>
</dependency>
```

If you want to run your tests in a **JavaFX** web view, make sure following dependency is added:

```xml
<dependency>
  <groupId>org.netbeans.html</groupId>
  <artifactId>net.java.html.boot.fx</artifactId>
  <version>1.4</version>
</dependency>
```

and finally, if you include necessary [Bck2Brwsr VM](http://bck2brwsr.apidesign.org) libraries, the runner also converts your code to JavaScript and executes it directly in a browser:

```xml
<!-- Bck2Brwsr VM presenter for BrowserRunner -->
<dependency>
  <groupId>org.apidesign.bck2brwsr</groupId>
  <artifactId>launcher.http</artifactId>
  <version>0.20</version>
</dependency>
<dependency>
  <groupId>org.apidesign.bck2brwsr</groupId>
  <artifactId>ko-bck2brwsr</artifactId>
  <version>0.20</version>
  <classifier>bck2brwsr</classifier>
</dependency>
<dependency>
    <groupId>com.dukescript.api</groupId>
    <artifactId>junit-browser-runner</artifactId>
    <version>1.0</version>
    <classifier>bck2brwsr</classifier>
</dependency>
<!-- End of Bck2Brwsr VM presenter for BrowserRunner -->
```

Other implementations of [Fn.Presenter](http://bits.netbeans.org/html+java/1.4/org/netbeans/html/boot/spi/Fn.Presenter.html) are also automatically recognized, so you can include them on the classpath and the runner picks them up.

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

package kr.re.dev.MoongleDic.DicData;

import org.junit.Test;
import static kr.re.dev.MoongleDic.Commons.Invoker.invoke;
import static kr.re.dev.MoongleDic.Commons.Invoker.invokeStatic;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by ice3x2 on 15. 4. 29..
 */
public class InvokerTest {


    private static class Foo {

        private static void executeA() {}
        private static void executeA(int a, Integer b) {}
        private static String executeA(Object C) {
            return C.toString();
        }
        private static String executeA(String C) {
            return C.toString() + "OK";
        }

        private static double executeA(int c, double b) {
            return b;
        }

        private int sum(int a, int b) {
            return (a + b) * 2;
        }
        private String execute(Object a, String b) {
            return b + a + b;
        }
        private String execute(String a, Object b) {
            return a + b;
        }
        private String execute(String ...  a) {
            StringBuilder builder = new StringBuilder();
            for(String s : a) {
                builder.append(s);
            }
            return builder.toString();
        }
    }


    @Test
    public void testInvoke() throws Exception {
        Foo test =  mock(Foo.class);
        assertEquals("OKOK", invokeStatic(Foo.class, "executeA", new StringBuilder().append("OKOK")));
        invokeStatic(Foo.class, "executeA");
        invokeStatic(Foo.class, "executeA", 10, 100);
        assertEquals("OKOK", invokeStatic(Foo.class, "executeA", "OK"));
        assertEquals(100.0, invokeStatic(Foo.class, "executeA", 10, (double) 100.0f));

        assertEquals(600, invoke(test, "sum", 100, 200));
        assertEquals("OK--", invoke(test, "execute", "OK", new StringBuilder().append("--")));
        assertEquals("OK--OK", invoke(test, "execute",  new StringBuilder().append("--"), "OK"));
    }

}
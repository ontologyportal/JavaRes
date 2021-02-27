package atp;

/**
 * Created by apease on 2/16/18 from https://stackoverflow.com/questions/9288107/run-single-test-from-a-junit-class-using-command-line
 * also https://android.googlesource.com/platform/cts/+/0228105/tools/junit/src/com/android/cts/junit/SingleJUnitTestRunner.java
 *  Usage:
 * java -cp path/to/testclasses:path/to/junit-4.8.2.jar SingleJUnitTestRunner
 com.mycompany.product.MyTest#testB

 */

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;

public class SingleJUnitTestRunner {

    private static String mUsage = "Usage: java -cp <classpath> SingleJUnitTestRunner" +
            " class#testmethod";
    private static final String PASSED_TEST_MARKER = "[ PASSED ]";
    private static final String FAILED_TEST_MARKER = "[ FAILED ]";

    /** *************************************************************
     */

    public static void main(String... args) throws ClassNotFoundException {

        if (args.length != 1)
            throw new IllegalArgumentException(mUsage);
        String[] classAndMethod = args[0].split("#");
        if (classAndMethod.length != 2)
            throw new IllegalArgumentException(mUsage);
        Request request = Request.method(Class.forName(classAndMethod[0]), classAndMethod[1]);
        JUnitCore jUnitCore = new JUnitCore();
        Result result = jUnitCore.run(request);
        String status = result.wasSuccessful() ? PASSED_TEST_MARKER : FAILED_TEST_MARKER;
        System.out.println(String.format("%s %s.%s", status, classAndMethod[0], classAndMethod[1]));
    }
}

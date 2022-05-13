package tests;

import edu.kit.kastel.checker.exclusivity.ExclusivityChecker;
import org.checkerframework.framework.test.CheckerFrameworkPerDirectoryTest;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.util.List;

/**
 * Test runner for tests of the Exclusivity Checker.
 *
 * <p>Tests appear as Java files in the {@code tests/exclusivity} folder. To add a new test case,
 * create a Java file in that directory. The file contains "// ::" comments to indicate expected
 * errors and warnings; see
 * https://github.com/typetools/checker-framework/blob/master/checker/tests/README .
 */
public class MethodInvocationTest extends CheckerFrameworkPerDirectoryTest {
    public MethodInvocationTest(List<File> testFiles) {
        super(
                testFiles,
                ExclusivityChecker.class,
                "methodinvocation",
                "-Anomsgtext",
                "-Astubs=stubs/",
                "-nowarn");
    }

    @Parameters
    public static String[] getTestDirs() {
        return new String[] {"methodinvocation"};
    }
}

package yhames.pro.project.afis;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.Thread;

import yhames.pro.project.afis.matchers.*;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    private Fingerprint fp;
    private static final String testDir = "src/test/java/yhames/pro/project/afis/";
    private static Thread server;

    public AppTest() {
        try {
            Path fileName = Path.of("resources/fingerprints/LeftIndex.bmp");
            fp = new Fingerprint(Files.readAllBytes(fileName));
        }
        catch (IOException e) {
            System.err.println("Unable to find the reference image");
            fail();
        }
    }

    @BeforeClass
    public static void setUp() {
        // Start server thread on 6969
        server = new Thread(new MatchServer());
        server.start();
    }

    @AfterClass
    public static void tearDown() {
        try {
            MatchServer.stop();
        }
        catch (IOException e) {
            System.err.println("Error while stopping server");
        }
    }

    @Test
    public void testDatabaseDownload() {
        DatabaseConnection db = new DatabaseConnection();

        byte[] expected = fp.getImg();
        byte[] actual = db.getFingerprints()[0].getImg();
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    /*
        Tests searching the database for a match
    */
    public void testDatabaseSearch() {
       Match match = new SourceAFIS().search(fp, new Fingerprint[] {fp});

       System.out.println("Match Score: " + match.getScore());

       Assert.assertTrue(match.isMatch());
    }

    @Test
    /*
        Tests the enrolling of new fingerprints
    */
    public void testEnroll() {
        boolean result = new DatabaseConnection().enroll(fp);
        Assert.assertTrue(result);
    }
    /*
        Test is 2 images which should NOT be a match
        are detected as such
    */
    @Test
    public void testMatching() {
        try {
            // get a fingerprint from a different finger
            Fingerprint test = new Fingerprint(
                Files.readAllBytes(Path.of("resources/fingerprints/LeftHand/LeftMiddle.bmp"))
            );
            Match match = new SourceAFIS().match(test, fp);

            System.out.println("Match Score: " + match.getScore());

            Assert.assertFalse(match.isMatch());
        }
        catch (IOException e) {
            fail();
        }
    }

    @Test
    /*
        Tests the MatchServer by calling a python script which simulates a client
    */
    public void testServer() {
        final int iterations = 2;

        int exitCode = runPythonScript(
            testDir + "MatchServerTester.py",
            iterations
        );

        Assert.assertEquals(0, exitCode);
    }

    @Test
    public void testHandshake() {
        int exitCode = runPythonScript(
                testDir + "TestHandshake.py",
                5
        );

        Assert.assertEquals(0, exitCode);

    }

    // Returns exit code of python program
    private int runPythonScript(String path, int iterations) {
        // Call python script
        ProcessBuilder processBuilder = new ProcessBuilder("python3", path, String.valueOf(iterations));
        processBuilder.redirectOutput();
    
        try {
            return processBuilder
                .start()
                .waitFor();
        }
        catch (Exception e) {
            return 1; 
        }
    }
}

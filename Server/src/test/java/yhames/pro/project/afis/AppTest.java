package yhames.pro.project.afis;

import org.junit.Assert;
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
    private Path fileName; 
    private Fingerprint fp; 

    public AppTest() {
        try {
           fileName = Path.of("/Users/james/ownCloud/University/Masters/Project/Code/Stable/Server/resources/fingerprints/LeftIndex.bmp");  
           fp = new Fingerprint(Files.readAllBytes(fileName));
        }
        catch (IOException e) {
            System.err.println("Unable to find the reference image");
            fail();
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
                Files.readAllBytes(Path.of("/Users/james/ownCloud/University/Masters/Project/Code/Stable/Server/resources/fingerprints/LeftHand/LeftMiddle.bmp"))
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
        final int iterations = 10;

        // Start the server in its own thread
        Thread t = new Thread(() -> {
            MatchServer server = new MatchServer();
            server.start(6969);
        });
        t.start();

        int exitCode = runPythonScript(
            "/Users/james/ownCloud/University/Masters/Project/Code/Stable/Server/src/test/java/yhames/pro/project/afis/MatchServerTester.py", 
            iterations
        );

        // Kill the server thread
        t.interrupt();        

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

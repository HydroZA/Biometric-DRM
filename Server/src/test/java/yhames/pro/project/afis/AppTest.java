package yhames.pro.project.afis;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
        Assert.assertArrayEquals(actual, expected);
    }

    @Test
    /*
        Tests if 2 images which should be a match
        are detected as such
    */
    public void testMatchingMatch() {
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
    public void testMatchingNoMatch() {
        fail();
    }
}

package yhames.pro.project.afis;

import org.junit.Assert;
import org.junit.Test;
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

    AppTest() {
        try {
           fileName = Path.of("fingerprints/LeftIndex.b64");  
           fp = new Fingerprint(Files.readString(fileName));
        }
        catch (IOException e) {
            System.err.println("Unable to find the reference b64 img");
        }
    }

    @Test
    public void testBase64Decode() {
        try {
            byte[] expected = Files.readAllBytes(Path.of("fingerprints/LeftIndex.bmp"));
            byte[] actual = Base64Decoder.decode(fp.getImg());

            Assert.assertEquals(expected, actual);
        }
        catch (IOException e) {
            System.err.println("Unable to find the reference b64 img");
        }   
    }

    @Test
    public void testDatabaseDownload() {
        DatabaseConnection db = new DatabaseConnection();

        Assert.assertEquals(db.getFingerprints()[0], fp.getImg());
    }

    @Test
    /*
        Tests if 2 images which should be a match
        are detected as such
    */
    public void testMatchingMatch() {
        Assert.assertTrue(new SourceAFIS().match(fp));
    }


    /*
        Test is 2 images which should NOT be a match
        are detected as such
    */
    @Test
    public void testMatchingNoMatch() {
        // TODO:
        Assert.assertTrue(true);
    }
}

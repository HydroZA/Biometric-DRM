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
    private String fp; 

    public AppTest() {
        try {
           fileName = Path.of("/Users/james/ownCloud/University/Masters/Project/Code/Stable/Server/resources/fingerprints/LeftIndex.b64");  
           fp = Files.readString(fileName);
        }
        catch (IOException e) {
            System.err.println("Unable to find the reference b64 img");
            fail();
        }
    }

    @Test
    public void testBase64Decode() {
        try {
            byte[] expected = Files.readAllBytes(Path.of("/Users/james/ownCloud/University/Masters/Project/Code/Stable/Server/resources/fingerprints/LeftIndex.bmp"));
            byte[] actual = Base64Decoder.decode(fp);

            Assert.assertEquals(expected, actual);
        }
        catch (IOException e) {
            System.err.println("Unable to find the reference b64 img");
            fail();
        }   
    }

    @Test
    public void testDatabaseDownload() {
        DatabaseConnection db = new DatabaseConnection();

        try {
            byte[] actual = Files.readAllBytes(Path.of("/Users/james/ownCloud/University/Masters/Project/Code/Stable/Server/resources/fingerprints/LeftIndex.bmp"));
            Assert.assertArrayEquals(db.getFingerprints()[0].getImg(), actual);
        }
        catch (IOException e) {
            fail();
        }
        
    }

    @Test
    /*
        Tests if 2 images which should be a match
        are detected as such
    */
    public void testMatchingMatch() {
       // Assert.assertTrue(new SourceAFIS().match(fp));
       Assert.assertTrue(false);
    }


    /*
        Test is 2 images which should NOT be a match
        are detected as such
    */
    @Test
    public void testMatchingNoMatch() {
        Assert.assertTrue(false);
    }
}

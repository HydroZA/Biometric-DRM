/*
    Listens for incoming TCP connections from client app
    
    client sends us a base64 encoded image along with a requested matching method
    
    we match the image according to their requested method against a database 
    of b64 encoded images

*/

package yhames.pro.project.afis;

import com.machinezoo.sourceafis.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App 
{
    public byte[] decodeBase64(String b64) {
        throw new UnsupportedOperationException();
    }

    /*
        @return: A array of b64 encoded images representing candidate fingerprints
    */
    public String[] getCandidates() {
        throw new UnsupportedOperationException();
    }

    public static void main( String[] args )
    {
        String img1 = args[0];
        String img2 = args[1];

        try {
            FingerprintTemplate probe = new FingerprintTemplate(
                new FingerprintImage(
                    Files.readAllBytes(Paths.get(img1)),
                    new FingerprintImageOptions()
                        .dpi(500)));
            
            FingerprintTemplate candidate = new FingerprintTemplate(
                new FingerprintImage(
                    Files.readAllBytes(Paths.get(img2)),
                    new FingerprintImageOptions()
                        .dpi(500)));
            
            double score = new FingerprintMatcher(probe).match(candidate);
            System.out.println(score);
        }
        catch (Exception e) {
            System.out.println("Crashed fam");
        }
    }
}

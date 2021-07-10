package yhames.pro.project.afis;

import java.io.InputStream;
import java.nio.ByteBuffer;

public class MatchRequest {
    private ComparisonMethod method;
    private Fingerprint probe;

    public MatchRequest (ComparisonMethod m, Fingerprint fp) {
        this.method = m;
        this.probe = fp;
    }

    public ComparisonMethod getMethod() {
        return method;
    }

    public void setMethod(ComparisonMethod method) {
        this.method = method;
    }

    public Fingerprint getProbe() {
        return probe;
    }

    public void setProbe(Fingerprint probe) {
        this.probe = probe;
    }

        /* Clients request matches in the following format:
    |--------|------------------------|--------------|
    1 byte            |  4 bytes      | n bytes
    method            |  body length  | body
    0x00 = sourceafis |               |
    0x01 = ssim       |
    0x02 = mse        |
    */
    public static MatchRequest read(InputStream in) {
        try {
            // Read the method
            ComparisonMethod method = ComparisonMethod.values()[in.readNBytes(1)[0]];

            // Read the message length
            byte[] lenBytes = in.readNBytes(4);
            int len = ByteBuffer.wrap(lenBytes).getInt();

            // Read the image body
            byte[] img = in.readNBytes(len);

            return new MatchRequest(method, new Fingerprint(img));
        }
        catch (Exception e) {
            System.err.println("Error while reading match request, continuing...");
            return null;
        }
    }
}

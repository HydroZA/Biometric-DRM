package yhames.pro.project.afis.responses;

import yhames.pro.project.afis.requests.EnrollRequest;

import java.io.IOException;
import java.io.OutputStream;

public class EnrollResponse extends Response {
    private boolean result;
    public EnrollResponse(boolean result) {
        this.result = result;
    }

    @Override
    public boolean send(OutputStream out) throws IOException {
        if (result) {
            out.write(new byte[] { 0x00});
        }
        else {
            out.write(new byte[] { 0x01 });
        }
        return result;
    }
}

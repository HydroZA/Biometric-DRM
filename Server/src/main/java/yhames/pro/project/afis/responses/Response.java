package yhames.pro.project.afis.responses;

import java.io.OutputStream;

public abstract class Response {
    public abstract boolean send(OutputStream out);
}

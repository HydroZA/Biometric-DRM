package yhames.pro.project.server.responses;

import java.io.IOException;
import java.io.OutputStream;

public abstract class Response {
    public abstract boolean send(OutputStream out) throws IOException;
}

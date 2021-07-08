/*
    Responsible for listening for incoming MatchRequests for clients

    should return these match requests to who ever owns its object

    sockets should close once the match request is complete
*/

package yhames.pro.project.afis;

import yhames.pro.project.afis.*;
import yhames.pro.project.afis.matchers.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;

public class MatchServer 
{
    private boolean stopped = false;

    public static void main( String[] args )
    {
        new MatchServer().start(6969);
    }

    private void start(int port) {
        try {
            ServerSocket server = new ServerSocket(port);
            System.out.println("Server Listening on " + port);

            while (!stopped) {
                Socket client = server.accept();
                handleClient(client);
                client.close();
            }
            server.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } 
    }


    private void handleClient(Socket client) {
        System.out.println("Got client: " + client.toString());

        try {
            InputStream in = client.getInputStream();
            OutputStream out = client.getOutputStream();

            MatchRequest matchRequest = readMatchRequest(in);
            Match result = matchClient(matchRequest);
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private Match matchClient(MatchRequest matchRequest) {
        switch (matchRequest.getMethod()) {
            case SOURCE_AFIS:
                System.out.println("SourceAFIS Method chosen by Client");
            case MSE:
                System.out.println("Mean Squared Error Method chosen by Client");
            case SSIM:
                System.out.println("Structural Similarity Index Method chosen by client");
        }
        return null;
    }

    /* Clients request matches in the following format:
    |--------|------------------------|--------------|
    1 byte            |  2 bytes      | n bytes
    method            |  body length  | body
    0x00 = sourceafis |               |
    0x01 = ssim       |
    0x02 = mse        |
    */
    private MatchRequest readMatchRequest(InputStream in) {
        try {
            // Read the method
            ComparisonMethod method = ComparisonMethod.values()[in.readNBytes(1)[0]];

            // Read the message length
            byte[] lenBytes = in.readNBytes(2);
            int len = lenBytes[0] + lenBytes[1];

            // Read the image body
            byte[] img = in.readNBytes(len);

            return new MatchRequest(method, new Fingerprint(img));
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
}

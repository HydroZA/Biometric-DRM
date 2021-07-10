/*
    Responsible for listening for incoming MatchRequests for clients

    should return these match requests to who ever owns its object

    sockets should close once the match request is complete
*/

package yhames.pro.project.afis;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import yhames.pro.project.afis.matchers.Match;
import yhames.pro.project.afis.matchers.SourceAFIS;

public class MatchServer 
{
    private boolean stopped = false;

    public static void main( String[] args )
    {
        new MatchServer().start(6969);
    }

    public void start(int port) {
        try {
            ServerSocket server = new ServerSocket(port);
            System.out.println("Server Listening on " + port);

            while (!stopped) {
                Socket client = server.accept();
                System.out.println("Got client: " + client.toString());

                handleClient(client);

                System.out.println("Connection with: " + client + "complete. Terminating Connection...");
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
        try {
            InputStream in = client.getInputStream();
            OutputStream out = client.getOutputStream();

            MatchRequest matchRequest = MatchRequest.read(in);

            if (matchRequest == null) {
                return;
            }

            Match result = handleMatching(matchRequest);
            
            if (result.isMatch()) {
                System.out.println("MatchRequest succeeded with score: " + result.getScore());
            }
            else {
                System.out.println("MatchRequest failed with score: " + result.getScore());
            }

            // Respond to the client
            MatchResponse response = new MatchResponse(result);
            if (response.send(out)) {
                System.out.println("Responded to the client successfully");
            }
            else {
                System.err.println("Failed to respond to the client");
            }
        }
        catch (Exception e) {
            System.err.println("Error while handling client, continuing...");
        }
    }

    private Match handleMatching(MatchRequest matchRequest) {
        DatabaseConnection db = new DatabaseConnection();

        switch (matchRequest.getMethod()) {
            case SOURCE_AFIS -> {
                System.out.println("SourceAFIS Method chosen by Client");
                return new SourceAFIS()
                        .search(
                                matchRequest.getProbe(),
                                db.getFingerprints()
                        );
            }
            case MSE -> System.out.println("Mean Squared Error Method chosen by Client");
            case SSIM -> System.out.println("Structural Similarity Index Method chosen by client");
        }
        return new Match(false);
    }
}

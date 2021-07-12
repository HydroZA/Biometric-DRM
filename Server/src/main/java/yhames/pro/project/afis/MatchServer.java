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

import yhames.pro.project.afis.requests.*;

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

                boolean result = handleClient(client);
                if (result) {
                    System.out.println("Client Request was Handled Successfully");
                } else {
                    System.out.println(("Something went wrong during client's request"));
                }

                System.out.println("Connection with: " + client + " complete. Terminating Connection...");
                client.close();
            }
            server.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } 
    }

    private boolean handleClient(Socket client) {
        try {
            InputStream in = client.getInputStream();
            OutputStream out = client.getOutputStream();

            // Request will be the relevant polymorphic type of Result
            Request result = Request.read(in);

            // The request is handled by the relevant type of request
            if (result != null) {
                return result.handle(out);
            }
            else {
                throw new Exception();
            }
        }
        catch (Exception e) {
            return false;
        }
    }
}

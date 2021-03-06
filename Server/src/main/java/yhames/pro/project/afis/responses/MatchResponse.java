package yhames.pro.project.afis.responses;

import yhames.pro.project.afis.matchers.Match;
import yhames.pro.project.afis.requests.MatchRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class MatchResponse extends Response {
    private Match match;

    public MatchResponse(Match mr) {
        this.match = mr;
    }

    /*
        MatchResponses Take the Following format:

        |-----------|-----------|---------------|
        1 byte      | 4 bytes   | n bytes
        isMatch     | msg length| msg body
        boolean     |           |
        0x00 = true |           |
        0x01 = false|           |

        NOTE: If isMatch is false then the response will only be a single byte
        If there is a match then the matched image from the database is sent to
        the client
     */
    private byte[] serialize() {
        if (match.isMatch()) {
            ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
            int imgLen = this.match.getFingerprint().getImg().length;
            lengthBuffer.putInt(imgLen);
            byte[] lengthBytes = lengthBuffer.array();

            // isMatch + length + img = size of whole message
            byte[] serialized = new byte[1 + 4 + imgLen];

            // Add 0x00 indicating a good match
            serialized[0] = 0x00;

            // Add the length of the image
            System.arraycopy(lengthBytes, 0, serialized, 1, lengthBytes.length);

            // Add the image
            System.arraycopy(this.match.getFingerprint().getImg(),
                    0,
                    serialized,
                    5,
                    imgLen
            );

            return serialized;
        }
        else {
            return new byte[] {
                    0x01
            };
        }
    }

    // Returns a boolean indicating success
    public boolean send(OutputStream out) throws IOException{
        byte[] matchResponseSerialized = this.serialize();
        out.write(matchResponseSerialized);
        return true;
    }
}

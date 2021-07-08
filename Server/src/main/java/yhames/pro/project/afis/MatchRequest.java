package yhames.pro.project.afis;

/* Clients request matches in the following format:
    |--------|------------------------|--------------|
    1 byte            |  2 bytes      | n bytes
    method            |  body length  | body
    0x00 = sourceafis |               |
    0x01 = ssim       |
    0x02 = mse        |
    
*/

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
}

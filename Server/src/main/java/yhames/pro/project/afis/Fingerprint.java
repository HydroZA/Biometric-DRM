package yhames.pro.project.afis;

public class Fingerprint {
    int id;
    byte[] img;
    
    public Fingerprint(int id, byte[]  img) {
        this.id = id;
        this.img = img;
    }

    public Fingerprint(byte[] img) {
        this.img = img;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getImg() {
        return img;
    }

    public void setImg(byte[] img) {
        this.img = img;
    }
    
}

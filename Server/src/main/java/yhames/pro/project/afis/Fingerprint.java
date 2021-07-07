package yhames.pro.project.afis;

public class Fingerprint {
    int id;
    String img;
    
    public Fingerprint(int id, String img) {
        this.id = id;
        this.img = img;
    }

    public Fingerprint(String img) {
        this.img = img;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
    
}

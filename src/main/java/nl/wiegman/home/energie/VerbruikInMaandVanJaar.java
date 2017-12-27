package nl.wiegman.home.energie;

public class VerbruikInMaandVanJaar extends VerbruikDto {

    private int maand; // Range: 1 .. 12

    public int getMaand() {
        return maand;
    }

    public void setMaand(int maand) {
        this.maand = maand;
    }
}

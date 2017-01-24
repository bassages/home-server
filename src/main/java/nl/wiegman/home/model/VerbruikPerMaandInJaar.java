package nl.wiegman.home.model;

public class VerbruikPerMaandInJaar extends Verbruik {

    private int maand; // Range: 1 .. 12

    public int getMaand() {
        return maand;
    }

    public void setMaand(int maand) {
        this.maand = maand;
    }
}

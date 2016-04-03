package nl.wiegman.home.model;

public class VerbruikPerMaandInJaar extends Verbruik {

    // Range: 1 .. 12
    private int maand;

    public int getMaand() {
        return maand;
    }

    public void setMaand(int maand) {
        this.maand = maand;
    }
}

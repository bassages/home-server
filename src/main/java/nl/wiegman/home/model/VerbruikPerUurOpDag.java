package nl.wiegman.home.model;

public class VerbruikPerUurOpDag extends Verbruik {

    private int uur; // Range: 0 - 23

    public int getUur() {
        return uur;
    }

    public void setUur(int uur) {
        this.uur = uur;
    }
}

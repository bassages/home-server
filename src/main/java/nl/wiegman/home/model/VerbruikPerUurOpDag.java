package nl.wiegman.home.model;

public class VerbruikPerUurOpDag extends Verbruik {

    // Range: 0 - 23
    int uur;

    public int getUur() {
        return uur;
    }

    public void setUur(int uur) {
        this.uur = uur;
    }
}

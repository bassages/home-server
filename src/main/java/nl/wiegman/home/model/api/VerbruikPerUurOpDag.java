package nl.wiegman.home.model.api;

public class VerbruikPerUurOpDag extends Verbruik {

    // 0 - 23
    int uur;

    public int getUur() {
        return uur;
    }

    public void setUur(int uur) {
        this.uur = uur;
    }
}

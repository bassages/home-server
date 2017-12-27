package nl.wiegman.home.energie;

public class VerbruikInUurOpDag extends VerbruikDto {

    private int uur; // Range: 0 - 23

    public int getUur() {
        return uur;
    }

    public void setUur(int uur) {
        this.uur = uur;
    }
}

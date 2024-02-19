package rs.ac.bg.etf.predictor;

public class BHR {

    private int registar;
    private int mask;

    public BHR(int size) {
        if (size > 32) {
            throw new IllegalArgumentException();
        }
        mask = (1 << size) - 1;
        registar = 0;
    }

    public int getValue() {
        return registar & mask;
    }

    public void insertOutcome(boolean outcome) {
        registar = (registar << 1) | (outcome ? 1 : 0);
    }

}

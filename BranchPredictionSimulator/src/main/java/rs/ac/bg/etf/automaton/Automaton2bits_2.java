package rs.ac.bg.etf.automaton;

public class Automaton2bits_2 implements Automaton {

    private int state;

    Automaton2bits_2(int state) {
        this.state = state;
    }

    Automaton2bits_2() {
        this(0);
    }

    @Override
    public void updateAutomaton(boolean outcome) {
        if (outcome) {
            if (state < 3) {
                state++;
            }
        } else {
            if (state > 0) {
                state--;
            }
        }
    }

    @Override
    public boolean predict() {
        return state >= 2;
    }

}

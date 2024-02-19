package rs.ac.bg.etf.automaton;



public interface Automaton {
    enum AutomatonType {ONEBIT, TWOBITS_TYPE1, TWOBITS_TYPE2, TWOBITS_TYPE3, TWOBITS_TYPE4}

    static Automaton instance(AutomatonType type) {
        switch (type) {
            case ONEBIT: return new Automaton1bit();
            case TWOBITS_TYPE1: return new Automaton2bits_1();
            case TWOBITS_TYPE2: return new Automaton2bits_2();
            case TWOBITS_TYPE3: return new Automaton2bits_3();
            case TWOBITS_TYPE4: return new Automaton2bits_4();
        }
        return null;
    }

    static Automaton[] instanceArray(AutomatonType type,int n)
    {
        Automaton[] arr = new Automaton[n];
        for (int i = 0; i < n; i++) {
            arr[i] = instance(type);
        }
        return arr;
    }

    void updateAutomaton(boolean outcome);

    boolean predict();

}

package rs.ac.bg.etf.predictor.correlation;

import rs.ac.bg.etf.automaton.Automaton;
import rs.ac.bg.etf.automaton.Automaton.AutomatonType;
import rs.ac.bg.etf.predictor.BHR;
import rs.ac.bg.etf.predictor.Instruction;
import rs.ac.bg.etf.predictor.Predictor;

public class Correlation implements Predictor {

    Automaton[][] automatons;
    BHR bhr;
    int mask;

    String signature;
    
    public Correlation(int BHRsize, int numOfLastBitsAddress, AutomatonType type){
    	signature="Correlation "+BHRsize+" "+numOfLastBitsAddress+" "+type+" ";
        bhr = new BHR(BHRsize);
        int cntRows = 1<<BHRsize;
        int rowSize = 1<<numOfLastBitsAddress;
        automatons = new Automaton[cntRows][];
        for (int i = 0; i < cntRows; i++) {
            automatons[i] = Automaton.instanceArray(type,rowSize);
        }
        mask = (1<<numOfLastBitsAddress)-1;
    }

    @Override
    public boolean predict(Instruction branch) {
        return automatons[bhr.getValue()][(int) (branch.getAddress() & mask)].predict();
    }

    @Override
    public void update(Instruction branch) {
        boolean outcome=branch.isTaken();
        automatons[bhr.getValue()][(int) (branch.getAddress() & mask)].updateAutomaton(outcome);
        bhr.insertOutcome(outcome);
    }
    
    @Override
    public String toString() {
    	return signature;
    }
}

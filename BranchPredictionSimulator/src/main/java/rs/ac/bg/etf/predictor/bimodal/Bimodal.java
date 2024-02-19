package rs.ac.bg.etf.predictor.bimodal;

import rs.ac.bg.etf.automaton.Automaton;
import rs.ac.bg.etf.predictor.BHR;
import rs.ac.bg.etf.predictor.Instruction;
import rs.ac.bg.etf.predictor.Predictor;

public class Bimodal implements Predictor {

    Automaton[][] automatons;
    int[] selector;
    int historySize;


    BHR bhr;
    int maskGroup;
    int maskSelector;
    
    String signature;


    public Bimodal(int BHRsize, int numOfLastBitsAddressGroups, int numOfLastBitsAddressSelector, Automaton.AutomatonType type, int historySize){
    	signature="Bimodal "+BHRsize+" "+numOfLastBitsAddressGroups+" "+numOfLastBitsAddressSelector+" "+type+" "+historySize;
        bhr = new BHR(BHRsize);
        this.historySize=historySize;

        int rowSizeGroup = 1<<(BHRsize>numOfLastBitsAddressGroups?BHRsize:numOfLastBitsAddressGroups);
        maskGroup = rowSizeGroup-1;

        automatons = new Automaton[2][];
        automatons[0] = Automaton.instanceArray(type,rowSizeGroup);
        automatons[1] = Automaton.instanceArray(type,rowSizeGroup);

        int rowSizeSelector=1<<numOfLastBitsAddressSelector;
        selector = new int[rowSizeSelector];
        maskSelector = rowSizeSelector-1;
    }
    public Bimodal(int BHRsize, int numOfLastBitsAddressGroups, int numOfLastBitsAddressSelector, Automaton.AutomatonType type)
    {
        this(BHRsize,numOfLastBitsAddressGroups,numOfLastBitsAddressSelector,type,2);
    }

    @Override
    public boolean predict(Instruction branch) {
        int indexRowGroup= (int) ((bhr.getValue() ^ branch.getAddress())&maskGroup);
        int indexSelectorGroup= (int) (branch.getAddress()&maskSelector);

        int selectedGroup=selector[indexSelectorGroup]<0?0:1;

        return automatons[selectedGroup][indexRowGroup].predict();
    }

    @Override
    public void update(Instruction branch) {
        int indexRowGroup= (int) ((bhr.getValue() ^ branch.getAddress())&maskGroup);
        int indexSelectorGroup= (int) (branch.getAddress()&maskSelector);

        int selectedGroup=selector[indexSelectorGroup]<0?0:1;

        boolean outcome = branch.isTaken();

        // Update Group
        automatons[selectedGroup][indexRowGroup].updateAutomaton(outcome);

        // Update Selector
        if(outcome) {
            if(selector[indexSelectorGroup] < (historySize-1)) selector[indexSelectorGroup]++;
        } else {
            if(selector[indexSelectorGroup] > (-historySize)) selector[indexSelectorGroup]--;
        }

        // Update BHR
        bhr.insertOutcome(outcome);
    }
    
    @Override
    public String toString() {
    	return signature;
    }
}

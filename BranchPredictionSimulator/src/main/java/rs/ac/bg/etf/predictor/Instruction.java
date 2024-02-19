package rs.ac.bg.etf.predictor;

public interface Instruction {

	long getAddress();
	
	boolean isTaken();
	
	boolean isConditional();
	
	boolean isBackwardBranch();
	
	boolean isBranch();
	
}

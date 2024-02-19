package rs.ac.bg.etf.parser.CisPenn2011;

import rs.ac.bg.etf.predictor.Instruction;

public class CISPENN2011_Instruction implements Instruction {

	private long pc; // address of branch instruction
	private boolean outcome;
	private boolean cond;
	private boolean isBackward;
	private boolean isBranch;
	
	public CISPENN2011_Instruction(long pc, boolean outcome, boolean isBranch, boolean cond, boolean isBackward) {
		super();
		this.pc = pc;
		this.outcome = outcome;
		this.cond = cond;
		this.isBackward = isBackward;
		this.isBranch = isBranch;
	}

	@Override
	public long getAddress() {
		return pc;
	}

	@Override
	public boolean isTaken() {
		return outcome;
	}

	@Override
	public boolean isConditional() {
		return cond;
	}

	@Override
	public boolean isBackwardBranch() {	return isBackward;	}

	@Override
	public boolean isBranch() {
		return isBranch;
	}

}

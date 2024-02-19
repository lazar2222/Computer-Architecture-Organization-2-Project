package rs.ac.bg.etf.parser;

import rs.ac.bg.etf.predictor.Instruction;

public interface Parser {

	public boolean hasNext();
	
	public Instruction getNext();
	
}

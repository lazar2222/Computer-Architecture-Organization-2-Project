package rs.ac.bg.etf.predictor;

public interface Predictor {

	public boolean predict(Instruction branch);
	
	public void update(Instruction branch);
	
}

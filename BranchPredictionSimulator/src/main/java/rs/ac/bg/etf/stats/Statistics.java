package rs.ac.bg.etf.stats;

public class Statistics {

	private int numOfCondBranches;
	private int numOfHits;
	private int numOfMisses;
	
	private int distanceB2B;
	private int sumDisnatce;
	private int numOfDistances;
	
	public void resetDistanceB2B() {
		numOfDistances++;
		sumDisnatce += distanceB2B;
		distanceB2B = 0;
	}
	
	public void incDistanceB2B() {
		distanceB2B++;
	}
	
	public double getAverageDistanceBetween2CondBranches() {
		return (double)sumDisnatce/numOfDistances;
	}
	
	public void incNumOfCondBranches() {
		numOfCondBranches++;
	}
	
	public void incNumOfHits() {
		numOfHits++;
	}
	
	public void incNumOfMisses() {
		numOfMisses++;
	}

	public int getNumOfCondBranches() {
		return numOfCondBranches;
	}

	public int getNumOfHits() {
		return numOfHits;
	}

	public int getNumOfMisses() {
		return numOfMisses;
	}
	
}

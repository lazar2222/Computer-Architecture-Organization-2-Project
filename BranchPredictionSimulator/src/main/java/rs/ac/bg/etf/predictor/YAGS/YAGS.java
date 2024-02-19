package rs.ac.bg.etf.predictor.YAGS;

import rs.ac.bg.etf.automaton.Automaton;
import rs.ac.bg.etf.automaton.Automaton.AutomatonType;
import rs.ac.bg.etf.predictor.BHR;
import rs.ac.bg.etf.predictor.Instruction;
import rs.ac.bg.etf.predictor.Predictor;

public class YAGS implements Predictor 
{

	int[] selector;
	Automaton[][] automatons;
	long[][] tags;
	BHR bhr;
	int depthBits;
	int selectorSize;
	int depthMask;
	int selectorMask;
	
	String signature;
	
	public YAGS(int depthBits,int selectorSize,AutomatonType type)
	{
		signature="YAGS "+depthBits+" "+selectorSize+" "+type;
		this.depthBits=depthBits;
		this.selectorSize=selectorSize;
		depthMask=(int) (Math.pow(2, depthBits)-1);
		selectorMask=(int) (Math.pow(2, selectorSize)-1);
		bhr=new BHR(depthBits);
		selector=new int[selectorMask+1];
		automatons=new Automaton[4][];
		tags=new long[2][depthMask+1];
		for (int i = 0; i < 4; i++) 
		{
			automatons[i]=Automaton.instanceArray(type, depthMask+1);
		}
		for (int i = 0; i < 2; i++) 
		{
			for (int j = 0; j < depthMask+1; j++) 
			{
				tags[i][j]=-1;
			}
		}
		for (int i = 0; i < selectorMask+1; i++) 
		{
			selector[i]=1;
		}
	}
	
	@Override
	public boolean predict(Instruction branch) 
	{
		int selectorEnt=(int) (branch.getAddress()&selectorMask);
		int ind=selector[selectorEnt]>0?1:0;
		int ent=(int) ((branch.getAddress()^bhr.getValue())&depthMask);
		if(tags[ind][ent]==branch.getAddress() && tags[ind][ent]!=-1) 
		{
			return automatons[ind][ent].predict();
		}
		else 
		{
			return automatons[2+ind][ent].predict();
		}
	}

	@Override
	public void update(Instruction branch) 
	{
		boolean outcome=branch.isTaken();
		int selectorEnt=(int) (branch.getAddress()&selectorMask);
		int ind=selector[selectorEnt]>0?1:0;
		int ent=(int) ((branch.getAddress()^bhr.getValue())&depthMask);
		
		automatons[2+ind][ent].updateAutomaton(outcome);
		
		if(tags[ind][ent]==branch.getAddress() && tags[ind][ent]!=-1) 
		{
			automatons[ind][ent].updateAutomaton(outcome);
		}
		else if(automatons[2+ind][ent].predict() != outcome) 
		{
			if(outcome) 
			{
				tags[1][ent]=branch.getAddress();
				automatons[1][ent].updateAutomaton(outcome);
				automatons[1][ent].updateAutomaton(outcome);
			}
			else 
			{
				tags[0][ent]=branch.getAddress();
				automatons[0][ent].updateAutomaton(outcome);
				automatons[0][ent].updateAutomaton(outcome);
			}
		}
		
		selector[selectorEnt]+=outcome?1:-1;
		if(selector[selectorEnt]>2) {selector[selectorEnt]=2;}
		if(selector[selectorEnt]<-1) {selector[selectorEnt]=-1;}
        
        bhr.insertOutcome(outcome);
	}

	@Override
    public String toString() {
    	return signature;
    }
}

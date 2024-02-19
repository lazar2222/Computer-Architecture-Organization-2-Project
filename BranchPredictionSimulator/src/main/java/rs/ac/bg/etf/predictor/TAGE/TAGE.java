package rs.ac.bg.etf.predictor.TAGE;

import java.util.Iterator;

import rs.ac.bg.etf.automaton.Automaton;
import rs.ac.bg.etf.automaton.Automaton.AutomatonType;
import rs.ac.bg.etf.predictor.BHR;
import rs.ac.bg.etf.predictor.Instruction;
import rs.ac.bg.etf.predictor.Predictor;

public class TAGE implements Predictor 
{
	
	Automaton[][] automatons;
	int[][] tags;
	int[][] usefull;
	BHR bhr;
    int depth;
    int mask;
    int depthMask;
    int width;
    int depthBits;
    int tagSize;
    
    String signature;
	
	public TAGE(int width,int depthBits,int tagSize,AutomatonType type) 
	{
		signature="TAGE "+width+" "+depthBits+" "+tagSize+" "+type;
		this.tagSize=tagSize;
		this.width=width;
		this.depthBits=depthBits;
		depth=(int) Math.pow(2, depthBits);
		mask=(int) (Math.pow(2, width)-1);
		automatons = new Automaton[width+1][];
		tags=new int[width+1][depth];
		usefull=new int[width+1][depth];
		for (int i = 0; i < width+1; i++) 
		{
			automatons[i]=Automaton.instanceArray(type, depth);
			for (int j = 0; j < depth; j++) 
			{
				tags[i][j]=-1;
				usefull[i][j]=0;
			}
		}
		bhr=new BHR(width);
	}
	
	@Override
	public boolean predict(Instruction branch) 
	{
		int preds=width;
		int tmask=mask;
		while(preds>0) 
		{
			int hash=(int) (branch.getAddress()^(bhr.getValue()&tmask));
			int ent=hash&(depth-1);
			int tag=(hash>>depthBits)^(tagSize-1);
			if(tags[preds][ent]!=-1 && tags[preds][ent]==tag) 
			{
				return automatons[preds][ent].predict();
			}
			preds--;
			tmask>>=1;
		}
		return automatons[0][(int) (branch.getAddress()&(depth-1))].predict();
	}

	@Override
	public void update(Instruction branch) 
	{
		boolean outcome=branch.isTaken();
		boolean prediction=predict(branch);
		
		int preds=width;
		int tmask=mask;
		while(preds>0) 
		{
			int hash=(int) (branch.getAddress()^(bhr.getValue()&tmask));
			int ent=hash&(depth-1);
			int tag=(hash>>depthBits)^(tagSize-1);
			if(tags[preds][ent]!=-1 && tags[preds][ent]==tag) 
			{
				automatons[preds][ent].updateAutomaton(outcome);
				if(prediction==outcome) 
				{
					usefull[preds][ent]++;
					if(usefull[preds][ent]>3) 
					{
						usefull[preds][ent]=3;
					}
				}
				else 
				{
					usefull[preds][ent]--;
					if(usefull[preds][ent]<0) 
					{
						usefull[preds][ent]=0;
					}
				}
			}
			preds--;
			tmask>>=1;
		}
		automatons[0][(int) (branch.getAddress()&(depth-1))].updateAutomaton(outcome);
        
		if(outcome!=prediction) 
		{
			preds=width;
			tmask=mask;
			while(preds>0) 
			{
				int hash=(int) (branch.getAddress()^(bhr.getValue()&tmask));
				int ent=hash&(depth-1);
				int tag=(hash>>depthBits)^(tagSize-1);
				if(usefull[preds][ent]==0) 
				{
					tags[preds][ent]=tag;
					automatons[preds][ent].updateAutomaton(outcome);
					break;
				}
				preds--;
				tmask>>=1;
			}
		}
		
        bhr.insertOutcome(outcome);
	}
	
	@Override
    public String toString() {
    	return signature;
    }
}

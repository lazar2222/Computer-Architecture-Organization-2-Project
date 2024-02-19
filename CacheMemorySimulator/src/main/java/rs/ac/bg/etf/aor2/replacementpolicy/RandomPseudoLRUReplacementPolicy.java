package rs.ac.bg.etf.aor2.replacementpolicy;

import rs.ac.bg.etf.aor2.memory.MemoryOperation;
import rs.ac.bg.etf.aor2.memory.cache.ICacheMemory;
import rs.ac.bg.etf.aor2.memory.cache.Tag;

import java.util.ArrayList;
import java.util.Random;

public class RandomPseudoLRUReplacementPolicy implements IReplacementPolicy {

	private Random r;
	private ICacheMemory cache;
	private boolean[][] bits;
	private int numGroups;
	private int setNum;
	private int setAsoc;
	
	public RandomPseudoLRUReplacementPolicy(int numGroups) 
	{
		this.numGroups=numGroups;
	}
	
	@Override
	public void init(ICacheMemory cacheMemory) 
	{
		r=new Random();
		this.cache=cacheMemory;
		setNum=(int) cache.getSetNum();
		setAsoc=(int) cache.getSetAsociativity();
		bits = new boolean[setNum][setAsoc];
		
		for (int i = 0; i < setNum; i++) 
		{
            for (int j = 0; j < setAsoc; j++) 
            {
				bits[i][j]=false;
			}
        }
	}

	@Override
	public int getBlockIndexToReplace(long adr) 
	{
		int set = (int) cache.extractSet(adr);
		ArrayList<Tag> tagMemory = cache.getTags();
        for (int i = 0; i < setAsoc; i++) 
        {
            int block = set * setAsoc + i;
            Tag tag = tagMemory.get(block);
            if (!tag.V) 
            {
                return set*setAsoc+i;
            }
        }
		
        int randomGroup = r.nextInt(0,numGroups);
		int index = numGroups+randomGroup;
		while(index<setAsoc) 
		{
			index=2*index+(bits[set][index]?1:0);
		}
		return set*setAsoc+(index-setAsoc);
	}

	@Override
	public void doOperation(MemoryOperation operation) 
	{
		 MemoryOperation.MemoryOperationType opr = operation.getType();

	        if ((opr == MemoryOperation.MemoryOperationType.READ) || (opr == MemoryOperation.MemoryOperationType.WRITE)) 
	        {
	        	long adr = operation.getAddress();
	            int set = (int) cache.extractSet(adr);
	            long tagTag = cache.extractTag(adr);
	            ArrayList<Tag> tagMemory = cache.getTags();
	            int entry = 0;
	            for (int i = 0; i < setAsoc; i++) {
	                int block = set * setAsoc + i;
	                Tag tag = tagMemory.get(block);
	                if (tag.V && (tag.tag == tagTag)) {
	                    entry = i;
	                    break;
	                }
	            }
	            
	            entry+=setAsoc;
	            int val=0;
	            while(entry>1) 
	            {
	            	boolean flag = entry%2==0;
	            	val*=2;
	            	val+=flag?1:0;
	            	entry/=2;
	            	bits[set][entry]=flag;
	            }
	        }
	        else if (operation.getType() == MemoryOperation.MemoryOperationType.FLUSHALL) 
	        {
	            reset();
	        }
		
	}

	@Override
	public String printValid() 
	{
		return null;
	}

	@Override
	public String printAll() 
	{
		return null;
	}

	@Override
	public void reset() 
	{
		for (int i = 0; i < setNum; i++) 
		{
            for (int j = 0; j < setAsoc; j++) 
            {
				bits[i][j]=false;
			}
        }
	}
   
}

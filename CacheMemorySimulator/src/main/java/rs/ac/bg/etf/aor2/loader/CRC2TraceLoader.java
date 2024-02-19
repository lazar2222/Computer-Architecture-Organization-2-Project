package rs.ac.bg.etf.aor2.loader;

import rs.ac.bg.etf.aor2.memory.MemoryOperation;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.tukaani.xz.XZInputStream;

public class CRC2TraceLoader implements ITraceLoader, Closeable 
{
	private XZInputStream xzIn;
	private ArrayList<MemoryOperation> addrs;
	private boolean end=false;
	private int lines=0;
	private long guard;
	
    public CRC2TraceLoader(String fileName,long guard) throws IOException 
    {
    	addrs=new ArrayList<>();
    	this.guard=guard;
        FileInputStream fin = new FileInputStream(fileName);
        BufferedInputStream in = new BufferedInputStream(fin);
        xzIn = new XZInputStream(in);
    }

    private void ParseLine() throws IOException 
    {
    	if(lines%100000==0) 
    	{
    		System.out.println("Lines "+lines);
    	}
    	lines++;
    	long ip=parseLong();
    	for (int i = 0; i < 4; i++) 
    	{
    		addrs.add(MemoryOperation.read(ip+i));
		}
    	parseLong();
    	parseLong();
    	for (int i = 0; i < 4; i++) 
    	{
    		ip=parseLong();
    		if(ip!=0) 
    		{
    			addrs.add(MemoryOperation.write(ip));
    		}
		}
    	for (int i = 0; i < 4; i++) 
    	{
    		ip=parseLong();
    		if(ip!=0) 
    		{
    			addrs.add(MemoryOperation.read(ip));
    		}
		}
    	parseLong();
    }
    
    private long parseLong() throws IOException 
    {
    	long val = 0;
    	for (int i = 0; i < 8; i++) 
    	{
    		int read=xzIn.read();
    		if(read==-1) {end=true;return 0;}
    		val=val+(read<<(8*i));
		}
    	return val;
    }
    
	@Override
	public void close() throws IOException {
		xzIn.close();
	}

	@Override
	public MemoryOperation getNextOperation() {
		if(addrs.isEmpty()) 
		{
			try {
				ParseLine();
			} catch (IOException e) {
				end=true; 
			}
		}
		return addrs.remove(0);
	}

	@Override
	public boolean isInstructionOperation() {
		return false;
	}

	@Override
	public boolean hasOperationToLoad() {
		return (!end || !addrs.isEmpty()) && lines<=guard;
	}

	@Override
	public void reset() {
	}
}

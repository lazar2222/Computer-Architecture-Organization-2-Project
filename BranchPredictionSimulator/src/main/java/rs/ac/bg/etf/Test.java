package rs.ac.bg.etf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import rs.ac.bg.etf.automaton.Automaton;
import rs.ac.bg.etf.automaton.Automaton.AutomatonType;
import rs.ac.bg.etf.parser.CisPenn2011.CISPENN2011_Parser;
import rs.ac.bg.etf.parser.CisPenn2017.CISPENN2017_Parser;
import rs.ac.bg.etf.parser.Parser;
import rs.ac.bg.etf.predictor.Instruction;
import rs.ac.bg.etf.predictor.Predictor;
import rs.ac.bg.etf.predictor.TAGE.TAGE;
import rs.ac.bg.etf.predictor.YAGS.YAGS;
import rs.ac.bg.etf.predictor.bimodal.Bimodal;
import rs.ac.bg.etf.predictor.correlation.Correlation;
import rs.ac.bg.etf.predictor.twoLevel.TwoLevel;
import rs.ac.bg.etf.stats.Statistics;

public class Test {

	public static ExecutorService executor;
	public static AtomicInteger running;
	
	public static void main(String[] args) {
        //Parser parser = new CISPENN2011_Parser("./traces/gcc-10M.trace.gz");
        //Parser parser = new CISPENN2011_Parser("./traces/ProcessedTrace.trace.gz");
        //Parser parser = new CISPENN2017_Parser("./traces/streamcluster-10M-v2.trace.gz");
		
		executor = Executors.newFixedThreadPool(8);
		
		running=new AtomicInteger();
			
		for(int f=0;f<3;f++) 
		{
			System.out.println("Running trace:"+f);
//			Predictor predictor = new TwoLevel(10, Automaton.AutomatonType.TWOBITS_TYPE1);
//			Predictor predictor = new Correlation(10,3, Automaton.AutomatonType.TWOBITS_TYPE2);
//			Predictor predictor = new Bimodal(10,5,6, Automaton.AutomatonType.TWOBITS_TYPE3);
//			Predictor predictor = new TAGE(4,10,8, Automaton.AutomatonType.TWOBITS_TYPE1);
//			Predictor predictor = new YAGS(10,6,Automaton.AutomatonType.TWOBITS_TYPE3);
			
			int[] depthLikeParam= {5,8,10,12,15};
			int[] secondaryParam= {3,4, 6, 8,10};
			int[] tageWidth=      {2,3, 4, 5, 6};
			AutomatonType[] types={AutomatonType.TWOBITS_TYPE1,AutomatonType.TWOBITS_TYPE2,AutomatonType.TWOBITS_TYPE3,AutomatonType.TWOBITS_TYPE4};
	
			for(int i=0;i<5;i++) 
			{
						for(int k=0;k<4;k++) 
						{
							test(getParser(f),new TwoLevel(depthLikeParam[i], types[k]),f);
						}
			}
			
			for(int i=0;i<5;i++) 
			{
				for(int j=0;j<5;j++) 
				{
						for(int k=0;k<4;k++) 
						{
							test(getParser(f),new Correlation(depthLikeParam[i], secondaryParam[j],types[k]),f);
						}
				}
			}
			
			for(int i=0;i<5;i++) 
			{
				for(int j=0;j<5;j++) 
				{
						for(int k=0;k<4;k++) 
						{
							test(getParser(f),new Bimodal(depthLikeParam[i],depthLikeParam[i], secondaryParam[j],types[k]),f);
						}
				}
			}
			
			for(int i=0;i<5;i++) 
			{
				for(int j=0;j<5;j++) 
				{
					for(int l=0;l<5;l++) 
					{
						for(int k=0;k<4;k++) 
						{
							test(getParser(f),new TAGE(tageWidth[l],depthLikeParam[i],secondaryParam[j],types[k]),f);
						}
					}
				}
			}
			
			for(int i=0;i<5;i++) 
			{
				for(int j=0;j<5;j++) 
				{
						for(int k=0;k<4;k++) 
						{
							test(getParser(f),new YAGS(depthLikeParam[i],secondaryParam[j],types[k]),f);
						}
				}
			}
		}
		executor.shutdown();
        try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        System.out.println("DONE");
	}
	
	public static Parser getParser(int f) 
	{
		switch (f) 
		{
			case 0:{return new CISPENN2011_Parser("./traces/ProcessedTrace.trace.gz");}
			case 1:{return new CISPENN2011_Parser("./traces/gcc-10M.trace.gz");}
			case 2:{return new CISPENN2017_Parser("./traces/streamcluster-10M-v2.trace.gz");}
		}
		return null;
	}
	
	public static void test(Parser parser,Predictor predictor,int test) 
	{
		if(!new File("Stats/"+predictor.toString()+" "+test+".txt").exists()) 
		{
			while(running.get()>8) {try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}}
			running.addAndGet(1);
			Runnable runnableTask = () -> {
			Statistics stats = new Statistics();
			long start = System.currentTimeMillis();
			System.out.println("Start!");
	
			Instruction ins;
			int all = 0;
			while ((ins = parser.getNext()) != null) {
				all++;
	//			if (!ins.isConditional()) {
	//				continue;
	//			}
				if(!ins.isBranch())
					continue;
				boolean prediction = predictor.predict(ins);
				if (prediction != ins.isTaken())
					stats.incNumOfMisses();
				else
					stats.incNumOfHits();
				stats.incNumOfCondBranches();
	
				predictor.update(ins);
			}
	
			long end = System.currentTimeMillis();
			System.out.println("End!");
	
			long durationInMillis = end - start;
			long millis = durationInMillis % 1000;
			long second = (durationInMillis / 1000) % 60;
			long minute = (durationInMillis / (1000 * 60)) % 60;
			long hour = (durationInMillis / (1000 * 60 * 60)) % 24;
	
			String time = String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);
			
			System.out.println(predictor.toString());
			System.out.println("Duration: " + time);
			System.out.println("Hits: " + stats.getNumOfHits());
			System.out.println("Misses: " + stats.getNumOfMisses());
			double percent = (double) stats.getNumOfHits() / stats.getNumOfCondBranches() * 100;
			System.out.println("Percent of hits: " + percent);
			System.out.println("Sum: " + stats.getNumOfCondBranches());
			int sum = stats.getNumOfHits() + stats.getNumOfMisses();
			System.out.println("Sum check: " + sum);
			System.out.println("All: " + all);
			
			 try(FileWriter fileWriter = new FileWriter("Stats/"+predictor.toString()+" "+test+".txt")) {
		            fileWriter.write("Duration: " + time+"\r\n");
		            fileWriter.write("Hits: " + stats.getNumOfHits()+"\r\n");
		            fileWriter.write("Misses: " + stats.getNumOfMisses()+"\r\n");
		            fileWriter.write("Percent of hits: " + percent+"\r\n");
		            fileWriter.write("Sum: " + stats.getNumOfCondBranches()+"\r\n");
		            fileWriter.write("Sum check: " + sum+"\r\n");
		            fileWriter.write("All: " + all+"\r\n");
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
			 	running.addAndGet(-1);
			};
			executor.execute(runnableTask);
		}
	}
}

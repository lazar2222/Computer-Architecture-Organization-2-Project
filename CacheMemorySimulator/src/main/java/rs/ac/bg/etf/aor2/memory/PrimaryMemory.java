package rs.ac.bg.etf.aor2.memory;

import java.util.HashMap;
import java.util.Random;

public class PrimaryMemory implements IMemory {


    private static Random rand = new Random();
    private HashMap<Long, Long> data = new HashMap<Long, Long>();
    private int adrSize;

    public PrimaryMemory(int adrSize) {
        this.adrSize = adrSize;
    }

    @Override
    public long read(long adr) {
        return data.getOrDefault(adr, rand.nextLong());
    }

    @Override
    public void write(long adr, long data) {
        this.data.put(adr, data);
    }

    @Override
    public int getAdrSize() {
        return adrSize;
    }

    @Override
    public int getDataSize() {
        //TODO: not yet implemented
        return 0;
    }

    @Override
    public IMemory getNextLevelMemory() {
        return null;
    }

    @Override
    public void reset() {
        data.clear();
    }
}

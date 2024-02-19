package rs.ac.bg.etf.aor2.memory.cache;

import rs.ac.bg.etf.aor2.memory.IMemory;
import rs.ac.bg.etf.aor2.memory.MemoryOperation;
import rs.ac.bg.etf.aor2.replacementpolicy.IReplacementPolicy;

import java.util.ArrayList;

public abstract class CacheMemory implements ICacheMemory {

    protected final int adrLen;
    protected final int setAsoc;
    protected final int blockNum;
    protected final int blockSize;
    protected final ArrayList<Tag> tagMemory;
    protected final ArrayList<long[]> dataMemory;
    private final int cacheSize;
    private final int setNum;
    private final int offsetLen;
    private final int setLen;
    private final int tagLen;
    protected IMemory nextLevelMemory;
    protected IReplacementPolicy replacementPolicy;


    protected long cacheHitNum;
    protected long cacheMissNum;
    protected long cacheWriteNum;
    protected long memoryAccessNum;
    protected long memoryReadNum;
    protected long memoryWriteNum;
    protected CacheAccessCharacteristics accessCharacteristics;
    protected long writeHits;
    protected long writeMissFreeSpace;
    protected long writeMissNoFreeSpace;
    protected TimeCalculator timeCalculator;
    private long cacheAccessNum;
    private long cacheReadNum;
    private long readHits;
    private long readMissFreeSpace;
    private long readMissNoFreeSpace;


    public CacheMemory(int adrLen, int setAsoc,
                       int blockNum, int blockSize, IMemory nextLevelMemory, IReplacementPolicy replacementPolicy) {
        this.adrLen = adrLen;
        this.setAsoc = setAsoc;
        this.blockNum = blockNum;
        this.blockSize = blockSize;
        this.cacheSize = blockNum * blockSize;
        this.setNum = blockNum / setAsoc;
        this.offsetLen = (int) (Math.log(blockSize) / Math.log(2));
        this.setLen = (int) (Math.log(setNum) / Math.log(2));
        this.tagLen = adrLen - setLen - offsetLen;

        this.tagMemory = new ArrayList<Tag>(blockNum);
        for (int i = 0; i < blockNum; i++) {
            tagMemory.add(createTag());
        }


        this.dataMemory = new ArrayList<long[]>(blockNum);
        for (int i = 0; i < blockNum; i++) {
            long[] data = new long[blockSize];
            for (int j = 0; j < blockSize; j++) {
                data[j] = 0;
            }
            dataMemory.add(data);
        }

        this.nextLevelMemory = nextLevelMemory;
        this.replacementPolicy = replacementPolicy;
        if (replacementPolicy != null) {
            replacementPolicy.init(this);
        }

        accessCharacteristics = new CacheAccessCharacteristics(this);
        timeCalculator = TimeCalculator.Simple();
        resetStatistic();
    }


    @Override
    public void reset() {
        tagMemory.clear();
        dataMemory.clear();
        replacementPolicy.reset();

        resetStatistic();
    }

    protected abstract Tag createTag();

    protected abstract int getType();

    protected abstract void writeMiss(long block, long adr, long data);

    protected abstract void writeHit(long block, long adr, long data);

    protected abstract void getFromMemory(int blockToFill, long adr);

    protected abstract boolean returnToMemory(int blockToReplace);

    @Override
    public long read(long adr) {
        cacheAccessNum++;
        accessCharacteristics.clean();
        accessCharacteristics.setAddress(adr);
        accessCharacteristics.setType(CacheAccessCharacteristics.RDTYPE);

        long data = 0;
        long set = extractSet(adr);
        long block = isAddressInSet(adr, set);
        boolean hit = block != -1;
        accessCharacteristics.setHit(hit);

        if (hit) {
            data = readHit(block, adr);
        } else {
            data = readMiss(block, adr);
        }
        return data;
    }


    private long readHit(long block, long adr) {
        long data = readValue(block, adr);
        replacementPolicy.doOperation(MemoryOperation.read(adr));

        accessCharacteristics.setAddresses("-");
        cacheHitNum++;

        readHits++;
        accessCharacteristics
                .setAccessTime(timeCalculator.getReadHitTimming(getType(), adrLen, setAsoc, blockNum, blockSize));

        return data;
    }

    private long readMiss(long block, long adr) {
        int blockToReplace = replacementPolicy.getBlockIndexToReplace(adr);
        if (returnToMemory(blockToReplace)) {
            readMissNoFreeSpace++;
            accessCharacteristics.setAccessTime(
                    timeCalculator.getReadMissNoFreeTimming(getType(), adrLen, setAsoc, blockNum, blockSize));
        } else {
            readMissFreeSpace++;
            accessCharacteristics
                    .setAccessTime(timeCalculator.getReadMissFreeTimming(getType(), adrLen, setAsoc, blockNum, blockSize));
        }
        getFromMemory(blockToReplace, adr);
        long data = readValue(blockToReplace, adr);
        replacementPolicy.doOperation(MemoryOperation.read(adr));

        cacheMissNum++;

        return data;
    }


    private long readValue(long block, long adr) {
        cacheReadNum++;
        long[] blockData = dataMemory.get((int) block);
        return blockData[(int) extractOffset(adr)];
    }


    @Override
    public void write(long adr, long data) {
        cacheAccessNum++;
        accessCharacteristics.clean();
        accessCharacteristics.setAddress(adr);
        accessCharacteristics.setType(CacheAccessCharacteristics.WRTYPE);

        long set = extractSet(adr);
        long block = isAddressInSet(adr, set);
        boolean hit = block != -1;
        accessCharacteristics.setHit(hit);

        if (hit) {
            writeHit(block, adr, data);
        } else {
            writeMiss(block, adr, data);
        }
    }


    private long isAddressInSet(long adr, long set) {
        long result = -1;
        long tagVal = extractTag(adr);
        for (int i = 0; i < setAsoc; i++) {
            int index = (int) set * setAsoc + i;
            Tag tag = tagMemory.get(index);
            if (tag.V && tag.tag == tagVal) {
                return set * setAsoc + i;
            }
        }
        return result;
    }

    @Override
    public void flush() {
        for (int i = 0; i < blockNum; i++) {
            returnToMemory(i);
        }
    }


    //region Simple getters

    @Override
    public int getAdrSize() {
        return adrLen;
    }

    @Override
    public int getDataSize() {
        //TODO: not yet implemented
        return 0;
    }

    @Override
    public IMemory getNextLevelMemory() {
        return nextLevelMemory;
    }

    @Override
    public long getCacheHitNum() {
        return cacheHitNum;
    }

    @Override
    public long getCacheMissNum() {
        return cacheMissNum;
    }

    @Override
    public long getCacheAccessNum() {
        return cacheAccessNum;
    }

    @Override
    public long getCacheReadNum() {
        return cacheReadNum;
    }

    @Override
    public long getCacheWriteNum() {
        return cacheWriteNum;
    }

    @Override
    public long getMemoryAccessNum() {
        return memoryAccessNum;
    }

    @Override
    public long getMemoryReadNum() {
        return memoryReadNum;
    }

    @Override
    public long getMemoryWriteNum() {
        return memoryWriteNum;
    }

    @Override
    public long getCacheSize() {
        return cacheSize;
    }

    @Override
    public long getBlockNum() {
        return blockNum;
    }

    @Override
    public long getBlockSize() {
        return blockSize;
    }

    @Override
    public long getSetNum() {
        return setNum;
    }

    @Override
    public long getSetLen() {
        return setLen;
    }

    @Override
    public long getOffsetLen() {
        return offsetLen;
    }

    @Override
    public long getTagLen() {
        return tagLen;
    }

    @Override
    public long getSetAsociativity() {
        return setAsoc;
    }

    @Override
    public long extractTag(long adr) {
        return extract(adr, offsetLen + setLen, tagLen);
    }

    @Override
    public long extractSet(long adr) {
        return extract(adr, offsetLen, setLen);
    }

    @Override
    public long extractOffset(long adr) {
        return extract(adr, 0, offsetLen);
    }

    @Override
    public ArrayList<Tag> getTags() {
        return (ArrayList<Tag>) tagMemory;
    }

    @Override
    public IReplacementPolicy getReplacementPolicy() {
        return replacementPolicy;
    }

    //endregion

    @Override
    public String getAccessTime() {
        return timeCalculator.calcTime(accessCharacteristics.getTimes());
    }

    @Override
    public String getAccessStatistics() {
        StringBuilder s = new StringBuilder();
        s.append(cacheAccessNum).append("\t");
        s.append(readHits + writeHits).append("\t");
        s.append(readMissFreeSpace + readMissNoFreeSpace + writeMissFreeSpace + writeMissNoFreeSpace).append("\t");

        s.append(cacheReadNum).append("\t");
        s.append(readHits).append("\t");
        s.append(readMissFreeSpace).append("\t");
        s.append(readMissNoFreeSpace).append("\t");

        s.append(cacheWriteNum).append("\t");
        s.append(writeHits).append("\t");
        s.append(writeMissFreeSpace).append("\t");
        s.append(writeMissNoFreeSpace).append("\t");

        return s.toString();
    }

    @Override
    public void resetStatistic() {
        cacheHitNum = 0;
        cacheMissNum = 0;

        cacheAccessNum = 0;
        cacheReadNum = 0;
        cacheWriteNum = 0;
        memoryAccessNum = 0;
        memoryReadNum = 0;
        memoryWriteNum = 0;

        readHits = 0;
        readMissFreeSpace = 0;
        readMissNoFreeSpace = 0;
        writeHits = 0;
        writeMissFreeSpace = 0;
        writeMissNoFreeSpace = 0;
    }

    @Override
    public String printLastAccess() {
        return accessCharacteristics.printAccess();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < blockNum; i++) {
            Tag tag = tagMemory.get(i);
            s = s.append(tag.toString());
            /*
             * long [] blockData = (long[]) dataMemory.get(i); for(int j = 0; j
             * < blockSize; j++){ s = s.append(", ").append(blockData[j]); }
             */
            s = s.append("\n");

        }
        return s.toString();
    }

    @Override
    public String printAll() {
        return toString();
    }

    @Override
    public String printValid() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < blockNum; i++) {
            Tag tag = tagMemory.get(i);
            if (tag.V) {
                s.append("block ").append(i);
                s.append(", set ").append(i / setAsoc).append(", ");
                s.append(tag.toString()).append("\n");
            }
        }
        return s.toString();
    }

    protected long compact(long tag, long set, long offset) {
        long result = 0;
        result = (((tag << setLen) | set) << offsetLen) | offset;
        return result;
    }

    private long extract(long adr, long start, long len) {
        long mask = 0;
        adr = adr >>> start;
        for (int i = 0; i < len; i++) {
            mask = (mask << 1) | 1;
        }
        return adr & mask;
    }
}

package rs.ac.bg.etf.aor2.memory.cache;

import rs.ac.bg.etf.aor2.memory.IMemory;
import rs.ac.bg.etf.aor2.replacementpolicy.IReplacementPolicy;

import java.util.ArrayList;

public interface ICacheMemory extends IMemory {

    int WRITEBACK_WRITEALLOCATED = 1;
    int WRITEBACK_NOWRITENOALLOCATED = 2;
    int WRITETHROUGHT_WRITEALLOCATED = 3;
    int WRITETHROUGHT_NOWRITEALLOCATED = 4;

    void flush();

    long getCacheHitNum();

    long getCacheMissNum();

    long getCacheAccessNum();

    long getCacheReadNum();

    long getCacheWriteNum();

    long getMemoryAccessNum();

    long getMemoryReadNum();

    long getMemoryWriteNum();

    String getAccessTime();

    String getAccessStatistics();

    void resetStatistic();

    String printLastAccess();

    String printAll();

    String printValid();

    long getCacheSize();

    long getBlockNum();

    long getBlockSize();

    long getSetNum();

    long getSetLen();

    long getOffsetLen();

    long getTagLen();

    long getSetAsociativity();

    long extractTag(long adr);

    long extractSet(long adr);

    long extractOffset(long adr);

    ArrayList<Tag> getTags();

    IReplacementPolicy getReplacementPolicy();

}

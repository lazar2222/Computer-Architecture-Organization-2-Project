package rs.ac.bg.etf.aor2.replacementpolicy;

import rs.ac.bg.etf.aor2.memory.MemoryOperation;
import rs.ac.bg.etf.aor2.memory.cache.ICacheMemory;

public interface IReplacementPolicy {

    void init(ICacheMemory cacheMemory);

    int getBlockIndexToReplace(long adr);

    void doOperation(MemoryOperation operation);

    String printValid();

    String printAll();

    void reset();
}

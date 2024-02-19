package rs.ac.bg.etf.aor2.loader;

import rs.ac.bg.etf.aor2.memory.MemoryOperation;

public interface ITraceLoader {

    MemoryOperation getNextOperation();

    boolean isInstructionOperation();

    boolean hasOperationToLoad();

    void reset();
}

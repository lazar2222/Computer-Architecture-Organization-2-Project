package rs.ac.bg.etf.aor2.memory;

public class MemoryOperation {
    private MemoryOperationType type;
    private long address;
    public MemoryOperation(MemoryOperationType type, long address) {
        this.type = type;
        this.address = address;
    }

    public static MemoryOperation read(long address) {
        return new MemoryOperation(MemoryOperationType.READ, address);
    }

    public static MemoryOperation write(long address) {
        return new MemoryOperation(MemoryOperationType.WRITE, address);
    }

    public static MemoryOperation flushAddress(long address) {
        return new MemoryOperation(MemoryOperationType.FLUSHADDR, address);
    }

    public static MemoryOperation flushAll(long address) {
        return new MemoryOperation(MemoryOperationType.FLUSHALL, address);
    }

    public MemoryOperationType getType() {
        return type;
    }

    public long getAddress() {
        return address;
    }

    public enum MemoryOperationType {READ, WRITE, FLUSHADDR, FLUSHALL}
}

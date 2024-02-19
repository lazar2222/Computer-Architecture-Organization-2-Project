package rs.ac.bg.etf.aor2.memory;

public interface IMemory {
    long read(long adr);

    void write(long adr, long data);

    int getAdrSize();

    int getDataSize();

    IMemory getNextLevelMemory();

    void reset();
}

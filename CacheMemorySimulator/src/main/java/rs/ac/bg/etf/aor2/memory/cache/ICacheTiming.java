package rs.ac.bg.etf.aor2.memory.cache;

public interface ICacheTiming {
    String getReadHitTimming();

    String getReadMissFreeTimming(int n);

    String getReadMissNoFreeTimming(int n);

    String getWriteHitTimming();

    String getWriteMissFreeTimming(int n);

    String getWriteMissNoFreeTimming(int n);

	/*
	public String getAllTime(int n, long readHits, long readMissFreeSpace,
			long readMissNoFreeSpace, long writeHits, long writeMissFreeSpace,
			long writeMissNoFreeSpace);
	*/
}

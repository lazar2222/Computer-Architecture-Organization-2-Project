package rs.ac.bg.etf.aor2.memory.cache;

import java.util.LinkedList;
import java.util.List;

public class CacheAccessCharacteristics {
    public static final long RDTYPE = 0;
    public static final long WRTYPE = 1;
    static final String RD = "Rd";
    static final String WR = "Wr";
    static final String CACHEMISS = "Cache Miss";
    static final String CACHEHIT = "Cache His";

    long address;
    long type;
    String accessTime;
    boolean hit;
    String addresses;
    ICacheMemory cache;

    List<String> times;

    public CacheAccessCharacteristics(ICacheMemory cache) {
        this.cache = cache;
        clean();

        times = new LinkedList<String>();
    }

    public long getAddress() {
        return address;
    }

    public void setAddress(long address) {
        this.address = address;
    }

    public long getType() {
        return type;
    }

    public void setType(long type) {
        this.type = type;
    }

    public String getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(String accessTime) {
        this.accessTime = accessTime;
        times.add(accessTime);
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public String getAddresses() {
        return addresses;
    }

    public void setAddresses(String addresses) {
        this.addresses = addresses;
    }

    public ICacheMemory getCache() {
        return cache;
    }

    public void setCache(ICacheMemory cache) {
        this.cache = cache;
    }

    public String printAccess() {
        StringBuilder s = new StringBuilder();
        StringBuilder hexAddress = new StringBuilder(Long.toHexString(address).toUpperCase());
        int padlength = cache.getAdrSize() / 4 - hexAddress.length();
        while (padlength > 0) {
            hexAddress.insert(0, '0');
            padlength--;
        }
        s = s.append("0x").append(hexAddress).append("\t");
        s = s.append((type == RDTYPE ? RD : WR)).append("\t");
        s = s.append(fit(cache.extractTag(address), cache.getTagLen())).append("\t");
        s = s.append(fit(cache.extractSet(address), cache.getSetLen())).append("\t");
        s = s.append(fit(cache.extractOffset(address), cache.getOffsetLen())).append("\t");
        s = s.append(accessTime).append("\t");
        // s = s.append( (hit ? CACHEHIT : CACHEMISS) ).append( "\t");
        s = s.append(addresses.toUpperCase()).append("\t");
        return s.toString();
    }

    public String fit(long word, long size) {
        String s = "";
        for (int i = 0; i < size; i++) {
            s = s + "0";
        }
        s = s + Long.toBinaryString(word);
        s = s.substring(s.length() - (int) size);

        return s;
    }

    public void clean() {
        address = 0;
        type = 0;
        accessTime = "";
        hit = false;
        addresses = "";

    }

    public List<String> getTimes() {
        return times;
    }

    public void setTimes(List<String> times) {
        this.times = times;
    }

}

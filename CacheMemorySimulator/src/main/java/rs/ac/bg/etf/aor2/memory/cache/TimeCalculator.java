package rs.ac.bg.etf.aor2.memory.cache;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeCalculator {

    Map<String, ICacheTiming> timmings;

    private TimeCalculator() {
    }

    public static TimeCalculator Simple() {
        TimeCalculator timeCalculator = new TimeCalculator();
        timeCalculator.initSimple();
        return timeCalculator;
    }

    public static TimeCalculator Complex() {
        TimeCalculator timeCalculator = new TimeCalculator();
        timeCalculator.initComplex();
        return timeCalculator;
    }

    public String getReadHitTimming(int type, int adrLen, int setAsoc,
                                    int blockNum, int blockSize) {
        String key = getCacheType(type, adrLen, setAsoc, blockNum, blockSize);
        ICacheTiming timming = timmings.get(key);
        return timming.getReadHitTimming();
    }

    public String getReadMissFreeTimming(int type, int adrLen, int setAsoc,
                                         int blockNum, int blockSize) {
        String key = getCacheType(type, adrLen, setAsoc, blockNum, blockSize);
        ICacheTiming timming = timmings.get(key);
        return timming.getReadMissFreeTimming(blockSize);
    }

    public String getReadMissNoFreeTimming(int type, int adrLen, int setAsoc,
                                           int blockNum, int blockSize) {
        String key = getCacheType(type, adrLen, setAsoc, blockNum, blockSize);
        ICacheTiming timming = timmings.get(key);
        return timming.getReadMissNoFreeTimming(blockSize);
    }

    public String getWriteHitTimming(int type, int adrLen, int setAsoc,
                                     int blockNum, int blockSize) {
        String key = getCacheType(type, adrLen, setAsoc, blockNum, blockSize);
        ICacheTiming timming = timmings.get(key);
        return timming.getWriteHitTimming();
    }

    public String getWriteMissFreeTimming(int type, int adrLen, int setAsoc,
                                          int blockNum, int blockSize) {
        String key = getCacheType(type, adrLen, setAsoc, blockNum, blockSize);
        ICacheTiming timming = timmings.get(key);
        return timming.getWriteMissFreeTimming(blockSize);
    }

    public String getWriteMissNoFreeTimming(int type, int adrLen, int setAsoc,
                                            int blockNum, int blockSize) {
        String key = getCacheType(type, adrLen, setAsoc, blockNum, blockSize);
        ICacheTiming timming = timmings.get(key);
        return timming.getWriteMissNoFreeTimming(blockSize);
    }

    private String getCacheType(int type, int adrLen, int setAsoc,
                                int blockNum, int blockSize) {
        String result = "" + type + "-";
        if (setAsoc == 1) {
            result += "dir";
        } else if (blockNum == setAsoc) {
            result += "asoc";
        } else {
            result += "set-asoc";
        }
        return result;
    }

    public String calcTime(List<String> times) {
        String result = "";
        Map<String, Integer> values = new HashMap<String, Integer>();
        for (String time : times) {
            parseLine(values, time);
        }
        String[] keys = values.keySet().toArray(
                new String[values.keySet().size()]);
        Arrays.sort(keys);
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            result += values.get(key) + "*" + key;
            if (i != keys.length - 1) {
                result += "+";
            }
        }
        return result;
    }

    private void parseLine(Map<String, Integer> values, String time) {
        int num = 1;
        String token = "";
        int state = 0;
        for (int i = 0; i < time.length(); i++) {
            char c = time.charAt(i);
            if (Character.isDigit(c)) {
                if (state == 0) {
                    num = Character.digit(c, 10);
                    state = 1;
                } else {
                    num = num * 10 + Character.digit(c, 10);
                }
            } else if (c == '+') {
                addToken(values, num, token);
                token = "";
            } else if (c == '*' || c == ' ') {

            } else if (c == '(') {
                token = "";
            } else if (c == ')') {
                addToken(values, num, token);
                token = "";
                num = 1;
                state = 0;
            } else {
                token += c;
            }
        }
        addToken(values, num, token);
    }

    public void addToken(Map<String, Integer> values, int num, String token) {
        if (token == null || token.equals("") || token.equals(" ")) {
            return;
        }
        Integer n = values.get(token);
        if (n == null) {
            values.put(token, num);
        } else {
            values.put(token, n + num);
        }
    }

    private void initComplex() {
        timmings = new HashMap<String, ICacheTiming>();
        timmings.put(ICacheMemory.WRITEBACK_WRITEALLOCATED + "-" + "dir",
                new DirWriteBackWriteAllocated());
        timmings.put(ICacheMemory.WRITEBACK_NOWRITENOALLOCATED + "-" + "dir",
                new DirWriteBackWriteNoAllocated());
        timmings.put(ICacheMemory.WRITETHROUGHT_WRITEALLOCATED + "-" + "dir",
                new DirWriteThrouWriteAllocated());
        timmings.put(ICacheMemory.WRITETHROUGHT_NOWRITEALLOCATED + "-" + "dir",
                new DirWriteThrouWriteNoAllocated());

        timmings.put(ICacheMemory.WRITEBACK_WRITEALLOCATED + "-" + "asoc",
                new AsocWriteBackWriteAllocated());
        timmings.put(ICacheMemory.WRITEBACK_NOWRITENOALLOCATED + "-" + "asoc",
                new AsocWriteBackWriteNoAllocated());
        timmings.put(ICacheMemory.WRITETHROUGHT_WRITEALLOCATED + "-" + "asoc",
                new AsocWriteThrouWriteAllocated());
        timmings.put(ICacheMemory.WRITETHROUGHT_NOWRITEALLOCATED + "-" + "asoc",
                new AsocWriteThrouWriteNoAllocated());

        timmings.put(ICacheMemory.WRITEBACK_WRITEALLOCATED + "-" + "set-asoc",
                new SetWriteBackWriteAllocated());
        timmings.put(ICacheMemory.WRITEBACK_NOWRITENOALLOCATED + "-" + "set-asoc",
                new SetWriteBackWriteNoAllocated());
        timmings.put(
                ICacheMemory.WRITETHROUGHT_WRITEALLOCATED + "-" + "set-asoc",
                new SetWriteThrouWriteAllocated());
        timmings.put(ICacheMemory.WRITETHROUGHT_NOWRITEALLOCATED + "-"
                + "set-asoc", new SetWriteThrouWriteNoAllocated());

    }

    private void initSimple() {
        timmings = new HashMap<String, ICacheTiming>();
        timmings.put(ICacheMemory.WRITEBACK_WRITEALLOCATED + "-" + "dir",
                new DirWriteBackWriteAllocatedSimple());
        timmings.put(ICacheMemory.WRITEBACK_NOWRITENOALLOCATED + "-" + "dir",
                new DirWriteBackWriteNoAllocatedSimple());
        timmings.put(ICacheMemory.WRITETHROUGHT_WRITEALLOCATED + "-" + "dir",
                new DirWriteThrouWriteAllocatedSimple());
        timmings.put(ICacheMemory.WRITETHROUGHT_NOWRITEALLOCATED + "-" + "dir",
                new DirWriteThrouWriteNoAllocatedSimple());

        timmings.put(ICacheMemory.WRITEBACK_WRITEALLOCATED + "-" + "asoc",
                new AsocWriteBackWriteAllocatedSimple());
        timmings.put(ICacheMemory.WRITEBACK_NOWRITENOALLOCATED + "-" + "asoc",
                new AsocWriteBackWriteNoAllocatedSimple());
        timmings.put(ICacheMemory.WRITETHROUGHT_WRITEALLOCATED + "-" + "asoc",
                new AsocWriteThrouWriteAllocatedSimple());
        timmings.put(ICacheMemory.WRITETHROUGHT_NOWRITEALLOCATED + "-" + "asoc",
                new AsocWriteThrouWriteNoAllocatedSimple());

        timmings.put(ICacheMemory.WRITEBACK_WRITEALLOCATED + "-" + "set-asoc",
                new SetWriteBackWriteAllocatedSimple());
        timmings.put(ICacheMemory.WRITEBACK_NOWRITENOALLOCATED + "-" + "set-asoc",
                new SetWriteBackWriteNoAllocatedSimple());
        timmings.put(
                ICacheMemory.WRITETHROUGHT_WRITEALLOCATED + "-" + "set-asoc",
                new SetWriteThrouWriteAllocatedSimple());
        timmings.put(ICacheMemory.WRITETHROUGHT_NOWRITEALLOCATED + "-"
                + "set-asoc", new SetWriteThrouWriteNoAllocatedSimple());

    }

    class DirWriteThrouWriteNoAllocated implements ICacheTiming {

        public String getReadHitTimming() {
            return "tSD";
        }

        public String getReadMissFreeTimming(int n) {
            return "tSD+" + n + "*(tOM+tDM)+tTM+tSD";
        }

        public String getReadMissNoFreeTimming(int n) {
            return "tSD+" + n + "*(tDM+tOM)+" + n + "*(tOM+tDM)+tTM+tSD";
        }

        public String getWriteHitTimming() {
            return "tSD+tDM+tOM";
        }

        public String getWriteMissFreeTimming(int n) {
            return "tSD+tOM";
        }

        public String getWriteMissNoFreeTimming(int n) {
            return "tSD+tOM";
        }

    }

    class DirWriteThrouWriteAllocated implements ICacheTiming {

        public String getReadHitTimming() {
            return "tSD";
        }

        public String getReadMissFreeTimming(int n) {
            return "tSD+" + n + "*(tOM+tDM)+tTM+tSD";
        }

        public String getReadMissNoFreeTimming(int n) {
            return "tSD+" + n + "*(tDM+tOM)+" + n + "*(tOM+tDM)+tTM+tSD";
        }

        public String getWriteHitTimming() {
            return "tSD+tDM+tOM";
        }

        public String getWriteMissFreeTimming(int n) {
            return "tSD+" + n + "*(tOM+tDM)+tTM+tSD+tDM+tOM";
        }

        public String getWriteMissNoFreeTimming(int n) {
            return "tSD+" + n + "*(tDM+tOM)+" + n
                    + "*(tOM+tDM)+tTM+tSD+tDM+tOM";
        }

    }

    class DirWriteBackWriteNoAllocated implements ICacheTiming {

        public String getReadHitTimming() {
            return "tSD";
        }

        public String getReadMissFreeTimming(int n) {
            return "tSD+" + n + "*(tOM+tDM)+tTM+tSD";
        }

        public String getReadMissNoFreeTimming(int n) {
            return "tSD+" + n + "*(tDM+tOM)+" + n + "*(tOM+tDM)+tTM+tSD";
        }

        public String getWriteHitTimming() {
            return "tSD+tDM";
        }

        public String getWriteMissFreeTimming(int n) {
            return "tSD+tOM";
        }

        public String getWriteMissNoFreeTimming(int n) {
            return "tSD+tOM";
        }

    }

    class DirWriteBackWriteAllocated implements ICacheTiming {

        public String getReadHitTimming() {
            return "tSD";
        }

        public String getReadMissFreeTimming(int n) {
            return "tSD+" + n + "*(tOM+tDM)+tTM+tSD";
        }

        public String getReadMissNoFreeTimming(int n) {
            return "tSD+" + n + "*(tDM+tOM)+" + n + "*(tOM+tDM)+tTM+tSD";
        }

        public String getWriteHitTimming() {
            return "tSD+tDM";
        }

        public String getWriteMissFreeTimming(int n) {
            return "tSD+" + n + "*(tOM+tDM)+tTM+tSD+tDM";
        }

        public String getWriteMissNoFreeTimming(int n) {
            return "tSD+" + n + "*(tDM+tOM)+" + n + "*(tOM+tDM)+tTM+tSD+tDM";
        }

    }

    class AsocWriteThrouWriteNoAllocated implements ICacheTiming {

        public String getReadHitTimming() {
            return "tSA+tDM";
        }

        public String getReadMissFreeTimming(int n) {
            return "tSA+" + n + "*(tOM+tDM)+tTM+tSA+tDM";
        }

        public String getReadMissNoFreeTimming(int n) {
            return "tSA+" + n + "*(tDM+tOM) +" + n + "*(tOM+tDM)+tTM+tSA+tDM";
        }

        public String getWriteHitTimming() {
            return "tSA+tDM+tOM";
        }

        public String getWriteMissFreeTimming(int n) {
            return "tSA+tOM";
        }

        public String getWriteMissNoFreeTimming(int n) {
            return "tSA+tOM";
        }

    }

    class AsocWriteThrouWriteAllocated implements ICacheTiming {

        public String getReadHitTimming() {
            return "tSA+tDM";
        }

        public String getReadMissFreeTimming(int n) {
            return "tSA+" + n + "*(tOM+tDM)+tTM+tSA+tDM";
        }

        public String getReadMissNoFreeTimming(int n) {
            return "tSA+" + n + "*(tDM+tOM)+" + n + "*(tOM+tDM)+tTM+tSA+tDM";
        }

        public String getWriteHitTimming() {
            return "tSA+tDM+tOM";
        }

        public String getWriteMissFreeTimming(int n) {
            return "tSA+" + n + "*(tOM+tDM)+tTM+tSA+tDM+tOM";
        }

        public String getWriteMissNoFreeTimming(int n) {
            return "tSA+" + n + "*(tDM+tOM)+" + n
                    + "*(tOM+tDM)+tTM+tSA+tDM+tOM";
        }

    }

    class AsocWriteBackWriteNoAllocated implements ICacheTiming {

        public String getReadHitTimming() {
            return "tSA+tDM";
        }

        public String getReadMissFreeTimming(int n) {
            return "tSA+" + n + "*(tOM+tDM)+tTM+tSA+tDM";
        }

        public String getReadMissNoFreeTimming(int n) {
            return "tSA+" + n + "*(tDM+tOM)+" + n + "*(tOM+tDM)+tTM+tSA+tDM";
        }

        public String getWriteHitTimming() {
            return "tSA+tDM";
        }

        public String getWriteMissFreeTimming(int n) {
            return "tSA+tOM";
        }

        public String getWriteMissNoFreeTimming(int n) {
            return "tSA+tOM";
        }

    }

    class AsocWriteBackWriteAllocated implements ICacheTiming {

        public String getReadHitTimming() {
            return "tSA+tDM";
        }

        public String getReadMissFreeTimming(int n) {
            return "tSA+" + n + "*(tOM+tDM)+tTM+tSA+tDM";
        }

        public String getReadMissNoFreeTimming(int n) {
            return "tSA+" + n + "*(tDM+tOM)+" + n + "*(tOM+tDM)+tTM+tSA+tDM";
        }

        public String getWriteHitTimming() {
            return "tSA+tDM";
        }

        public String getWriteMissFreeTimming(int n) {
            return "tSA+" + n + "*(tOM+tDM)+tTM+tSA+tDM";
        }

        public String getWriteMissNoFreeTimming(int n) {
            return "tSA+" + n + "*(tDM+tOM)+" + n + "*(tOM+tDM)+tTM+tSA+tDM";
        }

    }

    class SetWriteThrouWriteNoAllocated implements ICacheTiming {

        public String getReadHitTimming() {
            return "tSS";
        }

        public String getReadMissFreeTimming(int n) {
            return "tSS+" + n + "*(tOM+tDM)+tTM+tSS";
        }

        public String getReadMissNoFreeTimming(int n) {
            return "tSS+" + n + "*(tDM+tOM)+" + n + "*(tOM+tDM)+tTM+tSS";
        }

        public String getWriteHitTimming() {
            return "tSS+tDM+tOM";
        }

        public String getWriteMissFreeTimming(int n) {
            return "tSS+tOM";
        }

        public String getWriteMissNoFreeTimming(int n) {
            return "tSS+tOM";
        }

    }

    class SetWriteThrouWriteAllocated implements ICacheTiming {

        public String getReadHitTimming() {
            return "tSS";
        }

        public String getReadMissFreeTimming(int n) {
            return "tSS+" + n + "*(tOM+tDM)+tTM+tSS";
        }

        public String getReadMissNoFreeTimming(int n) {
            return "tSS+" + n + "*(tDM+tOM)+" + n + "*(tOM+tDM)+tTM+tSS";
        }

        public String getWriteHitTimming() {
            return "tSS+tDM+tOM";
        }

        public String getWriteMissFreeTimming(int n) {
            return "tSS+" + n + "*(tOM+tDM)+tTM+tSS+tDM+tOM";
        }

        public String getWriteMissNoFreeTimming(int n) {
            return "tSS+" + n + "*(tDM+tOM)+" + n
                    + "*(tOM+tDM)+tTM+tSS+tDM+tOM";
        }

    }

    class SetWriteBackWriteNoAllocated implements ICacheTiming {

        public String getReadHitTimming() {
            return "tSS";
        }

        public String getReadMissFreeTimming(int n) {
            return "tSS+" + n + "*(tOM+tDM)+tTM+tSS";
        }

        public String getReadMissNoFreeTimming(int n) {
            return "tSS+" + n + "*(tDM+tOM)+" + n + "*(tOM+tDM)+tTM+tSS";
        }

        public String getWriteHitTimming() {
            return "tSS+tDM";
        }

        public String getWriteMissFreeTimming(int n) {
            return "tSS+tOM";
        }

        public String getWriteMissNoFreeTimming(int n) {
            return "tSS+tOM";
        }

    }

    class SetWriteBackWriteAllocated implements ICacheTiming {

        public String getReadHitTimming() {
            return "tSS";
        }

        public String getReadMissFreeTimming(int n) {
            return "tSS+" + n + "*(tOM+tDM)+tTM+tSS";
        }

        public String getReadMissNoFreeTimming(int n) {
            return "tSS+" + n + "*(tDM+tOM)+" + n + "*(tOM+tDM)+tTM+tSS";
        }

        public String getWriteHitTimming() {
            return "tSS+tDM";
        }

        public String getWriteMissFreeTimming(int n) {
            return "tSS+" + n + "*(tOM+tDM)+tTM+tSS+tDM";
        }

        public String getWriteMissNoFreeTimming(int n) {
            return "tSS+" + n + "*(tDM+tOM)+" + n + "*(tOM+tDM)+tTM+tSS+tDM";
        }

    }

    class DirWriteThrouWriteNoAllocatedSimple implements ICacheTiming {

        public String getReadHitTimming() {
            return "tcm";
        }

        public String getReadMissFreeTimming(int n) {
            return "tcm+tb";
        }

        public String getReadMissNoFreeTimming(int n) {
            return "tcm+2*tb";
        }

        public String getWriteHitTimming() {
            return "tcm+tom";
        }

        public String getWriteMissFreeTimming(int n) {
            return "tcm+tom";
        }

        public String getWriteMissNoFreeTimming(int n) {
            return "tcm+tom";
        }

    }

    class DirWriteThrouWriteAllocatedSimple implements ICacheTiming {

        public String getReadHitTimming() {
            return "tcm";
        }

        public String getReadMissFreeTimming(int n) {
            return "tcm+tb";
        }

        public String getReadMissNoFreeTimming(int n) {
            return "tcm+2*tb";
        }

        public String getWriteHitTimming() {
            return "tcm+tom";
        }

        public String getWriteMissFreeTimming(int n) {
            return "tcm+tb+tom";
        }

        public String getWriteMissNoFreeTimming(int n) {
            return "tcm+2*tb+tom";
        }

    }

    class DirWriteBackWriteNoAllocatedSimple implements ICacheTiming {

        public String getReadHitTimming() {
            return "tcm";
        }

        public String getReadMissFreeTimming(int n) {
            return "tcm+tb";
        }

        public String getReadMissNoFreeTimming(int n) {
            return "tcm+2*tb";
        }

        public String getWriteHitTimming() {
            return "tcm";
        }

        public String getWriteMissFreeTimming(int n) {
            return "tcm+tom";
        }

        public String getWriteMissNoFreeTimming(int n) {
            return "tcm+tom";
        }

    }

    class DirWriteBackWriteAllocatedSimple implements ICacheTiming {

        public String getReadHitTimming() {
            return "tcm";
        }

        public String getReadMissFreeTimming(int n) {
            return "tcm+tb";
        }

        public String getReadMissNoFreeTimming(int n) {
            return "tcm+2*tb";
        }

        public String getWriteHitTimming() {
            return "tcm";
        }

        public String getWriteMissFreeTimming(int n) {
            return "tcm+tb";
        }

        public String getWriteMissNoFreeTimming(int n) {
            return "tcm+2*tb";
        }

    }

    class AsocWriteThrouWriteNoAllocatedSimple implements ICacheTiming {

        public String getReadHitTimming() {
            return "tcm";
        }

        public String getReadMissFreeTimming(int n) {
            return "tcm+tb";
        }

        public String getReadMissNoFreeTimming(int n) {
            return "tcm+2*tb";
        }

        public String getWriteHitTimming() {
            return "tcm+tom";
        }

        public String getWriteMissFreeTimming(int n) {
            return "tcm+tom";
        }

        public String getWriteMissNoFreeTimming(int n) {
            return "tcm+tom";
        }

    }

    class AsocWriteThrouWriteAllocatedSimple implements ICacheTiming {

        public String getReadHitTimming() {
            return "tcm";
        }

        public String getReadMissFreeTimming(int n) {
            return "tcm+tb";
        }

        public String getReadMissNoFreeTimming(int n) {
            return "tcm+2*tb";
        }

        public String getWriteHitTimming() {
            return "tcm+tom";
        }

        public String getWriteMissFreeTimming(int n) {
            return "tcm+tb+tom";
        }

        public String getWriteMissNoFreeTimming(int n) {
            return "tcm+2*tb+tom";
        }

    }

    class AsocWriteBackWriteNoAllocatedSimple implements ICacheTiming {

        public String getReadHitTimming() {
            return "tcm";
        }

        public String getReadMissFreeTimming(int n) {
            return "tcm+tb";
        }

        public String getReadMissNoFreeTimming(int n) {
            return "tcm+2*tb";
        }

        public String getWriteHitTimming() {
            return "tcm";
        }

        public String getWriteMissFreeTimming(int n) {
            return "tcm+tom";
        }

        public String getWriteMissNoFreeTimming(int n) {
            return "tcm+tom";
        }

    }

    class AsocWriteBackWriteAllocatedSimple implements ICacheTiming {

        public String getReadHitTimming() {
            return "tcm";
        }

        public String getReadMissFreeTimming(int n) {
            return "tcm+tb";
        }

        public String getReadMissNoFreeTimming(int n) {
            return "tcm+2*tb";
        }

        public String getWriteHitTimming() {
            return "tcm";
        }

        public String getWriteMissFreeTimming(int n) {
            return "tcm+tb";
        }

        public String getWriteMissNoFreeTimming(int n) {
            return "tcm+2*tb";
        }

    }

    class SetWriteThrouWriteNoAllocatedSimple implements ICacheTiming {

        public String getReadHitTimming() {
            return "tcm";
        }

        public String getReadMissFreeTimming(int n) {
            return "tcm+tb";
        }

        public String getReadMissNoFreeTimming(int n) {
            return "tcm+2*tb";
        }

        public String getWriteHitTimming() {
            return "tcm+tom";
        }

        public String getWriteMissFreeTimming(int n) {
            return "tcm+tom";
        }

        public String getWriteMissNoFreeTimming(int n) {
            return "tcm+tom";
        }

    }

    class SetWriteThrouWriteAllocatedSimple implements ICacheTiming {

        public String getReadHitTimming() {
            return "tcm";
        }

        public String getReadMissFreeTimming(int n) {
            return "tcm+tb";
        }

        public String getReadMissNoFreeTimming(int n) {
            return "tcm+2*tb";
        }

        public String getWriteHitTimming() {
            return "tcm+tom";
        }

        public String getWriteMissFreeTimming(int n) {
            return "tcm+tb+tom";
        }

        public String getWriteMissNoFreeTimming(int n) {
            return "tcm+2*tb+tom";
        }

    }

    class SetWriteBackWriteNoAllocatedSimple implements ICacheTiming {

        public String getReadHitTimming() {
            return "tcm";
        }

        public String getReadMissFreeTimming(int n) {
            return "tcm+tb";
        }

        public String getReadMissNoFreeTimming(int n) {
            return "tcm+2*tb";
        }

        public String getWriteHitTimming() {
            return "tcm";
        }

        public String getWriteMissFreeTimming(int n) {
            return "tcm+tom";
        }

        public String getWriteMissNoFreeTimming(int n) {
            return "tcm+tom";
        }

    }

    class SetWriteBackWriteAllocatedSimple implements ICacheTiming {

        public String getReadHitTimming() {
            return "tcm";
        }

        public String getReadMissFreeTimming(int n) {
            return "tcm+tb";
        }

        public String getReadMissNoFreeTimming(int n) {
            return "tcm+2*tb";
        }

        public String getWriteHitTimming() {
            return "tcm";
        }

        public String getWriteMissFreeTimming(int n) {
            return "tcm+tb";
        }

        public String getWriteMissNoFreeTimming(int n) {
            return "tcm+2*tb";
        }

    }
}

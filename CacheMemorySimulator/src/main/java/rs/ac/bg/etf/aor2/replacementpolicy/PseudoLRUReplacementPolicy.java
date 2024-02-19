package rs.ac.bg.etf.aor2.replacementpolicy;

import rs.ac.bg.etf.aor2.memory.MemoryOperation;
import rs.ac.bg.etf.aor2.memory.cache.ICacheMemory;
import rs.ac.bg.etf.aor2.memory.cache.Tag;

import java.util.ArrayList;

public class PseudoLRUReplacementPolicy implements IReplacementPolicy {
    protected ICacheMemory ICacheMemory;
    protected int[] LRUCnts;
    protected int setAsoc;

    public PseudoLRUReplacementPolicy() {
        LRUCnts = new int[1];
        LRUCnts[0] = 0;
    }

    public void init(ICacheMemory c) {
        this.ICacheMemory = c;
        setAsoc = (int) c.getSetAsociativity();
        int size = (int) ICacheMemory.getSetNum();

        LRUCnts = new int[size];

        for (int i = 0; i < size; i++) {
            LRUCnts[i] = 0;
        }
        if (setAsoc != 4)
            throw new RuntimeException(
                    "Implemented PseudoLRU support only size 4");
    }

    public int getBlockIndexToReplace(long adr) {
        int set = (int) ICacheMemory.extractSet(adr);
        return set * setAsoc + getEntry(adr);
    }


    private int getEntry(long adr) {
        int set = (int) ICacheMemory.extractSet(adr);
        ArrayList<Tag> tagMemory = ICacheMemory.getTags();
        int result = 0;
        for (int i = 0; i < setAsoc; i++) {
            int block = set * setAsoc + i;
            Tag tag = tagMemory.get(block);
            if (!tag.V) {
                return i;
            }
        }
        int LRUCnt = LRUCnts[set];
        int convert[] = {3, 3, 2, 2, 1, 0, 1, 0};
        result = convert[LRUCnt & 7];

        return result;
    }

    @Override
    public void doOperation(MemoryOperation operation) {

        MemoryOperation.MemoryOperationType opr = operation.getType();

        if ((opr == MemoryOperation.MemoryOperationType.READ)
                || (opr == MemoryOperation.MemoryOperationType.WRITE)) {

            long adr = operation.getAddress();
            int set = (int) ICacheMemory.extractSet(adr);
            long tagTag = ICacheMemory.extractTag(adr);
            ArrayList<Tag> tagMemory = ICacheMemory.getTags();
            int entry = 0;
            for (int i = 0; i < setAsoc; i++) {
                int block = set * setAsoc + i;
                Tag tag = tagMemory.get(block);
                if (tag.V && (tag.tag == tagTag)) {
                    entry = i;
                    break;
                }
            }
            int LRUCnt = LRUCnts[set];
            LRUCnt = LRUCnt & 7;
            switch (entry) {
                case 0:
                    LRUCnt = LRUCnt & 2;
                    break;
                case 1:
                    LRUCnt = (LRUCnt & 2) | 1;
                    break;
                case 2:
                    LRUCnt = (LRUCnt & 1) | 4;
                    break;
                case 3:
                    LRUCnt = (LRUCnt & 1) | 6;
                    break;
            }
            LRUCnts[set] = LRUCnt;

        } else if (operation.getType() == MemoryOperation.MemoryOperationType.FLUSHALL) {
            for (int i = 0; i < LRUCnts.length; i++) {
                LRUCnts[i] = 0;
            }

        }
    }

    public String printAll() {
        String s = "";
        int size = LRUCnts.length;
        for (int i = 0; i < size; i++) {
            s = s + "Set " + i + ", Pseudo LRU counter " + LRUCnts[i] + "\n";
        }
        return s;
    }

    @Override
    public void reset() {
        for (int i = 0; i < LRUCnts.length; i++) {
            LRUCnts[i] = 0;
        }
    }

    public String printValid() {
        String s = "";
        int setAsoc = (int) ICacheMemory.getSetAsociativity();
        int setNumber = (int) ICacheMemory.getSetNum();
        ArrayList<Tag> tagMemory = ICacheMemory.getTags();
        for (int set = 0; set < setNumber; set++) {
            boolean valid = false;
            for (int j = 0; j < setAsoc; j++) {
                int block = set * setAsoc + j;
                Tag tag = tagMemory.get(block);
                if (tag.V) {
                    valid = true;
                }
            }
            if (valid) {
                s = s + "Set " + set + ", Pseudo LRU counter " + LRUCnts[set]
                        + "\n";
            }
        }
        return s;
    }
}

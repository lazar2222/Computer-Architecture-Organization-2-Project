package rs.ac.bg.etf.aor2.memory.cache;

public class Tag {
    public boolean V;
    public long tag;
    public int tagLen;
    private boolean D;
    private boolean writeBack;

    public Tag(int tagLen, boolean writeBack) {
        this.tagLen = tagLen;
        this.tag = 0;
        this.V = false;
        this.D = false;
        this.writeBack = writeBack;
    }

    public Tag() {
        this(0, true);
    }

    public String toString() {
        String s = "" + "V = " + (V ? 1 : 0)
                + (writeBack ? (", D = " + (D ? 1 : 0)) : "") + ", Tag = ";

        String t = Long.toBinaryString(tag);
        int length = t.length();
        for (int i = length; i < tagLen; i++) {
            t = "0" + t;
        }
        return s + t;
    }

    public boolean isDirty() {
        return D;
    }

    public void setDirty(boolean D) {
        this.D = D;
    }
}

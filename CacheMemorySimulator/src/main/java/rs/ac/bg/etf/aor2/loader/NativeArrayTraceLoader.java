package rs.ac.bg.etf.aor2.loader;

import rs.ac.bg.etf.aor2.memory.MemoryOperation;

import java.util.Iterator;
import java.util.LinkedList;

public class NativeArrayTraceLoader implements ITraceLoader {

    public static class NativeOperation{
        MemoryOperation memoryOperation;
        boolean isInstruction;

        private NativeOperation(MemoryOperation mo, boolean isInstruction){
            this.memoryOperation=mo;
            this.isInstruction=isInstruction;
        }

        public static NativeOperation ReadInst(long addrress){
            return new NativeOperation(MemoryOperation.read(addrress),true);
        }
        public static NativeOperation ReadData(long addrress){
            return new NativeOperation(MemoryOperation.read(addrress),false);
        }
        public static NativeOperation WriteData(long addrress){
            return new NativeOperation(MemoryOperation.write(addrress),false);
        }

    }

    LinkedList<NativeOperation> operations = new LinkedList<>();
    Iterator<NativeOperation> iterator;

    boolean isInstrucion=false;

    public NativeArrayTraceLoader(MemoryOperation... memoryOperation) {
        for (MemoryOperation op : memoryOperation) {
            if (op != null)
                operations.add(new NativeOperation(op,false));
        }
        iterator = operations.iterator();
    }

    public NativeArrayTraceLoader(NativeOperation... nativeOperation) {
        for (NativeOperation op : nativeOperation) {
            if (op != null)
                operations.add(op);
        }
        iterator = operations.iterator();
    }



    @Override
    public MemoryOperation getNextOperation() {
        if(hasOperationToLoad()){
            final NativeOperation next = iterator.next();
            isInstrucion=next.isInstruction;
            return next.memoryOperation;

        }else  return  null;

    }

    @Override
    public boolean isInstructionOperation() {
        return isInstrucion;
    }

    @Override
    public boolean hasOperationToLoad() {
        return iterator.hasNext();

    }

    @Override
    public void reset() {
        iterator = operations.iterator();

    }
}

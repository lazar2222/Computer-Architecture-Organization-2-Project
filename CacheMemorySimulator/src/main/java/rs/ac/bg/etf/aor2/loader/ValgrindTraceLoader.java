package rs.ac.bg.etf.aor2.loader;

import rs.ac.bg.etf.aor2.memory.MemoryOperation;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;

public class ValgrindTraceLoader implements ITraceLoader, Closeable {

    private BufferedReader bufferedReader;

    private final String fileName;

    private String currentLine = null;
    private String currentOperation;
    private long address;
    private int blockNumber;
    private int line=0;
    private long guard;

    public ValgrindTraceLoader(String fileName,long guard) throws IOException {
        this.fileName = fileName;
        this.guard=guard;
        this.bufferedReader = new BufferedReader(new FileReader(fileName));
        readNewLine();
    }

    private void parseCurrentLine() {
    	line++;
    	if(line%100000==0) 
    	{
    		System.out.println("Lines "+line);
    	}
        try {
            boolean currentLineHasValidFormat = false;

            while (!currentLineHasValidFormat) {
                if (currentLine == null) {
                    return;
                }
                /*
                    possible format [operation address, number of data]:
                        SB 04017e70
                        I  04017e70,4
                         L 1ffefffe2f,1
                         S 1ffefffda8,8
                         M 0501c628,4
                    -------------
                               SB: superblock (basic block) [ignore]
                                L:  load   [rd]
                                S:  store  [wr]
                                M:  modify [rd+wr]
                     other format: [ignore]
                 */

                currentOperation = currentLine.substring(0, 2).trim().toUpperCase();

                switch (currentOperation) {
                    case "I":
                    case "L":
                    case "S":
		    case "M":
                        try {
                            int comma = currentLine.lastIndexOf(',');
                            address = Long.parseLong(currentLine.substring(3, comma), 16);
                            blockNumber = Integer.parseInt(currentLine.substring(comma + 1));
                            currentLineHasValidFormat = true;
                        } catch (Exception e) {
                    }
                    // other operation (SB or other format) => ignore (currentLineHasValidFormat = false;)
                }

                if (!currentLineHasValidFormat) {
                    currentLine = bufferedReader.readLine();
                }
            }
        } catch (Exception e) {
            readNewLine();
        }
    }

    /**
     * This method is used to read next operation.
     *
     * @return MemoryOperation This returns MemoryOperation.
     */
    @Override
    public MemoryOperation getNextOperation() {
        if (!hasOperationToLoad()) {
            return null;
        }

        MemoryOperation result = null;
        switch (currentOperation) {
            case "I":
            case "L":
            case "M":
                result = MemoryOperation.read(address);
                break;
            case "S":
                result = MemoryOperation.write(address);
                break;
        }
        address++;
        blockNumber--;
        if (blockNumber == 0) {
            if (currentOperation.equals("M")) {
                currentLine = currentLine.replace('M', 'S');
                parseCurrentLine();
            } else {
                readNewLine();
            }
        }
        return result;
    }

    /**
     * This method is used to check if there is more operation to read.
     *
     * @return boolean This returns true, if there is more operation to read, otherwise returns false.
     */
    @Override
    public boolean hasOperationToLoad() {
        return currentLine != null && line<=guard;
    }

     /**
     * This method is used to reset TraceLoader. File starts from begin;
     */
    @Override
    public void reset() {
        try {
            close();
        } catch (Exception e) {
        }
        try {
            bufferedReader = new BufferedReader(new FileReader(fileName));
            readNewLine();
        } catch (Exception e) {
        }
    }

    private void readNewLine() {
        try {
            currentLine = bufferedReader.readLine();
            parseCurrentLine();
        } catch (IOException e) {
            currentLine = null;
        }

    }

    /**
     * This method is used to close file handle.
     */
    @Override
    public void close() throws IOException {
        bufferedReader.close();
        currentLine = null;
    }

    /**
     * This method is used to check if current operation is instruction read.
     *
     * @return boolean This returns true, if current operation is instruction read, otherwise returns false.
     */
    @Override
    public boolean isInstructionOperation() {
        return currentOperation != null && "I".equals(currentOperation);
    }
}

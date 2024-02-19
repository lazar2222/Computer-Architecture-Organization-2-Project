package rs.ac.bg.etf.aor2.replacementpolicy;

import rs.ac.bg.etf.aor2.memory.MemoryOperation;
import rs.ac.bg.etf.aor2.memory.cache.ICacheMemory;
import rs.ac.bg.etf.aor2.memory.cache.Tag;

import java.util.ArrayList;

public class LRUReplacementPolicy implements IReplacementPolicy {

    protected ArrayList<Integer> counters;
    protected ICacheMemory ICacheMemory;

    public LRUReplacementPolicy() {
        counters = new ArrayList<Integer>();
    }

    public void init(ICacheMemory c) {
        /* ZADATAK METODE:
         * postaviti neophodne parametre koji zavise od kes memorije
         * RESENJE:
         * Kod LRU algoritma zamene,
         * potrebno je da postoji onoliko counter-a koliko i blokova
         * inicijalno svi brojaci imaju vrednost 0
         */

        this.ICacheMemory = c;
        int size = (int) ICacheMemory.getBlockNum();
        counters = new ArrayList<Integer>(size);
        for (int i = 0; i < size; i++) {
            counters.add(0);
        }

    }


    public int getBlockIndexToReplace(long adr) {
        /* ZADATAK METODE:
         * vratiti poziciju adrese (unutar celokupnog niza blokova u kesu) koja treba da se izbaci iz kesa
         * RESENJE:
         * kod LRU algoritma, blok za izbacivanje je onaj ciji counter ima vrednost 0
         *
         *
         * Potrebno je pronaci set u kome pripada adresa
         * zatim je potrebno iterirati kroz sve brojace i
         * treba naci onaj koji ima vrednost 0 ili ciji ulaz nije validan.
         */
        int setAsoc = (int) ICacheMemory.getSetAsociativity();
        int set = (int) ICacheMemory.extractSet(adr);

        ArrayList<Tag> tags = ICacheMemory.getTags();
        int result = 0;
        for (int i = 0; i < setAsoc; i++) {
            Integer counter = counters.get(set * setAsoc + i);
            if (counter == 0 || !tags.get(set * setAsoc + i).V) {
                result = i;
                break;
            }
        }
        return set * setAsoc + result;
    }

    @Override
    public void doOperation(MemoryOperation operation) {
        /* ZADATAK METODE:
         * obraditi slucaj kada se se pronasla adresa unutar kesa
         * kod LRU algoritma zamene u slucaju kada se vrsi
         * operacija citanja ili pisanja potrebno je promeniti stanje counter-a
         * U slucaju da se radi o operaciji FLUSHALL,
         * potrebno je resetovati brojace svih blokova
         *
         * RESENJE:
         * ispitati parametar opr i
         * ako je citanje ili pisanje:
         * 	treba da se nadje brojac validnog bloka kod kog se desio hit
         * 	tag (parametra adr) == tag (adresa bloka u memoriji)
         * 	sigurni smo da je bilo hit-a pa cemo sigurno naci indeks tog bloka
         * 	na osnovu indeksa, mozemo da dohvatimo i counter tog bloka
         * 	potrebno je sada iterirati kroz sve countere i promeniti im stanje i to:
         * 	ako je counter iterirajuceg bloka veci od counter-a bloka gde je bio hit,
         * 			onda treba dekrementirati vrednost counter-a iterirajuceg bloka
         * 	ako je counter iterirajuceg bloka jednak counter-u bloka gde je bio hit (isti counter),
         * 			onda treba postaviti vrednost na maksimalnu vrednost
         * 			(velicina asocijativne memorije - 1 => setAsoc-1)
         * 	ako je counter iterirajuceg bloka manji od counter-a bloka gde je bio hit,
         * 			nista ne raditi
         * ako je FLUSHALL:	potrebno je iterirati kroz sve brojace u kesu i postaviti im vrednost 0
         */
        MemoryOperation.MemoryOperationType opr = operation.getType();
        long adr = operation.getAddress();
        if ((opr == MemoryOperation.MemoryOperationType.READ)
                || (opr == MemoryOperation.MemoryOperationType.WRITE)) {
            int set = (int) ICacheMemory.extractSet(adr);
            int setAsoc = (int) ICacheMemory.getSetAsociativity();
            long tagData = ICacheMemory.extractTag(adr);
            ArrayList<Tag> tagMemory = ICacheMemory.getTags();

            int cnts[] = new int[setAsoc];
            int index = 0;        //index bloka kod kog se desio hit
            int oldCnt = 0;        //vrednost counter bloka kod kog se desio hit
            for (int i = 0; i < setAsoc; i++) {
                int block = set * setAsoc + i;
                Tag tag = tagMemory.get(block);
                cnts[i] = counters.get(block);
                if (tag.V && tag.tag == tagData) {
                    oldCnt = cnts[i];
                    index = i;
                }
            }
            for (int i = 0; i < setAsoc; i++) {
                int block = set * setAsoc + i;
                if (cnts[i] > oldCnt) {
                    counters.set(block, cnts[i] - 1);
                } else if (i == index) {
                    counters.set(block, setAsoc - 1);
                } else {
                    // stanje ostaje isto
                    //counters.set(block, new Integer(cnts[i]));

                }
            }

        } else if (opr == MemoryOperation.MemoryOperationType.FLUSHALL) {
            for (int i = 0; i < counters.size(); i++) {
                counters.set(i, 0);
            }
        }

    }


    public String printAll() {
        StringBuilder s = new StringBuilder();
        int size = counters.size();
        int setAsoc = (int) ICacheMemory.getSetAsociativity();
        for (int i = 0; i < size; i++) {
            int set = i / setAsoc;
            s = s.append("Block ").append(i).append(", Set ").append(set + ", LRU counter ")
                    .append(counters.get(i).intValue()).append("\n");
        }
        return s.toString();
    }

    @Override
    public void reset() {
        for (int i = 0; i < counters.size(); i++) {
            counters.set(i, 0);
        }
    }

    public String printValid() {
        StringBuilder s = new StringBuilder();
        int setAsoc = (int) ICacheMemory.getSetAsociativity();
        int setNumber = (int) ICacheMemory.getSetNum();
        ArrayList<Tag> tagMemory = ICacheMemory.getTags();
        for (int set = 0; set < setNumber; set++) {
            StringBuilder validCnt = new StringBuilder();
            boolean valid = false;
            for (int j = 0; j < setAsoc; j++) {
                int block = set * setAsoc + j;
                Tag tag = tagMemory.get(block);
                validCnt = validCnt.append("Block ").append(block).append(", Set ").append(set).append(", LRU counter ")
                        .append(counters.get(block).intValue()).append("\n");
                if (tag.V) {
                    valid = true;
                }
            }
            if (valid) {
                s = s.append(validCnt);
            }
        }
        return s.toString();
    }
}

package rs.ac.bg.etf.aor2.replacementpolicy;

import rs.ac.bg.etf.aor2.memory.MemoryOperation;
import rs.ac.bg.etf.aor2.memory.cache.ICacheMemory;
import rs.ac.bg.etf.aor2.memory.cache.Tag;

import java.util.ArrayList;

public class FIFOReplacementPolicy implements IReplacementPolicy {

    protected ArrayList<Integer> counters;
    protected ICacheMemory ICacheMemory;

    public FIFOReplacementPolicy() {
        counters = new ArrayList<Integer>();
    }

    public void init(ICacheMemory c) {
        /* ZADATAK METODE:
         * postaviti neophodne parametre koji zavise od kes memorije
         * RESENJE:
         * Kod FIFO algoritma zamene,
         * potrebno je da postoji onoliko counter-a koliko i setova (setNum)
         * inicijalno svi brojaci ukazuju na nulti ulaz svakog seta
         */

        this.ICacheMemory = c;
        int size = (int) ICacheMemory.getSetNum();
        counters = new ArrayList<Integer>(size);

        // postavljamo vrednost 0 brojaca za svaki set
        for (int i = 0; i < size; i++) {
            counters.add(0);
        }
    }

    public int getBlockIndexToReplace(long adr) {
        /* ZADATAK METODE:
         * vratiti poziciju bloka (unutar celokupnog niza blokova u kesu) koji treba da se izbaci iz kesa
         * RESENJE:
         * kod FIFO algoritma counter nam govori koji je sledeci blok za izbacivanje,
         * nakon izbacenog bloka, sledeci na redu (u tom setu) za izbacivanje
         * se nalazi na poziciji izbacenog bloka + 1 po modulu velicine seta
         *
         * Potrebno je pronaci set u koji bi treba da se stavi nova adresa (parametar long adr)
         * da bi se dohvatio brojac tog seta
         * na osnovu brojaca (vrednost brojaca = pozicija) znamo koja adresa treba da bude izbacena iz kesa
         * rezultat metode je sracunata pozicija te adrese UNUTAR CELOKUPNOG NIZA blokova u kesu
         * u tom setu je potrebno i inkrementirati brojac (da bi se ukazivalo na sledeci blok za izbacivanje)
         */

        int setAsoc = (int) ICacheMemory.getSetAsociativity();
        int set = (int) ICacheMemory.extractSet(adr);
		
		/*Potrebno, jedino ako postoji operacija FLUSHADR 
		
		ArrayList<Tag> tags = ICacheMemory.getTags();
		for (int i = 0; i < setAsoc; i++) {
			if (!tags.get(set * setAsoc + i).V) {
				result = i;
				counters.set(set, new Integer((i + 1) % setAsoc));
				return set * setAsoc + i;
			}
		}
		*/

        Integer counter = counters.get(set);
        counters.set(set, (counter + 1) % setAsoc);
        return set * setAsoc + counter;
    }

    @Override
    public void doOperation(MemoryOperation operation) {
        /* ZADATAK METODE:
         * obraditi slucaj kada se se pronasla adresa unutar kesa
         * u FIFO algoritmu zamene ne moramo nista da radimo pri operacijama citanja i pisanja,
         * jedina operacija koja treba da se obradi je FLUSHALL,
         * sto bi trebalo da resetuje brojace svih setova
         * RESENJE:
         * ispitati da li parametar opr ukazuje na to da se radi o operaciji FLUSHALL
         * ako jeste: potrebno je iterirati kroz sve brojace u kesu i postaviti im vrednost 0
         * ako nije: nista ne raditi
         */

        if (operation.getType() == MemoryOperation.MemoryOperationType.FLUSHALL) {
            for (int i = 0; i < counters.size(); i++) {
                counters.set(i, 0);
            }
        }
    }

    public String printAll() {
        StringBuilder s = new StringBuilder();
        int size = counters.size();
        for (int i = 0; i < size; i++) {
            s = s.append("Set ").append(i).append(", FIFO counter ").append(counters.get(i)).append("\n");
        }
        return s.toString();
    }

    @Override
    public void reset() {

        for (int i = 0; i < counters.size(); i++)
            counters.set(i, 0);
    }

    public String printValid() {
        StringBuilder s = new StringBuilder();
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
                    break;
                }
            }
            if (valid) {
                s = s.append("Set ").append(set).append(", FIFO counter ").append(counters.get(set).intValue())
                        .append("\n");
            }
        }
        return s.toString();
    }
}

package rs.ac.bg.etf.automaton;

public class Automaton2bits_1 implements Automaton {

	private int state;

	Automaton2bits_1(int state) {
		this.state = state;
	}
	Automaton2bits_1() {
		this(0);
	}

	@Override
	public void updateAutomaton(boolean outcome) {
		switch (state) {
		case 0:
			if(outcome) state = 1;
			break;
		case 1:
			if(outcome) state = 3;
			else state = 2;
			break;
		case 2:
			if(outcome) state = 1;
			else state = 0;
			break;
		case 3:
			if(outcome) state = 3;
			else state = 2;
			break;
		default:
			break;
		}
	}

	@Override
	public boolean predict() {
		return state != 0;
	}
}

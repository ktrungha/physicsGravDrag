package contraction;


public class State {
	private long aPos, bPos;

	State(long aPos, long bPos) {
		this.aPos = aPos;
		this.bPos = bPos;
	}
	
	public long getaPos() {
		return aPos;
	}
	
	public long getbPos() {
		return bPos;
	}
	
	void changeAPos(long val) {
		aPos += val;
	}

	void changeBPos(long val) {
		bPos += val;
	}
}
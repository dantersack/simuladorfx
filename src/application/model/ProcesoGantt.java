package application.model;

public class ProcesoGantt {

	private int id;
	private int t;

	public ProcesoGantt(int id, int t) {
		this.id = id;
		this.t = t;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getT() {
		return t;
	}

	public void setT(int t) {
		this.t = t;
	}

}

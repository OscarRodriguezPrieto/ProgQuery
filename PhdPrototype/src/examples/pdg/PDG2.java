package examples.pdg;

public class PDG2 implements PDG1.I {
	public PDG1 pdg;
	public PDG1[][][] tensor;

	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * Multiple comment to keep ordered the results of the query
	 * 
	 * 
	 * 
	 * 
	 * null
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	public void m(PDG1 pdg) {
		// pdg state mod
		pdg.i = 2;

		pdg.i = 5;
	}

	public void m2(PDG1 pdg) {
		// pdg state mod through method
		pdg.setI(2);
	}

	public void m3(PDG1 pdg) {

		this.pdg = null;
	}

	public void m4(PDG1 pdg) {
		if (pdg != null)
			this.pdg = null;
	}

	public void m5(PDG1 pdg) {
		if (pdg != null)
			this.pdg = null;
		else
			this.pdg = new PDG1();
	}

	public void m6(PDG1 pdg) {

		pdg = new PDG1();
		// pdg state mod
		pdg.i = 2;

		pdg.i = 5;
	}

	public void m7(PDG1 pdg) {
		if (pdg.i == 5)
			pdg = new PDG1();
		// pdg state mod
		pdg.i = 2;

		pdg.i = 5;
	}

	public void m8(PDG1 pdg) {
		if (pdg.i == 5)
			pdg = new PDG1();
		if (pdg.i == 2)
			pdg.i = 5;
	}

	public PDG1 getPDG1() {

		return pdg;
	}

	public PDG1[][] getTensor() {

		return tensor[0];
	}

	public int[] getPDG1Array() {
		return pdg.getArray();
	}
	// Falta pasar un array con un atributo como elemento o un objeto con un
	// atributo de esta clase como atributo
	// Está chungo ---> ES UN MAY

	public PDG2 variableRet(PDG2 p) {
		return p.hashCode() == 2 ? p : this;
	}

	public PDG2 variableRetBis(PDG2 p) {
		if (p.hashCode() == 2)
			return p;
		else
			return this;
	}

	public PDG2 thisReturn(PDG2 p) {
		return this;
	}

	public PDG2 paramReturn(PDG2 p) {
		p.m();
		return p;
	}


}

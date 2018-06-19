package examples.pdg;

import java.io.File;

import examples.classesDependencies.ClassUse;



public class A extends Z {
	static final int m = 3;
	File f;

	@Override
	public void confusing(int i, String g) {
		// VARDEC---atributo composition
		// RETURN TYPE
		// new Class
		// Static Member access
		// Class IDENT

		f = null;
		int a = ClassUse.getZ().f.hashCode() + ClassUse.getZ().confusing(g, i) + 2 + X();
	}
}

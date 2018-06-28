package examples.pdg;

import java.io.File;

import examples.classesDependencies.ClassUse;




public class A {
	static final int m = 3;
	File f;

	// @Override
	public void confusing(int i, String g) {
		// VAR DEC
		// RETURN TYPE
		// new Class
		// Static Member access or ( mInv or Ident, static access implicit-
		// excluding the class itself)
		// Solo Z.m, o Z.A.m, nunca ().().a.m aunque m sea estático
		// Distinguir Z.a.m de Z.A.m symbol(Z.A) classSymbol vs symbol(Z.a)
		// attribute symbol
		// Class IDENT

		f = null;
		int a = ClassUse.getZ().ff.hashCode() + ClassUse.getZ().confusing(g, i) + 2;
	}
}

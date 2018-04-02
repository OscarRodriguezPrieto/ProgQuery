package tests.classes;

public class A implements I{
public A(){
	am();
	asm();
}
	public void am() {
		new B().m();
		new A();
		asm();
	}

	public static void asm() {
		B.sm();
		new A().am();

	}
}

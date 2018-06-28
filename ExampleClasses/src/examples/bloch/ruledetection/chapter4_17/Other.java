package examples.bloch.ruledetection.chapter4_17;

public class Other extends Base {
	int i = 5;

	@Override
	public void method2() {
		System.out.println(i);
	}

	public static void main(String[] args) {
		new Other();
	}
}

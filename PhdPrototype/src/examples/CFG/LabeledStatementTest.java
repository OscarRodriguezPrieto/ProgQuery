package examples.CFG;

public class LabeledStatementTest {

	public static void main(String[] args) {
		int a;
		label: while (true)
			break label;
		label: for (int i = 0; i < 4; i++)
			label2: {
				System.out.println(i);
				if (i > 1)
					continue;
				System.out.println("A");
			}

	}
}

package examples.cmu;
@SuppressWarnings(value = { "" })
public class SubA extends A {

	public   SubA() {
		super.array = new int[2];
		f = null;// A F en vez de a THIS
		change(array);
	}

	public static int c;

	public final D d = null;
	private double[] otherArray;

	@Override
	public int[] getArray() {
		return array;
	}

	@Override
	public void confusing(int i, String g) { // DOS RELACIONES <-THIS REPETIDAS 'igual deberían ser this y attr'
		// array[2] = 0;
		change(d.a.array); // MAY BE MAL,
		change(this.array);// MAY BE MAL,
		change(array);// NINGUNA, FALTAN A THIS Y A ARRAY
		changee(this);
		changeIns();/*FALTA A THIS, Y A ARRAY OPCIONAL-cada vez más*/ changeIns(array);/*Falta a this, y a array*/changeInsBis();changeInsBisBis();changeInsBisBisBis();
		changeIns(this); 
		confusing(i, g);
	}

	private static void changee(SubA a) {
		a.array[7] = 5;
		change(a.array);
	}

	private static void change(int[] array) {// FALTA A PARAM
		array[7] = 5;
	}

	private void changeIns(SubA a) {
		a.array[7] = 5;
		this.otherArray[9]++;
	}

	private void changeIns() {// FALTAN THIS y ATTR
		array[7] = 5;// EN VEZ DE THIS, ATTR, tenemos ATTR,ATTR DUP
	}
	private void changeIns(int[] array) {
		array[7] = 5;
	}

	private void changeInsBis() {
		array = null;// TAMBIÉN MAL
	}

	private void changeInsBisBis() {
		otherArray = null;// BIEN, DEBE SER POR EL ATRIBUTO SUPER
	}

	private void changeInsBisBisBis() {
		otherArray[8] = 0;
	}
}

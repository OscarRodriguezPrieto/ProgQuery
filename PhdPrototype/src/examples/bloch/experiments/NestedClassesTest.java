package examples.bloch.experiments;
public class NestedClassesTest {
	private int val;
	public String toString(){
		return "NESTED TOSTRING";
	}
	
	Comparable c= new Comparable(){
		
		public void foo(){
			boolean val;
			val=false;
			System.out.println(val);
		}
		@Override
		public int compareTo(Object o) {
			return val;
		}};
}

package examples.bloch.experiments;

import java.util.ArrayList;

//@SuppressWarnings("unchecked")
/**
 * @author Oscar
 *
 * @param <E>
 */
public class AnnotationsAndWarningsTest<E> {

	public String toString(){
		E[] array=(E[]) new ArrayList<E>().toArray();
		
		return array.toString();
	}
	
	
	/**
	 * @param a2
	 */
	@SuppressWarnings("unchecked")
	public void foo(int a2){
		@SuppressWarnings("unchecked")
		int a=2;
	}
	
	
}

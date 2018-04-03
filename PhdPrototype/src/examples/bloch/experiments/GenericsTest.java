package examples.bloch.experiments;

import java.util.HashSet;
import java.util.Set;

public class GenericsTest<E> {

	
	public void foo(Set<?> s){
		Set s2=new HashSet<>();
		Set<String> s3=new HashSet<String>();
	}
}

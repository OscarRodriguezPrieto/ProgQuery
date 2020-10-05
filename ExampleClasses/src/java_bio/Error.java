package java_bio;

import java.util.Iterator;

public class Error implements Iterable<String> {

	
	
	public Iterator<String> iterator(){
		return new Iterator<String>() {

//			(){
//				super();
//			}
			int result=0;
			@Override
			public boolean hasNext() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public String next() {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
	}
}

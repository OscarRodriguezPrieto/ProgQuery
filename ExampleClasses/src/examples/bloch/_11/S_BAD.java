package examples.bloch._11;

import java.io.IOException;
import java.io.Serializable;

public class S_BAD implements Serializable {

	 private void writeObject(java.io.ObjectOutputStream out)
	 {
		 
	 }
		 private void readObject()
		     throws IOException, ClassNotFoundException{
			 
		 }
		 
		  Object readResolve() {
			return null;
			  
		  }
}

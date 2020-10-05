package examples.bloch._11;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;

public class S_COM2 implements Serializable {

	
	public Object readResolve() throws ObjectStreamException{
		return new S_COM2();
	}
}

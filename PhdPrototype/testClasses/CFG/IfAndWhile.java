
public class IfAndWhileLoop {

	public Void m(Integer i) {
		if (i == 2) {
			i=9;
			System.out.println(i);
		} else {
			while (i < 8) {
				i++;
				if (i % 2)
					return ;
			}
			if(true)
				;
		}
	}
}


public class Switch {

	public int m(Integer i) {
		Throwable t;
		final int a = 9;
		sw: switch (i) {
		case 1:
		case 2:
			i++;
			break;
		case 3 + 4:
			i += 3;
			break;
		case a:
			System.out.println("A");
			while (true) {
				if ("A".contains("K"))
					break sw;
				else
					break;
			}
		case 89:
		case 90:
		default:
			i = 0;
		case 100:
			break;
		case 100000:
			throw new IllegalStateException();
		}
return 0;
	}
}

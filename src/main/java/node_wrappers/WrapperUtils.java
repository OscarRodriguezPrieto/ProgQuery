package node_wrappers;

public class WrapperUtils {

	public static String stringToNeo4jQueryString(String string) {
		return string.replace("\\", "\\\\")
				//.replace("\"", "\\\"") 
				;
	}
}

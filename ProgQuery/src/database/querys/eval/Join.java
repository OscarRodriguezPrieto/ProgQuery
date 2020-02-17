package database.querys.eval;

import java.util.List;

import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

public class Join {

	 @UserFunction
	    @Description("example.join(['s1','s2',...], delimiter) - join the given strings with the given delimiter.")
	    public String join(
	            @Name("strings") List<String> strings,
	            @Name(value = "delimiter", defaultValue = ",") String delimiter) {
	        if (strings == null || delimiter == null) {
	            return null;
	        }
	        return String.join(delimiter, strings);
	    }
}

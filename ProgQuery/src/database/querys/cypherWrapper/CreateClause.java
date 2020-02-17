package database.querys.cypherWrapper;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CreateClause implements Clause {

	MatchElement[] elements;

	public CreateClause(MatchElement... elements) {
		super();
		this.elements = elements;
	}

	public CreateClause(String... elements) {
		super();
		this.elements = new MatchElement[elements.length];
		this.elements = Stream.of(elements).map(s -> new MatchImpl(s)).collect(Collectors.toList())
				.toArray(this.elements);
	}

	@Override
	public String clauseToString() {
		String res = "CREATE ";
		for (MatchElement match : elements)
			res += match.matchToString();
		return res;
	}

}

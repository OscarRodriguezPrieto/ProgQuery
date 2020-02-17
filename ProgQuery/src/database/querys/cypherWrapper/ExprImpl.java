package database.querys.cypherWrapper;

public class ExprImpl implements Expression {

	private String exprString;

	public ExprImpl(String exprString) {
		super();
		this.exprString = exprString;
	}
	@Override
	public String expToString() {
		// TODO Auto-generated method stub
		return exprString;
	}

}

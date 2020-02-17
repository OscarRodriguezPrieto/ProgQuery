package database.querys.eval;

import database.querys.ServerQueryManager;

public class DeleteAllServer {

	public static void main(String[] args) throws Exception {
		final String DELETE_ALL = "MATCH (N) DETACH DELETE N";
		ServerQueryManager.SERVER_MANAGER = new ServerQueryManager("Oscar", "pass", "address");
		ServerQueryManager.SERVER_MANAGER.executeQuery(DELETE_ALL);
		ServerQueryManager.SERVER_MANAGER.close();
	}
}

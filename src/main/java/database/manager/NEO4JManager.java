package database.manager;

import node_wrappers.NodeWrapper;

public interface NEO4JManager extends AutoCloseable{


    NodeWrapper getProgramFromDB(String programId, String userId) ;
}

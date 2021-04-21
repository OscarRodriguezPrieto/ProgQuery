package es.uniovi.reflection.progquery.database.manager;

import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;

public interface NEO4JManager extends AutoCloseable{


    NodeWrapper getProgramFromDB(String programId, String userId) ;
}

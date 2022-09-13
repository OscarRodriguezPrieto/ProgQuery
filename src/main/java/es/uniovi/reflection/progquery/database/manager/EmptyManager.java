package es.uniovi.reflection.progquery.database.manager;

import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;

public class EmptyManager implements NEO4JManager {

    @Override
    public NodeWrapper getProgramFromDB(String programId, String userId) {
        //We return null, just if we query an empty/non-existing database with always the same result
        return null;
    }

    @Override
    public void close() {

    }
}

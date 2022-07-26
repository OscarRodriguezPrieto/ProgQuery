package es.uniovi.reflection.progquery.database.manager;

import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.Pair;
import es.uniovi.reflection.progquery.utils.keys.external.ExternalNotDefinedTypeKey;
import es.uniovi.reflection.progquery.utils.keys.external.ExternalTypeDefKey;

import java.util.stream.Stream;

public class EmptyManager implements NEO4JManager {

    @Override
    public NodeWrapper getProgramFromDB(String programId, String userId) {
        //We return null, just if we query an empty/non-existing database with always the same result
        return null;
    }

    @Override
    public Stream<Pair<NodeWrapper, ExternalTypeDefKey>> getDeclaredMethodsFrom(String programID, String userID) {
        throw new IllegalStateException("DONT NEED TO RETRIEVE EXTERNAL CALLABLES WITH AN EMPTY MANAGER, getProgramFromDB is alwys null");
    }

    @Override
    public Stream<Pair<NodeWrapper, ExternalNotDefinedTypeKey>> getDeclaredTypeDefsFrom(String programID, String userID) {

        throw new IllegalStateException("DONT NEED TO RETRIEVE EXTERNAL TYPE DEFINITIONS WITH AN EMPTY MANAGER, getProgramFromDB is alwys null");
    }
    @Override
    public Stream<Pair<NodeWrapper, ExternalNotDefinedTypeKey>> getNotDeclaredTypesFrom(String programID, String userID) {

        throw new IllegalStateException("DONT NEED TO RETRIEVE EXTERNAL TYPES WITH AN EMPTY MANAGER, getProgramFromDB is alwys null");
    }
    @Override
    public void close() {

    }
}

package es.uniovi.reflection.progquery.database.manager;

import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.Pair;
import es.uniovi.reflection.progquery.utils.types.ExternalTypeDefKey;

import java.util.List;
import java.util.stream.Stream;


public interface NEO4JManager extends AutoCloseable {


    NodeWrapper getProgramFromDB(String programId, String userId) ;

    Stream<Pair<NodeWrapper, ExternalTypeDefKey>> getDeclaredMethodsFrom(String programID, String userID) ;


    Stream<Pair<NodeWrapper, ExternalTypeDefKey>> getDeclaredTypeDefsFrom(String programID, String userID) ;

    @Override
    void close();
}
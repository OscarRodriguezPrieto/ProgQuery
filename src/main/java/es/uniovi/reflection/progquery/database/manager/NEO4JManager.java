package es.uniovi.reflection.progquery.database.manager;

import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.Pair;
import es.uniovi.reflection.progquery.utils.keys.external.ExternalNotDefinedTypeKey;
import es.uniovi.reflection.progquery.utils.keys.external.ExternalTypeDefKey;

import java.util.stream.Stream;


public interface NEO4JManager extends AutoCloseable {


    NodeWrapper getProgramFromDB(String programId, String userId) ;

    Stream<Pair<NodeWrapper, ExternalTypeDefKey>> getDeclaredMethodsFrom(String programID, String userID) ;


    Stream<Pair<NodeWrapper, ExternalNotDefinedTypeKey>> getNotDeclaredTypesFrom(String programID, String userID) ;
    Stream<Pair<NodeWrapper, ExternalNotDefinedTypeKey>> getDeclaredTypeDefsFrom(String programID, String userID) ;

    @Override
    void close();
}

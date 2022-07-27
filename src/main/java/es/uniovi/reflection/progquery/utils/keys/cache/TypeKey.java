package es.uniovi.reflection.progquery.utils.keys.cache;

import es.uniovi.reflection.progquery.utils.keys.external.ExternalNotDefinedTypeKey;
import es.uniovi.reflection.progquery.utils.keys.external.ExternalTypeDefKey;

public interface TypeKey {

    String nodeType();

    default ExternalNotDefinedTypeKey getExternalKey(){
      return  new ExternalNotDefinedTypeKey(toString(),nodeType());
    }

    default ExternalNotDefinedTypeKey getExternalDeclaredKey() {
        throw new IllegalStateException("Type key " + getClass() + ":" + this.toString() + " cannot be declared.");
    }
}

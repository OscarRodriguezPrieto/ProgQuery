package es.uniovi.reflection.progquery.utils.keys.cache;

import es.uniovi.reflection.progquery.utils.keys.external.ExternalNotDefinedTypeKey;
import es.uniovi.reflection.progquery.utils.keys.external.ExternalTypeDefKey;

public interface TypeKey {

    ExternalNotDefinedTypeKey getExternalKey();

    default ExternalNotDefinedTypeKey getExternalDeclaredKey() {
        throw new IllegalStateException("Type key " + getClass() + ":" + this.toString() + " cannot be declared.");
    }
}

package es.uniovi.reflection.progquery.utils.types;

import java.util.Objects;

public class ExternalNotDefinedTypeKey {

    String fullyQualifiedTypeName;

    public ExternalNotDefinedTypeKey( String fullyQualifiedTypeName) {
        this.fullyQualifiedTypeName = fullyQualifiedTypeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ExternalNotDefinedTypeKey that = (ExternalNotDefinedTypeKey) o;
        return Objects.equals(fullyQualifiedTypeName, that.fullyQualifiedTypeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullyQualifiedTypeName);
    }

    @Override
    public String toString() {
        return "ExternalNotDefinedTypeKey{" + "fullyQualifiedTypeName='" + fullyQualifiedTypeName + '\'' + '}';
    }
}

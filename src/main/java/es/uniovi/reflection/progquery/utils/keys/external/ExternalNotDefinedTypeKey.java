package es.uniovi.reflection.progquery.utils.keys.external;

import java.util.Objects;

public class ExternalNotDefinedTypeKey {

    String fullyQualifiedTypeName;
    private String nodeType;

    public ExternalNotDefinedTypeKey(String fullyQualifiedTypeName, String nodeType) {
        this.fullyQualifiedTypeName = fullyQualifiedTypeName;
        this.nodeType = nodeType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ExternalNotDefinedTypeKey that = (ExternalNotDefinedTypeKey) o;
        return Objects.equals(fullyQualifiedTypeName, that.fullyQualifiedTypeName) &&
                Objects.equals(nodeType, that.nodeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullyQualifiedTypeName, nodeType);
    }

    @Override
    public String toString() {
        return fullyQualifiedTypeName;
    }
}

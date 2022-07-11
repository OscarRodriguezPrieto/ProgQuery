package es.uniovi.reflection.progquery.utils.types;

import java.util.Objects;

public class ExternalTypeDefKey {

    private String fileName;
    private String fullyQualifiedTypeName;

    public ExternalTypeDefKey(String fileName, String fullyQualifiedTypeName) {
        this.fileName = fileName;
        this.fullyQualifiedTypeName = fullyQualifiedTypeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ExternalTypeDefKey that = (ExternalTypeDefKey) o;
        return Objects.equals(fileName, that.fileName) &&
                Objects.equals(fullyQualifiedTypeName, that.fullyQualifiedTypeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, fullyQualifiedTypeName);
    }

    @Override
    public String toString() {
        return "ExternalTypeDefKey{" + "fileName='" + fileName + '\'' + ", fullyQualifiedTypeName='" +
                fullyQualifiedTypeName + '\'' + '}';
    }
}

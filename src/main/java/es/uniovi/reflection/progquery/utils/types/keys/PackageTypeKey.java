package es.uniovi.reflection.progquery.utils.types.keys;

import com.sun.tools.javac.code.Symbol;
import es.uniovi.reflection.progquery.utils.types.TypeKey;

import javax.lang.model.type.NoType;
import java.util.Objects;

public class PackageTypeKey implements TypeKey {
    private String packageName;

    public PackageTypeKey(NoType packType){
        packageName = packType.toString();

    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PackageTypeKey that = (PackageTypeKey) o;
        return Objects.equals(packageName, that.packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName);
    }
}

package es.uniovi.reflection.progquery.utils;

import com.sun.tools.javac.code.Symbol;

public class MethodNameInfo {
    private String simpleName, completeName, fullyQualifiedName;

    public String getSimpleName() {
        return simpleName;
    }

    public String getCompleteName() {
        return completeName;
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public MethodNameInfo(Symbol.MethodSymbol methodSymbol) {

        String methodType = methodSymbol.type.toString();
        if (methodSymbol.isConstructor()) {
            simpleName = "<init>";
            methodType = methodType.substring(0, methodType.length() - 4);
        } else
            simpleName = methodSymbol.name.toString();


        completeName = methodSymbol.owner + ":" + simpleName;
        fullyQualifiedName = completeName + methodType;
    }

    public MethodNameInfo(String simpleName, String completeName, String fullyQualifiedName) {
        this.simpleName = simpleName;
        this.completeName = completeName;
        this.fullyQualifiedName = fullyQualifiedName;
    }
}

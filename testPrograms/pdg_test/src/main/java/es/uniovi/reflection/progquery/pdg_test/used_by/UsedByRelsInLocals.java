package es.uniovi.reflection.progquery.pdg_test.used_by;

public class UsedByRelsInLocals {

    private static Object paramsUsage(Object[] arrayParam, int primitiveParam, UsefulFields... variableArg) {
        System.out.println(arrayParam);
        int foo= primitiveParam > 2 ? variableArg.length : ((Object[])arrayParam).length ;
        return variableArg[arrayParam[primitiveParam].hashCode()];
    }
    private void localUsage() {
        final Object[] localArray=null, secondLocalArray=new Object[]{};
        int primitiveParam;
        UsefulFields variableArg;
        if(localArray.equals(secondLocalArray))
            return ;
        primitiveParam=0;
        primitiveParam-=20;



    }

}
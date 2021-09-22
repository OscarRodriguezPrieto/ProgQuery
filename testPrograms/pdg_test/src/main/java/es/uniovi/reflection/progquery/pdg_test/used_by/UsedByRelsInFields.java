
package es.uniovi.reflection.progquery.pdg_test.used_by;

import java.util.Collections;

public class UsedByRelsInFields extends AttributedSuperclass{
    /*
        Examples of different kinds of variables:
            static and non-static fields defined inside and outside of the class (in the sources and binaries), parameters, and locals (method and block variables)
            primitive and reference types for the variables
        USED BY
        different expressions of the Java language
            identifier, field access ( non-static access to static fields, this/super access, this/super field access )
            IDEA DEL THIS_REF --- IMPLICITLY_USED_BY -----> attr/method access with identifier
            UNARY INCREMENT TEST USE AND MODIF
    */

    static int internalStaticField;
    Object internalInstanceField;

    private static void externalFieldsUsage(){
        internalStaticField=0;
        System.out.println(new UsefulFields().externalInstanceField);
        for(;UsefulFields.externalStaticField<10;) {
            UsefulFields instance=new UsefulFields();
            instance.externalInstanceField.toString();
            UsefulFields.externalStaticField+=100;
        }
        //Incluir otra de binario que no sea System.out
        String res="";
        for(Object o: Collections.EMPTY_LIST)
            res=o.toString();
        while(new UsefulFields().externalStaticField==1) {
            int l= new Object[4].length;
        }
    }
    private void internalFieldsUsage(){
        internalInstanceField=null;
        if(internalStaticField+1==5)
            UsedByRelsInFields.internalStaticField++;
        else
            new UsedByRelsInFields().internalStaticField--;
        while(this.internalInstanceField.hashCode()>0) {
            try {
                internalInstanceField.wait();
            }catch(Exception ex){
                UsedByRelsInFields.this.internalInstanceField.toString();
                UsedByRelsInFields.this.internalStaticField++;
            }
        }
    }
    private void superFieldUsage() throws InterruptedException {
        inheritedField=System.out.hashCode();
        superStaticField.wait();
        System.out.println(super.superStaticField.toString());
        UsedByRelsInFields.superStaticField.wait();
        UsedByRelsInFields.this.superStaticField.wait();
        UsedByRelsInFields.super.superStaticField.wait();
        new UsedByRelsInFields().superStaticField.wait();
        System.out.println( AttributedSuperclass.superStaticField.hashCode()+8);

        if(inheritedField==0)
            return ;
        switch(super.inheritedField) {
            case 2:
                this.inheritedField--;
            default:
                UsedByRelsInFields usedByVar = new UsedByRelsInFields();
                usedByVar.inheritedField--;
                ((AttributedSuperclass)usedByVar).inheritedField += 10;
        }
    }

}

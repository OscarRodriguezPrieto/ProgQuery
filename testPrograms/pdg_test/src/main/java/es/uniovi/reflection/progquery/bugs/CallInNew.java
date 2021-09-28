package es.uniovi.reflection.progquery.bugs;

public class CallInNew {
    public CallInNew(Object o){

    }
    public Object b(){
        return System.out;
    }
    public void a(){
        new ClassFile(b()) ;
    }
}

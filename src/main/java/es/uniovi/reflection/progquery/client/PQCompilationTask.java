package es.uniovi.reflection.progquery.client;

import com.sun.source.util.JavacTask;

import java.rmi.Remote;

public interface PQCompilationTask extends Remote {

    void compile(JavacTask task, String[] args);
}

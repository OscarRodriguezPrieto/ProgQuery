package test.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import com.sun.tools.javac.api.JavacTaskImpl;

public class CompilerUtils {

	public static JavacTaskImpl getTask(List<File> fileList) {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
		Iterable<? extends JavaFileObject> sources = fileManager.getJavaFileObjectsFromFiles(fileList);
		return (JavacTaskImpl) compiler.getTask(null, null, diagnostics, null, null, sources);
	}

	public static JavacTaskImpl getTask(String src) {
		return getTask(new File(src));
	}

	public static JavacTaskImpl getTask(String... src) {
		List<File> list = new ArrayList<File>();
		for (String s : src)
			list.add(new File(s));
		return getTask(list);
	}

	public static JavacTaskImpl getTask(File f) {
		List<File> list = new ArrayList<File>();
		list.add(f);
		return getTask(list);
	}
}


import com.sun.source.util.JavacTask;

import database.DatabaseFachade;
import tasklisteners.GetStructuresAfterAnalyze;

public class WiggleIndexerPlugin implements com.sun.source.util.Plugin {

	private static final String PLUGIN_NAME = "WiggleIndexerPlugin";

	@Override
	public void init(JavacTask task, String[] args) {
		task.addTaskListener(new GetStructuresAfterAnalyze(task, DatabaseFachade.getDB()));

	}

	@Override
	public String getName() {
		return PLUGIN_NAME;
	}

}

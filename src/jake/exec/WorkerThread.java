package jake;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

public class WorkerThread extends ForkJoinWorkerThread {

    private List<Plugin> plugins;

    public WorkerThread(ForkJoinPool pool) {
        super(pool);
        plugins = new ArrayList<>();
        plugins.add(new Markdown());
    }

    public List<Plugin> getPlugins() {
        return this.plugins;
    }
}

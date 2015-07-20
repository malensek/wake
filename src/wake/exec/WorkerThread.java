package wake.exec;

import wake.core.Plugin;
import wake.plugins.Copy;
import wake.plugins.Markdown;
import wake.plugins.gallery.Gallery;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

public class WorkerThread extends ForkJoinWorkerThread {

    private List<Plugin> plugins;

    public WorkerThread(ForkJoinPool pool) {
        super(pool);
        plugins = new ArrayList<>();
        plugins.add(new Gallery());
        plugins.add(new Markdown());
        plugins.add(new Copy());
    }

    public List<Plugin> getPlugins() {
        return this.plugins;
    }
}

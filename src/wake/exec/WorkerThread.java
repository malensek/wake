package wake.exec;

import wake.core.Plugin;
import wake.plugins.Copy;
import wake.plugins.Markdown;
import wake.plugins.gallery.Gallery;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

public class WorkerThread extends ForkJoinWorkerThread {

    private Map<String, Plugin> plugins = new LinkedHashMap<>();

    public WorkerThread(ForkJoinPool pool) {
        super(pool);
        registerPlugin(new Gallery());
        registerPlugin(new Markdown());
        registerPlugin(new Copy());
    }

    private void registerPlugin(Plugin plugin) {
        plugins.put(plugin.name(), plugin);
    }

    public Collection<Plugin> getPlugins() {
        return this.plugins.values();
    }

    public Plugin getPlugin(String name) {
        return this.plugins.get(name);
    }
}

package wake.exec;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.List;

import wake.core.Plugin;
import wake.core.WakeFile;

public class Task {

    private WakeFile taskFile;
    private String pluginName;

    List<WakeFile> outputs = null;
    List<WakeFile> dependencies = null;

    public Task(File file) {
        this.taskFile = new WakeFile(file.getAbsolutePath());
        this.pluginName = determinePlugin();
    }

    public Task(Path path) {
        this.taskFile = new WakeFile(path);
        this.pluginName = determinePlugin();
    }

    private String determinePlugin() {
        Collection<Plugin> plugins
            = ((WorkerThread) Thread.currentThread()).getPlugins();

        for (Plugin plugin : plugins) {
            if (plugin.wants(taskFile)) {
                return plugin.name();
            }
        }

        return null;
    }

    private Plugin pluginInstance() {
        return ((WorkerThread) Thread.currentThread()).getPlugin(pluginName);
    }

    public boolean needsExecution() {
        long oldestOutChange = oldestChange(outputs());
        long newestDepChange = newestChange(dependencies());
        long sourceChange = changeTime(taskFile);

        return (sourceChange > oldestOutChange
                || newestDepChange > oldestOutChange);
    }

    public List<WakeFile> outputs() {
        if (this.outputs == null) {
            Plugin plugin = pluginInstance();
            outputs = plugin.produces(taskFile);
        }

        return this.outputs;
    }

    public List<WakeFile> dependencies() {
        if (this.dependencies == null) {
            Plugin plugin = pluginInstance();
            dependencies = plugin.requires(taskFile);
        }

        return this.dependencies;
    }

    public ExecutionResult execute() {
        Plugin plugin = pluginInstance();

        /* Create directories for the output files */
        List<WakeFile> expectedOutputs = outputs();
        for (WakeFile file : expectedOutputs) {
            file.mkParentDir();
        }

        List<WakeFile> out = null;
        try {
            out = plugin.process(taskFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ExecutionResult(pluginName, out);
    }

    private long newestChange(List<WakeFile> files) {
        long newest = Long.MIN_VALUE;

        for (WakeFile file : files) {
            long change = changeTime(file);
            if (change < 0) {
                change = Long.MAX_VALUE;
            }

            if (change > newest) {
                newest = change;
            }
        }

        return newest;
    }

    private long oldestChange(List<WakeFile> files) {
        long oldest = Long.MAX_VALUE;

        for (WakeFile file : files) {
            long change = changeTime(file);
            if (change < oldest) {
                oldest = change;
            }
        }

        return oldest;
    }

    private long changeTime(WakeFile file) {
        FileTime ft;

        try {
            ft = (FileTime) Files.getAttribute(file.toPath(), "unix:ctime");
        } catch (IOException e) {
            return -1;
        }

        return ft.toMillis();
    }
}

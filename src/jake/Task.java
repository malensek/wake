package jake;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;

public class Task {

    private WakeFile taskFile;
    private Plugin plugin;

    public Task(File file) {
        this.taskFile = new WakeFile(file.getAbsolutePath());
    }

    public Task(Path path) {
        this.taskFile = new WakeFile(path);
    }

    public boolean needsExecution() {
        plugin = determinePlugin();
        if (plugin == null) {
            //System.out.println("Could not determine plugin!");
            return false;
        }

        List<WakeFile> outputs = plugin.produces(taskFile);
        List<WakeFile> dependencies = plugin.requires(taskFile);

        for (WakeFile dependency : dependencies) {
            if (dependency.exists() == false) {
                /* TODO Warn the user that a dep wasn't fulfilled */
                return false;
            }
        }

        long oldestOutChange = oldestChange(outputs);
        long newestDepChange = newestChange(dependencies);
        long sourceChange = changeTime(taskFile);

        return (sourceChange > oldestOutChange
                || newestDepChange > oldestOutChange);
    }

    public void execute() {
        if (plugin == null) {
            System.out.println("No plugin found for file: " + taskFile);
        }

        try {
            plugin.process(taskFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private Plugin determinePlugin() {
        List<Plugin> plugins
            = ((WorkerThread) Thread.currentThread()).getPlugins();

        for (Plugin plugin : plugins) {
            if (plugin.wants(taskFile)) {
                return plugin;
            }
        }
        return null;
    }
}

/* wake - http://sigpipe.io/wake                       *
 * Copyright (c) 2016 Matthew Malensek                 *
 * Distributed under the MIT License (see LICENSE.txt) */

package io.sigpipe.wake.exec;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;

import io.sigpipe.wake.core.Plugin;
import io.sigpipe.wake.core.WakeFile;
import io.sigpipe.wake.plugins.Plugins;

public class Task {

    private WakeFile taskFile;
    private Plugin plugin;

    List<WakeFile> outputs = null;
    List<WakeFile> dependencies = null;

    public Task(File file) {
        this.taskFile = new WakeFile(file.getAbsolutePath());
        determinePlugin();
    }

    public Task(Path path) {
        this.taskFile = new WakeFile(path);
        determinePlugin();
    }

    private void determinePlugin() {
        for (Plugin plugin : Plugins.pluginCache) {
            if (plugin.wants(taskFile)) {
                this.plugin = plugin;

                /* Take the first plugin that wants the file: */
                break;
            }
        }
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
            outputs = this.plugin.produces(taskFile);
        }

        return this.outputs;
    }

    public List<WakeFile> dependencies() {
        if (this.dependencies == null) {
            dependencies = this.plugin.requires(taskFile);
        }

        return this.dependencies;
    }

    public ExecutionResult execute() {
        /* Create directories for the output files */
        List<WakeFile> expectedOutputs = outputs();
        for (WakeFile file : expectedOutputs) {
            file.mkParentDir();
        }

        List<WakeFile> out = null;
        try {
            out = this.plugin.process(taskFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ExecutionResult(plugin.name(), out);
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
        LinkOption options[] = new LinkOption[0];

        try {
            Path path = file.toPath();

            if (Files.isSymbolicLink(path)) {
                options = new LinkOption[] { LinkOption.NOFOLLOW_LINKS };
            }

            ft = (FileTime) Files.getAttribute(path, "unix:ctime", options);
        } catch (IOException e) {
            return -1;
        }

        return ft.toMillis();
    }

    @Override
    public String toString() {
        return taskFile.getName() + " [" + this.plugin.name() + "]";
    }
}

/* wake - http://sigpipe.io/wake                       *
 * Copyright (c) 2016 Matthew Malensek                 *
 * Distributed under the MIT License (see LICENSE.txt) */

package io.sigpipe.wake.exec;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

import io.sigpipe.wake.core.Plugin;
import io.sigpipe.wake.plugins.Plugins;

public class WorkerThread extends ForkJoinWorkerThread {

    private Map<String, Plugin> plugins = new LinkedHashMap<>();

    public WorkerThread(ForkJoinPool pool) {
        super(pool);
        for (Class<? extends Plugin> p : Plugins.pluginList) {
            try {
                registerPlugin(p.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
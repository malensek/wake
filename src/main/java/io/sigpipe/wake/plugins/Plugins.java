/* wake - http://sigpipe.io/wake                       *
 * Copyright (c) 2016 Matthew Malensek                 *
 * Distributed under the MIT License (see LICENSE.txt) */

package io.sigpipe.wake.plugins;

import java.util.ArrayList;
import java.util.List;

import io.sigpipe.wake.core.Plugin;
import io.sigpipe.wake.plugins.gallery.Gallery;

public class Plugins {

    public static List<Class<? extends Plugin>> pluginList = new ArrayList<>();

    //TODO wrap this in something to prevent modification
    public static List<Plugin> pluginCache = new ArrayList<>();

    static {
        pluginList.add(Gallery.class);
        pluginList.add(WeeklySchedule.class);
        pluginList.add(Markdown.class);
        pluginList.add(CodeListing.class);
        pluginList.add(Symlink.class);
        pluginList.add(Copy.class);
    }

    static {
        for (Class<? extends Plugin> clazz : pluginList) {
            try {
                pluginCache.add(clazz.getConstructor().newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

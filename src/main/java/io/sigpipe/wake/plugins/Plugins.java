/* wake - http://sigpipe.io/wake                       *
 * Copyright (c) 2016 Matthew Malensek                 *
 * Distributed under the MIT License (see LICENSE.txt) */

/* wake - http://sigpipe.io/wake                       *
 * Copyright (c) 2016 Matthew Malensek                 *
 * Distributed under the MIT License (see LICENSE.txt) */

package wake.plugins;

import java.util.ArrayList;
import java.util.List;

import wake.core.Plugin;
import wake.plugins.gallery.Gallery;

public class Plugins {

    public static List<Class<? extends Plugin>> pluginList = new ArrayList<>();

    static {
        pluginList.add(Gallery.class);
        pluginList.add(Markdown.class);
        pluginList.add(Symlink.class);
        pluginList.add(Copy.class);
    }

}

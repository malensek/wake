/* wake - http://sigpipe.io/wake                       *
 * Copyright (c) 2016 Matthew Malensek                 *
 * Distributed under the MIT License (see LICENSE.txt) */

package io.sigpipe.wake.core;

import org.apache.velocity.VelocityContext;

public class BasicTitle implements TitleMaker {

    private static final String titleKey = "title";

    public BasicTitle() {

    }

    public void makeTitle(VelocityContext context, WakeFile file) {
        String title = "";
        if (context.containsKey(titleKey)) {
            title = (String) context.get(titleKey);
        }

        context.put("title", title);
    }

}

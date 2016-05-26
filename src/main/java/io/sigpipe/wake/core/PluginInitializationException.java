/* wake - http://sigpipe.io/wake                       *
 * Copyright (c) 2016 Matthew Malensek                 *
 * Distributed under the MIT License (see LICENSE.txt) */

package io.sigpipe.wake.core;

public class PluginInitializationException extends Exception {

    private static final long serialVersionUID = -1792971236098199548L;

    public PluginInitializationException() {
        super();
    }

    public PluginInitializationException(String s) {
        super(s);
    }

    public PluginInitializationException(String s, Throwable t) {
        super(s, t);
    }

}

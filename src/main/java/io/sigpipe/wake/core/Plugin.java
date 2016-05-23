/* wake - http://sigpipe.io/wake                       *
 * Copyright (c) 2016 Matthew Malensek                 *
 * Distributed under the MIT License (see LICENSE.txt) */

package io.sigpipe.wake.core;

import java.util.List;

public interface Plugin {

    /**
     * Retrieves the name of this Plugin implementation.
     */
    public String name();

    /**
     * Determines whether or not this Plugin can process the given file.
     */
    public boolean wants(WakeFile file);

    /**
     * Produces a list of dependencies for a particular input file.
     */
    public List<WakeFile> requires(WakeFile file);

    /**
     * Retrieves a list of outputs this Plugin produces from the given file.
     */
    public List<WakeFile> produces(WakeFile file);

    /**
     * Processes the given file, producing the outputs specified by the
     * produces() method.
     * @return List of files produced by the plugin
     */
    public List<WakeFile> process(WakeFile file) throws Exception;
}

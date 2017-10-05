/* wake - http://sigpipe.io/wake                       *
 * Copyright (c) 2016 Matthew Malensek                 *
 * Distributed under the MIT License (see LICENSE.txt) */

package io.sigpipe.wake.core;

import org.apache.velocity.VelocityContext;

public interface TitleMaker {

    void makeTitle(VelocityContext context, WakeFile file);

}

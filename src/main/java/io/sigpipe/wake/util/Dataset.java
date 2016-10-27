/* wake - http://sigpipe.io/wake                       *
 * Copyright (c) 2016 Matthew Malensek                 *
 * Distributed under the MIT License (see LICENSE.txt) */

package io.sigpipe.wake.util;

import java.util.HashMap;
import java.util.Map;

public class Dataset extends HashMap<Object, Object> {

    public Dataset() {
        super();
    }

    public Dataset(Map<?, ?> map) {
        super(map);
    }

    public int parseInt(String parameterName, int defaultValue) {
        int value = defaultValue;
        try {
            value = Integer.parseInt((String) this.get(parameterName));
        } catch (NumberFormatException e) { }
        return value;
    }

    }

}

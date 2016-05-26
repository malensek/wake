/* wake - http://sigpipe.io/wake                       *
 * Copyright (c) 2016 Matthew Malensek                 *
 * Distributed under the MIT License (see LICENSE.txt) */

package io.sigpipe.wake.util;

import java.util.HashMap;
import java.util.Map;

public class SharedDataset {

    private static SharedDataset instance;
    private Map<String, Map<?, ?>> datasets = new HashMap<>();

    private SharedDataset() {

    }

    public static synchronized SharedDataset instance() {
        if (instance == null) {
            instance = new SharedDataset();
        }
        return instance;
    }

    public synchronized Map<?, ?> getDataset(SharedDatasetAccessor accessor) {
        Map<?, ?> dataset = datasets.get(accessor.getDatasetID());
        if (dataset == null) {
            dataset = accessor.createDataset();
        }
        return dataset;
    }

}

/* wake - http://sigpipe.io/wake                       *
 * Copyright (c) 2016 Matthew Malensek                 *
 * Distributed under the MIT License (see LICENSE.txt) */

package io.sigpipe.wake.util;

public interface SharedDatasetAccessor {

    /**
     * Creates a shared dataset. This operation will be performed by the first
     * thread that tries to access the dataset.
     */
    Dataset createDataset();

    /**
     * Retrieves this dataset's unique ID. Generally, this is the absolute path
     * name of the file or directory that is associated with the dataset.
     */
    String getDatasetID();

}

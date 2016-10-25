/* wake - http://sigpipe.io/wake                       *
 * Copyright (c) 2016 Matthew Malensek                 *
 * Distributed under the MIT License (see LICENSE.txt) */

package io.sigpipe.wake.plugins.gallery;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import io.sigpipe.wake.util.Dataset;
import io.sigpipe.wake.util.SharedDatasetAccessor;
import io.sigpipe.wake.util.YAMLFrontMatter;

class DatasetAccessor implements SharedDatasetAccessor {

    private File galleryDir;

    public DatasetAccessor(File galleryDir) {
        this.galleryDir = galleryDir;
    }

    public Dataset createDataset() {
        Path galleryPath = new File(galleryDir.getAbsolutePath()
                + "/" + Gallery.galleryFileName).toPath();

        String content;
        try {
            content = new String(Files.readAllBytes(galleryPath));
        } catch (IOException e) {
            return new Dataset();
        }

        return new Dataset(YAMLFrontMatter.readFrontMatter(content));
    }

    public String getDatasetID() {
        return galleryDir.getAbsolutePath();
    }

}


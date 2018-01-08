package io.sigpipe.wake.plugins;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

import com.esotericsoftware.yamlbeans.YamlReader;

import io.sigpipe.wake.util.Dataset;
import io.sigpipe.wake.util.SharedDatasetAccessor;

class YAMLDatasetAccessor implements SharedDatasetAccessor {

    private File yamlFile;

    public YAMLDatasetAccessor(File yamlFile) {
        this.yamlFile = yamlFile;
    }

    public Dataset createDataset() {
        Map<?, ?> yamlData;

        try {
            String content = new String(Files.readAllBytes(yamlFile.toPath()));
            YamlReader reader = new YamlReader(content);
            yamlData = (Map<?, ?>) reader.read();
        } catch (Exception e) {
            e.printStackTrace();
            return new Dataset();
        }

        return new Dataset(yamlData);
    }

    public String getDatasetID() {
        return yamlFile.getAbsolutePath();
    }
}


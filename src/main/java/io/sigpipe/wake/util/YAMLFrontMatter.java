/* wake - http://sigpipe.io/wake                       *
 * Copyright (c) 2016 Matthew Malensek                 *
 * Distributed under the MIT License (see LICENSE.txt) */

package io.sigpipe.wake.util;

import java.util.HashMap;
import java.util.Map;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

public class YAMLFrontMatter {

    public static Map<?, ?> readFrontMatter(String content) {
        Map<?, ?> yamlData = new HashMap<>();

        if (content.startsWith("---")) {
            /* Read YAML front matter */
            YamlReader yaml = new YamlReader(content);
            try {
                yamlData = (Map<?, ?>) yaml.read();
            } catch (YamlException e) {
                return yamlData;
            }
        }

        return yamlData;
    }

    public static String removeFrontMatter(String content) {
        if (content.startsWith("---")) {
            content = content.substring(content.indexOf("---", 3) + 3);
        }
        return content;
    }
}

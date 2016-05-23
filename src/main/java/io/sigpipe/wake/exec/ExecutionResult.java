/* wake - http://sigpipe.io/wake                       *
 * Copyright (c) 2016 Matthew Malensek                 *
 * Distributed under the MIT License (see LICENSE.txt) */

package wake.exec;

import java.util.List;

import wake.core.WakeFile;

public class ExecutionResult {

    private String pluginName;
    private List<WakeFile> files;

    public ExecutionResult(String pluginName, List<WakeFile> files) {
        this.pluginName = pluginName;
        this.files = files;
    }

    public List<WakeFile> files() {
        return this.files;
    }

    @Override
    public String toString() {
        if (files.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < files.size(); ++i) {
            sb.append(String.format(
                        "%-15s%s",
                        pluginName,
                        files.get(i).getRelativePath()));

            if (i + 1 != files.size()) {
                sb.append(System.lineSeparator());
            }
        }

        return sb.toString();
    }
}

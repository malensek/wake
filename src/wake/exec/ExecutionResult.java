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

    @Override
    public String toString() {
        if (files.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(pluginName + "\t\t" + files.get(0).getRelativePath());

        for (int i = 1; i < files.size(); ++i) {
            sb.append(System.lineSeparator());
            sb.append("\t\t" + files.get(i).getRelativePath());
        }

        return sb.toString();
    }
}

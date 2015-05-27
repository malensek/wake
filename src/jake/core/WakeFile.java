package jake.core;

import java.io.File;
import java.nio.file.Path;

public class WakeFile extends File {

    public WakeFile(String path) {
        super(path);
    }

    public WakeFile(Path path) {
        super(path.toFile().getAbsolutePath());
    }

    public WakeFile(WakeFile parent, String path) {
        super(parent, path);
    }

    public String getExtension() {
        String name = getName();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex == -1) {
            return "";
        }
        return name.substring(dotIndex + 1);
    }

    public String getNameWithoutExtension() {
        String name = getName();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex == -1) {
            return name;
        }
        return name.substring(0, dotIndex);
    }

    public WakeFile getOutputFile() {
        String absPath = getAbsolutePath();
        String outString = absPath.replace(
                Configuration.getSourceDir().getAbsolutePath(),
                Configuration.getOutputDir().getAbsolutePath());
        return new WakeFile(outString);
    }
}

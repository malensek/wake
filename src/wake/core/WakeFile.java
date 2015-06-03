package wake.core;

import java.io.File;
import java.nio.file.Path;

public class WakeFile extends File {

    private static final long serialVersionUID = -2874340808369948981L;

    public WakeFile(String path) {
        super(path);
    }

    public WakeFile(Path path) {
        super(path.toFile().getAbsolutePath());
    }

    public WakeFile(File parent, String path) {
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

    public String getRelativePath() {
        String absPath = this.getAbsolutePath();
        String pwd = new File("").getAbsolutePath();
        String rel = absPath.replace(pwd, "");
        if (rel.startsWith("/") || rel.startsWith("\\")) {
            rel = rel.substring(1);
        }
        return rel;
    }

    public void mkParentDir() {
        this.getParentFile().mkdirs();
    }

    public WakeFile getOutputFile() {
        String absPath = getAbsolutePath();
        String outString = absPath.replace(
                Configuration.getSourceDir().getAbsolutePath(),
                Configuration.getOutputDir().getAbsolutePath());
        return new WakeFile(outString);
    }
}

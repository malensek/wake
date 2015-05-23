package jake;

import java.io.File;

public class Configuration {

    public static File getSourceDir() {
        return new File("source");
    }

    public static File getOutputDir() {
        return new File("output");
    }

    public static WakeFile getTemplateDir() {
        return new WakeFile("templates");
    }
}

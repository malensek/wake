package jake.core;

import java.io.File;

public class Configuration {

    public static File getSourceDir() {
        return new File("source");
    }

    public static File getOutputDir() {
        return new File("output");
    }

    public static File getTemplateDir() {
        return new File("templates");
    }
}

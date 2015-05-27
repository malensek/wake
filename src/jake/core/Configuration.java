package jake.core;

public class Configuration {

    public static WakeFile getSourceDir() {
        return new WakeFile("source");
    }

    public static WakeFile getOutputDir() {
        return new WakeFile("output");
    }

    public static WakeFile getTemplateDir() {
        return new WakeFile("templates");
    }
}

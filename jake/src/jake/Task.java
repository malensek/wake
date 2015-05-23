package jake;

import java.io.File;
import java.nio.file.Path;

public class Task {

    public WakeFile taskFile;

    public Task(File file) {
        this.taskFile = new WakeFile(file.getAbsolutePath());
    }

    public Task(Path path) {
        this.taskFile = new WakeFile(path);
    }

    public boolean needsExecution() {
        return true;
    }
}

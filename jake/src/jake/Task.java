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
    private long newestChange(List<WakeFile> files) {
        long newest = Long.MIN_VALUE;

        for (WakeFile file : files) {
            long change = changeTime(file);
            if (change < 0) {
                change = Long.MAX_VALUE;
            }

            if (change > newest) {
                newest = change;
            }
        }

        return newest;
    }

    private long oldestChange(List<WakeFile> files) {
        long oldest = Long.MAX_VALUE;

        for (WakeFile file : files) {
            long change = changeTime(file);
            if (change < oldest) {
                oldest = change;
            }
        }

        return oldest;
    }

    private long changeTime(WakeFile file) {
        FileTime ft;

        try {
            ft = (FileTime) Files.getAttribute(file.toPath(), "unix:ctime");
        } catch (IOException e) {
            return -1;
        }

        return ft.toMillis();
    }
    }
}

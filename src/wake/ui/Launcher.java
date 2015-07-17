package wake.ui;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import wake.core.Configuration;
import wake.exec.Task;
import wake.exec.WorkerThreadFactory;

public class Launcher {

    static {
        /* Disable the Mac OS X AWT dock icon: */
        System.setProperty("apple.awt.UIElement", "true");
    }

    public static void main(String[] args) throws Exception {

        ForkJoinPool fjp = new ForkJoinPool(
                Runtime.getRuntime().availableProcessors(),
                new WorkerThreadFactory(), null, false);

        Configuration config = Configuration.instance();
        File sourceDir = config.getSourceDir();

        if (sourceDir.exists() == false) {
            System.out.println("Could not locate source directory!");
            System.exit(1);
        }

        String sourcePath = sourceDir.getAbsolutePath();
        Set<Task> taskList = Files.walk(Paths.get(sourcePath))
            .parallel()
            .filter(Files::isRegularFile)
            .map(path -> new Task(path))
            .collect(Collectors.toSet());

        fjp.submit(() -> {
            try {
                taskList
                    .parallelStream()
                    .filter(task -> task.needsExecution())
                    .forEach(task -> task.execute());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).get();
    }
}

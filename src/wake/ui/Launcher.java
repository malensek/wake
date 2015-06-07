package wake.ui;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import wake.core.Configuration;
import wake.exec.Task;
import wake.exec.WorkerThreadFactory;

public class Launcher {
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

        String sourcePath = config.getSourceDir().getAbsolutePath();
        List<Path> paths =
            Files.walk(Paths.get(sourcePath))
            .collect(Collectors.toList());

        fjp.submit(() -> {
            try {
                paths.stream()
                .parallel()
                .filter(Files::isRegularFile)
                .map(path -> new Task(path))
                .filter(task -> task.needsExecution())
                .forEach(task -> task.execute());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).get();
    }
}

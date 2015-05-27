package jake.ui;

import jake.Configuration;
import jake.Task;
import jake.WorkerThreadFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ForkJoinPool;

public class Launcher {
    public static void main(String[] args) throws Exception {

        ForkJoinPool fjp = new ForkJoinPool(
                Runtime.getRuntime().availableProcessors(),
                new WorkerThreadFactory(), null, false);

        String sourceDir = Configuration.getSourceDir().getAbsolutePath();
        fjp.submit(() -> {
            try {
                Files.walk(Paths.get(sourceDir))
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

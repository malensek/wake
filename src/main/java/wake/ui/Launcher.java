package wake.ui;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import wake.core.Configuration;
import wake.core.WakeFile;
import wake.exec.ExecutionResult;
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
        File outputDir = config.getOutputDir();

        if (sourceDir.exists() == false) {
            System.out.println("Could not locate source directory!");
            System.exit(1);
        }

        String sourcePath = sourceDir.getAbsolutePath();
        Set<Task> taskList = fjp.submit(() -> {
            return Files.walk(Paths.get(sourcePath))
                .parallel()
                .filter(Files::isRegularFile)
                .map(path -> new Task(path))
                .collect(Collectors.toSet());
        }).get();

        fjp.submit(() -> {
            try {
                taskList
                    .parallelStream()
                    .filter(task -> task.needsExecution())
                    .map(task -> task.execute())
                    .forEach(System.out::println);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).get();

        Set<WakeFile> generatedOutputs = taskList
            .parallelStream()
            .flatMap(task -> task.outputs().stream())
            .collect(Collectors.toSet());

        Set<WakeFile> existingOutputs =
            Files.walk(outputDir.toPath())
            .map(file -> new WakeFile(file))
            .collect(Collectors.toSet());

        System.out.println("Cleaning up orphaned files...");
        Set<WakeFile> orphans = new HashSet<>(existingOutputs);
        orphans.removeAll(generatedOutputs);
        ExecutionResult er = new ExecutionResult(
                "Remove", new ArrayList<WakeFile>(orphans));
        System.out.println(er);
        for (WakeFile orphan : orphans) {
            orphan.delete();
        }

        System.out.println("Setting permissions...");
        Files.walk(outputDir.toPath()).;
        existingOutputs.retainAll(generatedOutputs);
        Set<PosixFilePermission> filePerms = config.getFilePermissions();
        Set<PosixFilePermission> dirPerms = config.getDirPermissions();
        for (WakeFile file : existingOutputs) {
            if (file.isFile() && filePerms != null) {
                System.out.print('f');
                Files.setPosixFilePermissions(file.toPath(), filePerms);
            } else if (file.isDirectory() && dirPerms != null) {
                System.out.print('d');
                Files.setPosixFilePermissions(file.toPath(), dirPerms);
            }
        }


    }
}

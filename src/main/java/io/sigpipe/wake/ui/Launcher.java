/* wake - http://sigpipe.io/wake                       *
 * Copyright (c) 2016 Matthew Malensek                 *
 * Distributed under the MIT License (see LICENSE.txt) */

package io.sigpipe.wake.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import io.sigpipe.wake.core.Configuration;
import io.sigpipe.wake.core.WakeFile;
import io.sigpipe.wake.exec.ExecutionResult;
import io.sigpipe.wake.exec.Task;
import io.sigpipe.wake.exec.WorkerThreadFactory;

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
                .map(Task::new)
                .collect(Collectors.toSet());
        }).get();

        fjp.submit(() -> {
            try {
                taskList
                    .parallelStream()
                    .filter(Task::needsExecution)
                    .map(Task::execute)
                    .forEach(System.out::println);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).get();

        System.out.println("Cleaning up orphaned files...");
        ExecutionResult er = cleanOrphans(taskList, outputDir.toPath());
        if (er.files().size() > 0) {
            System.out.println(er);
        }

        System.out.println("Setting permissions...");
        setPermissions(outputDir.toPath());
    }

    private static ExecutionResult cleanOrphans(
            Set<Task> taskList, Path outputDir)
    throws IOException {

        Set<WakeFile> generatedOutputs = taskList
            .parallelStream()
            .flatMap(task -> task.outputs().stream())
            .collect(Collectors.toSet());

        Set<WakeFile> existingOutputs =
            Files.walk(outputDir, FileVisitOption.FOLLOW_LINKS)
            .filter(path -> Files.isDirectory(path) != true)
            .map(WakeFile::new)
            .collect(Collectors.toSet());

        Set<WakeFile> orphans = new HashSet<>(existingOutputs);
        orphans.removeAll(generatedOutputs);

        for (WakeFile orphan : orphans) {
            orphan.delete();
        }

        int removed = 0;
        do {
            List<WakeFile> emptyDirs =
                Files.walk(outputDir, FileVisitOption.FOLLOW_LINKS)
                .filter(Files::isDirectory)
                .filter(path -> path.toFile().list().length == 0)
                .map(WakeFile::new)
                .collect(Collectors.toList());

            for (WakeFile emptyDir : emptyDirs) {
                emptyDir.delete();
            }

            removed = emptyDirs.size();
            orphans.addAll(emptyDirs);
        } while (removed > 0);

        return new ExecutionResult("Remove", new ArrayList<>(orphans));
    }

    private static void setPermissions(Path outputDir)
    throws IOException {
        Configuration config = Configuration.instance();
        List<Path> finalOutputs =
            Files.walk(outputDir, FileVisitOption.FOLLOW_LINKS)
            .collect(Collectors.toList());

        Set<PosixFilePermission> filePerms = config.getFilePermissions();
        Set<PosixFilePermission> dirPerms = config.getDirPermissions();
        for (Path p : finalOutputs) {
            if (p.toFile().isDirectory() && dirPerms != null) {
                Files.setPosixFilePermissions(p, dirPerms);
            } else if (p.toFile().isFile() && filePerms != null) {
                Files.setPosixFilePermissions(p, filePerms);
            }
        }
    }
}

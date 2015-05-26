package jake.ui;

import jake.Configuration;
import jake.Task;
import jake.WakeFile;
import jake.WorkerThreadFactory;

import java.io.File;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.pegdown.PegDownProcessor;

public class Launcher {
    public static void main(String[] args) throws Exception {
        /*
        System.out.println(System.getProperty("user.dir"));
        System.out.println(new File(".").getPath());
        System.out.println(Configuration.getOutputDir().getAbsolutePath());
        WakeFile w = new WakeFile("source/projects/index.md");
        System.out.println(w.getAbsolutePath());
        System.out.println(w.getOutputFile().getAbsolutePath());
        System.exit(0);
        */

        ForkJoinPool fjp = new ForkJoinPool(
                Runtime.getRuntime().availableProcessors(),
                new WorkerThreadFactory(), null, false);

        String source = Configuration.getSourceDir().getAbsolutePath();
        fjp.submit(() ->
        Files.walk(Paths.get(source))
            .parallel()
            .filter(Files::isRegularFile)
            .map(path -> new Task(path))
            .filter(task -> task.needsExecution())
            .collect(Collectors.toList())
        ).get();

        System.out.println("goodbye");
        System.exit(0);

        VelocityEngine ve = new VelocityEngine();
        ve.init();

        Template t = ve.getTemplate("testtemplate.vm");
        VelocityContext ctxt = new VelocityContext();
        ctxt.put("name", "World");
        ctxt.put("title", "What what");
        ctxt.put("test", "What what!!!");

        StringWriter writer = new StringWriter();
        t.merge(ctxt, writer);
        System.out.println(writer.toString());

        String content = new String(Files.readAllBytes(Paths.get("index.md")));
        PegDownProcessor pdp = new PegDownProcessor();
        System.out.println(pdp.markdownToHtml(content));
    }
}

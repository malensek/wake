package io.sigpipe.wake.core;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * General utility functions for dealing with page templates.
 */
public class TemplateUtils {

    /**
     * Given a VelocityEngine template file, this method retrieves all calls to
     * the parse() and input() functions and then builds a list of dependent
     * files. Note that the template file passed in is not included in the list.
     *
     * @param config The system configuration to use to locate templates
     * @param template The VelocityEngine template file to parse
     *
     * @return A List of WakeFile instances representing all the dependencies of
     * the given file.
     */
    public static List<WakeFile> getTemplateDependencies(
            Configuration config, WakeFile template) {

        List<WakeFile> dependencies = new ArrayList<>();
        List<WakeFile> children = new ArrayList<>();
        String content = "";

        try {
            content = new String(
                    Files.readAllBytes(Paths.get(template.getPath())));
            content = content.replaceAll("\\s+", "");
        } catch (Exception e) {

        }

        int idx = 0;
        String search = "#parse(";
        while (idx >= 0) {
            int start = content.indexOf(search, idx);
            int end = content.indexOf(')', start);
            if (start < 0 || end < 0) {
                break;
            }
            String file = content.substring(start + search.length(), end)
                .replaceAll("\"", "")
                .replaceAll("'", "");
            children.add(new WakeFile(config.getTemplateDir(), file));
            idx = end;
        }

        dependencies.addAll(children);
        for (WakeFile child : children) {
            List<WakeFile> childDeps = getTemplateDependencies(config, child);
            dependencies.addAll(childDeps);
        }

        /* Includes are not processed by the template engine, so they cannot
         * have children. */
        idx = 0;
        search = "#include(";
        while (idx >= 0) {
            int start = content.indexOf(search, idx);
            int end = content.indexOf(')', start);
            if (start < 0 || end < 0) {
                break;
            }
            String files = content.substring(start + search.length(), end);
            for (String file : files.split(",")) {
                file = file
                    .replaceAll("\"", "")
                    .replaceAll("'", "");
                dependencies.add(new WakeFile(config.getTemplateDir(), file));
            }
            idx = end;
        }

        return dependencies;
    }

}

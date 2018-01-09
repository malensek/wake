package io.sigpipe.wake.plugins;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.options.MutableDataSet;

import io.sigpipe.wake.core.Configuration;
import io.sigpipe.wake.core.Plugin;
import io.sigpipe.wake.core.TemplateUtils;
import io.sigpipe.wake.core.WakeFile;
import io.sigpipe.wake.util.Dataset;
import io.sigpipe.wake.util.SharedDataset;
import io.sigpipe.wake.util.YAMLFrontMatter;

public class WeeklySchedule implements Plugin {

    protected static final String scheduleFileName = "WeeklySchedule.wake.md";
    protected static final String scheduleTemplate = "weekly-schedule.vm";

    private Pattern weekPattern = Pattern.compile("week([0-9]*)\\.md");
    private Parser markdownParser;
    private HtmlRenderer htmlRenderer;
    private WakeFile template;

    public WeeklySchedule() {
        Configuration config = Configuration.instance();

        MutableDataSet options = config.getMarkdownOptions();
        markdownParser = Parser.builder(options).build();
        htmlRenderer = HtmlRenderer.builder(options).build();

        template = new WakeFile(config.getTemplateDir(), scheduleTemplate);
    }

    @Override
    public String name() {
        return "Schedule";
    }

    @Override
    public boolean wants(WakeFile file) {
        if (isScheduleSettings(file) == true) {
            return true;
        }

        WakeFile settings = this.getSettings(file);
        if (settings.exists() == false) {
            /* If a settings file doesn't exist, the plugin is not interested in
             * this directory. */
            return false;
        }

        return weekPattern.matcher(file.getName()).matches();
    }

    @Override
    public List<WakeFile> requires(WakeFile file) {
        if (isScheduleSettings(file) == false) {
            /* Week descriptions are used to build a single schedule page;
             * therefore, don't have any requirements */
            return Arrays.asList();
        }

        List<WakeFile> dependencies = new ArrayList<>();
        dependencies.add(this.template);
        dependencies.addAll(
                TemplateUtils.getTemplateDependencies(
                    Configuration.instance(),
                    this.template));
        try {
            dependencies.addAll(locateWeekDescriptions(file));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dependencies;
    }

    @Override
    public List<WakeFile> produces(WakeFile file) {
        if (isScheduleSettings(file) == false) {
            return Arrays.asList();
        }

        YAMLDatasetAccessor yda = new YAMLDatasetAccessor(file);
        Dataset scheduleParams = SharedDataset.instance().getDataset(yda);

        String outputFileName = "index.html";
        if (scheduleParams.containsKey("output")) {
            outputFileName = (String) scheduleParams.get("output");
        }
        return Arrays.asList(new WakeFile(
                    file.getParentFile(), outputFileName)
                .toOutputFile());
    }

    @Override
    public void process(WakeFile file) throws Exception {
        if (isScheduleSettings(file) == false) {
            /* For week descriptions, processing the individual files is a
             * no-op. They will be gathered and combined to form a single
             * schedule page. */
            return;
        }

        YAMLDatasetAccessor yda = new YAMLDatasetAccessor(file);
        Dataset scheduleParams = SharedDataset.instance().getDataset(yda);
        Configuration config = Configuration.instance();

        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty("file.resource.loader.path",
                config.getTemplateDir().getAbsolutePath());
        velocityEngine.init();
        Template scheduleTemplate = velocityEngine.getTemplate(
                this.template.getName());

        String firstWeekStr = (String) scheduleParams.get("first_week");
        LocalDate firstWeek = LocalDate.parse(firstWeekStr);

        int weekSize = scheduleParams.parseInt("week_size", 5);
        int maxWeek = 0;
        Map<Integer, Map<?, ?>> weeks = new TreeMap<>();
        for (WakeFile weekDescription : locateWeekDescriptions(file)) {
            Map<Object, Object> week = new HashMap<>();
            Matcher matcher = this.weekPattern.matcher(weekDescription.getName());
            matcher.find();

            int weekNumber = Integer.parseInt(matcher.group(1));
            if (weekNumber > maxWeek) {
                maxWeek = weekNumber;
            }
            week.put("number", weekNumber);

            String dateRange = createDateRange(firstWeek, weekNumber, weekSize);
            week.put("dateRange", dateRange);

            String fileContent;
            fileContent = new String(
                    Files.readAllBytes(weekDescription.toPath()));
            Map<?, ?> yamlData = YAMLFrontMatter.readFrontMatter(fileContent);
            if ("false".equals(yamlData.get("publish"))) {
                continue;
            }
            for (Object key : yamlData.keySet()) {
                Object value = yamlData.get(key);
                week.put(key, value);
            }
            fileContent = YAMLFrontMatter.removeFrontMatter(fileContent);

            String topics = "";
            String materials = "";

            int sepLocation = fileContent.indexOf("---");
            if (sepLocation == -1) {
                topics = fileContent;
            } else {
                topics = fileContent.substring(0, sepLocation);
                materials = fileContent.substring(sepLocation + 3);
            }

            week.put("topics",
                    htmlRenderer.render(markdownParser.parse(topics)));
            week.put("materials",
                    htmlRenderer.render(markdownParser.parse(materials)));

            weeks.put(weekNumber, week);
        }

        boolean fillMissing = scheduleParams.parseBoolean("fill_missing", true);
        if (fillMissing) {
            for (int i = 1; i < maxWeek; ++i) {
                if (weeks.containsKey(i) == false) {
                    Map<Object, Object> week = new HashMap<>();
                    week.put("number", i);
                    String dateRange
                        = createDateRange(firstWeek, i, weekSize);
                    week.put("dateRange", dateRange);
                    weeks.put(i, week);
                }
            }
        }

        VelocityContext context = new VelocityContext(scheduleParams);
        context.put("weeks", weeks.values());

        String content = new String(Files.readAllBytes(file.toPath()));
        content = YAMLFrontMatter.removeFrontMatter(content);
        context.put("markdown_content",
                htmlRenderer.render(markdownParser.parse(content)));

        FileWriter writer = new FileWriter(this.produces(file).get(0));
        scheduleTemplate.merge(context, writer);
        writer.close();
    }

    private boolean isScheduleSettings(File file) {
        return file.getName().equals(scheduleFileName);
    }

    /**
     * Retrieves the schedule settings file associated with the given file.
     */
    private WakeFile getSettings(File file) {
        return new WakeFile(file.getParentFile(), scheduleFileName);
    }

    private List<WakeFile> locateWeekDescriptions(WakeFile settingsFile)
    throws IOException {
        return Files.list(settingsFile.getParentFile().toPath())
            .map(Path::toString)
            .filter(this.weekPattern.asPredicate())
            .map(p -> new WakeFile(p))
            .collect(Collectors.toList());
    }

    private String createDateRange(
            LocalDate firstWeek, int weekNumber, int weekSize) {

        LocalDate weekStart = firstWeek.plus(weekNumber - 1, ChronoUnit.WEEKS);
        LocalDate weekEnd = weekStart.plus(weekSize - 1, ChronoUnit.DAYS);

        Month startMonth = weekStart.getMonth();
        Month endMonth = weekEnd.getMonth();
 
        String dateRange = "";
        if (startMonth == endMonth) {
            String month = startMonth.getDisplayName(
                    TextStyle.SHORT, Locale.getDefault());
            dateRange =
                month + " " + weekStart.getDayOfMonth()
                + " - "
                + weekEnd.getDayOfMonth();
        } else {
            String month1 = startMonth.getDisplayName(
                    TextStyle.SHORT, Locale.getDefault());
            String month2 = endMonth.getDisplayName(
                    TextStyle.SHORT, Locale.getDefault());
            dateRange =
                month1 + " " + weekStart.getDayOfMonth()
                + " - "
                + month2 + " " + weekEnd.getDayOfMonth();
        }

        return dateRange;
    }
}

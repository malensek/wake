package io.sigpipe.wake.core;

import org.apache.velocity.VelocityContext;

public class DashedTitle implements TitleMaker {

    private static final String titleKey = "title";

    private String baseTitle;
    private String separator = " - ";

    public DashedTitle(String baseTitle) {
        this.baseTitle = baseTitle;
    }

    public DashedTitle(String baseTitle, String separator) {
        this.baseTitle = baseTitle;
        this.separator = separator;
    }

    public void makeTitle(VelocityContext context, WakeFile file) {
        String subTitle = "";
        if (context.containsKey(titleKey)) {
            subTitle += separator;
            subTitle += context.get(titleKey);
        }

        context.put("title", baseTitle + subTitle);
    }

}

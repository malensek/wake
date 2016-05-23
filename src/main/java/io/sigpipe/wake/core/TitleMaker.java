package io.sigpipe.wake.core;

import org.apache.velocity.VelocityContext;

public interface TitleMaker {

    public void makeTitle(VelocityContext context, WakeFile file);

}

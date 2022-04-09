package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.model.listeners.RunListener;
import hudson.model.*;

import java.util.logging.*;

@Extension
public class GlobalRunListener extends RunListener<Run<?, ?>> {
    private static final Logger logger = Logger.getLogger(GlobalRunListener.class.getName());

    @Override
    public void onCompleted(Run<?, ?> build, TaskListener listener) {
        listener.getLogger().println("[ExecutionReporter] println");
        logger.log(Level.WARNING, "log");
    }
}

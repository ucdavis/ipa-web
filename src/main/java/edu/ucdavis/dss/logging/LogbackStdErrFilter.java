package edu.ucdavis.dss.logging;

import java.util.Arrays;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Credit: http://stackoverflow.com/questions/5653062/how-can-i-configure-logback-to-log-different-levels-for-a-logger-to-different-de
 */
@SuppressWarnings("rawtypes")
public class LogbackStdErrFilter extends ch.qos.logback.core.filter.AbstractMatcherFilter {
    @Override
    public FilterReply decide(Object event)
    {
        if (!isStarted())
        {
            return FilterReply.NEUTRAL;
        }

        LoggingEvent loggingEvent = (LoggingEvent) event;

        List<Level> eventsToKeep = Arrays.asList(Level.WARN, Level.ERROR);
        if (eventsToKeep.contains(loggingEvent.getLevel()))
        {
            return FilterReply.NEUTRAL;
        }
        else
        {
            return FilterReply.DENY;
        }
    }
}
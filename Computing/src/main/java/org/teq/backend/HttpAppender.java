package org.teq.backend;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.util.concurrent.ConcurrentLinkedQueue;

@Plugin(name = "HttpAppender", category = "Core", elementType = Appender.ELEMENT_TYPE, printObject = true)
public class HttpAppender extends AbstractAppender {
    private static final ConcurrentLinkedQueue<String> logQueue = new ConcurrentLinkedQueue<>();

    protected HttpAppender(String name, Layout<?> layout) {
        super(name, null, layout, false);
    }

    @Override
    public void append(LogEvent event) {
        String message = new String(getLayout().toByteArray(event));
        logQueue.add(message);
    }

    public static String getLogs() {
        StringBuilder logs = new StringBuilder();
        while (!logQueue.isEmpty()) {
            logs.append(logQueue.poll());
        }
        return logs.toString();
    }

    @PluginFactory
    public static HttpAppender createAppender(
            @org.apache.logging.log4j.core.config.plugins.PluginAttribute("name") String name,
            @org.apache.logging.log4j.core.config.plugins.PluginElement("Layout") Layout<?> layout) {
        return new HttpAppender(name, layout);
    }
}

package net.randomsync.robotframework.mqtt;

import org.robotframework.javalib.library.AnnotationLibrary;

public class MQTTLibrary extends AnnotationLibrary {

    public static final String KEYWORD_PATTERN = "net/randomsync/robotframework/mqtt/keywords/**/*.class";

    public static final String ROBOT_LIBRARY_SCOPE = "GLOBAL";

    public static final String ROBOT_LIBRARY_VERSION = "1.0.0";

    public static final String ROBOT_LIBRARY_DOC_FORMAT = "HTML";

    public MQTTLibrary() {
        super(KEYWORD_PATTERN);
    }

}

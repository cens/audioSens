package edu.ucla.cens.audiosens.config;

public class OhmageWriterConfig 
{
    public static final String OBSERVER_ID = "org.ohmage.probes.audioSensProbe";
    public static final int OBSERVER_VERSION = 6;

    public static final String STREAM_FEATURES = "features";
    public static final int STREAM_FEATURES_VERSION = 6;
    
    public static final String STREAM_SENSORS = "sensors";
    public static final int STREAM_SENSORS_VERSION = 6;
    
    public static final String STREAM_CLASSIFIERS = "classifiers";
    public static final int STREAM_CLASSIFIERS_VERSION = 6	;

    public static final boolean WRITESFEATURES = false;
    public static final boolean WRITESSENSORS = true;
    public static final boolean WRITESCLASSIFIERS = true;
}

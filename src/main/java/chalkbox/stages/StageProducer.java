package chalkbox.stages;

import chalkbox.config.Config;
import chalkbox.config.ConfigException;

public interface StageProducer {
    Stage build(Config config) throws ConfigException;

    String getName();
}

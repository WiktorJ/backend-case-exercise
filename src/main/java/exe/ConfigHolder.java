package exe;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;


public class ConfigHolder {

    private ConfigHolder(){}

    private static class LazyHolder {

        public static final Configuration config = init();

        private static Configuration init() {
            Parameters params = new Parameters();
            FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                    new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                            .configure(params.properties()
                                    .setFileName("app.properties"));
            try {
                return builder.getConfiguration();
            } catch (ConfigurationException cex) {
                throw new RuntimeException(cex);
            }
        }
    }

    public static Configuration getConfig(){
        return LazyHolder.config;
    }
}

package org.shadows;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

/**
 * Fill the comment
 *
 * @author bayura-ea
 */
public class AppProperties extends Properties {

    public AppProperties() throws IOException {
        this.load(Bootstrap.class.getClassLoader().getResourceAsStream("application.properties"));
        for (Object key : keySet()) {
            Optional.ofNullable(System.getenv((String) key))
                    .ifPresent(v -> put(key, v));
        }
    }


}

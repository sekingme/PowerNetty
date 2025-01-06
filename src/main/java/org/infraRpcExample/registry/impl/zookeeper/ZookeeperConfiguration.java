 /* power by sekingme */

package org.infraRpcExample.registry.impl.zookeeper;

import java.util.function.Function;

/**
 * @author sekingme
 */
public enum ZookeeperConfiguration {

    NAME_SPACE("namespace", "datapower_netty", value -> value),
    SERVERS("servers", null, value -> value),

    /**
     * Initial amount of time to wait between retries
     */
    BASE_SLEEP_TIME("base.sleep.time.ms", 60, Integer::valueOf),
    MAX_SLEEP_TIME("max.sleep.ms", 300, Integer::valueOf),
    DIGEST("digest", null, value -> value),

    MAX_RETRIES("max.retries", 5, Integer::valueOf),


    SESSION_TIMEOUT_MS("session.timeout.ms", 30000, Integer::valueOf),
    CONNECTION_TIMEOUT_MS("connection.timeout.ms", 7500, Integer::valueOf),

    BLOCK_UNTIL_CONNECTED_WAIT_MS("block.until.connected.wait", 600, Integer::valueOf),
    ;
    private final String name;
    private final Object defaultValue;
    private final Function<String, Object> converter;

    <T> ZookeeperConfiguration(String name, T defaultValue, Function<String, T> converter) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.converter = (Function<String, Object>) converter;
    }

    public String getName() {
        return name;
    }

    public <T> T getParameterValue(String param) {
        Object value = param != null ? converter.apply(param) : defaultValue;
        return (T) value;
    }

}

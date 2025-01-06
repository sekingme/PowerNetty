 /* power by sekingme */

package org.remoteRpcExample.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * constant
 */
public class Constants {

    private Constants() {
        throw new IllegalStateException(Constants.class.getName());
    }

    public static final String COMMA = ",";

    public static final String SLASH = "/";

    public static final int NETTY_SERVER_HEART_BEAT_TIME = 1000 * 60 * 3 + 1000;

    public static final int NETTY_CLIENT_HEART_BEAT_TIME = 1000 * 6;

    /**
     * charset
     */
    public static final Charset UTF8 = StandardCharsets.UTF_8;

    /**
     * cpus
     */
    public static final int CPUS = Runtime.getRuntime().availableProcessors();

    /**
     * netty epoll enable switch
     */
    public static final String NETTY_EPOLL_ENABLE = System.getProperty("netty.epoll.enable", "true");

    /**
     * OS Name
     */
    public static final String OS_NAME = System.getProperty("os.name");

    /**
     * warm up time
     */
    public static final int WARM_UP_TIME = 10 * 60 * 1000;

}

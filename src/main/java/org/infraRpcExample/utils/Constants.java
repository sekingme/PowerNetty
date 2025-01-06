 /* power by sekingme */

package org.infraRpcExample.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * constant
 */
public class Constants {

    /**
     * timestamp
     */
    public static final String  TIMESTAMP                      = "timestamp";
    public static final String  DIVISION_STRING                = "/";
    public static final int     NETTY_SERVER_HEART_BEAT_TIME   = 1000 * 60 * 3 + 1000;
    public static final int     NETTY_CLIENT_HEART_BEAT_TIME   = 1000 * 6;
    public static final String  COMMA                          = ",";
    public static final String  COLON                          = ":";
    public static final String  SPACE                          = " ";
    public static final String  SINGLE_SLASH                   = "/";
    public static final String  DOUBLE_SLASH                   = "//";
    public static final String  SINGLE_QUOTES                  = "'";
    public static final String  DOUBLE_QUOTES                  = "\"";
    public static final String  SEMICOLON                      = ";";
    public static final String  EQUAL_SIGN                     = "=";
    public static final String  AT_SIGN                        = "@";
    /**
     * registry properties
     */
    public static final String  REGISTRY_DP_NETTY              = "/datapower/netty/servers";
    public static final String  REGISTRY_DP_DEAD_SERVERS       = "/datapower/netty/dead_servers";
    public static final String  DELETE_OP                      = "delete";
    public static final String  ADD_OP                         = "add";
    /**
     * stream event string
     */
    public static final String  STREAM_EVENT_DONE              = "[DONE]";
    public static final String  STREAM_EVENT_ERROR             = "ERROR: ";
    public static final String  DATA_BEGIN                     = "DATA:";
    public static final String  DOUBLE_NEW_LINE                = "\n\n";
    /**
     * status code
     */
    public static final int     SUCCESS_CODE                   = 200;
    public static final int     BAD_REQUEST_CODE               = 400;
    public static final int     UNAUTHORIZED_REQUEST_CODE      = 401;
    public static final int     NOT_FOUND_CODE                 = 404;
    public static final int     INTERNAL_SERVER_ERROR_CODE     = 500;
    public static final int     SERVICE_UNAVAILABLE_CODE       = 503;
    /**
     * worker cpu load
     */
    public static final int     DEFAULT_WORKER_CPU_LOAD        = Runtime.getRuntime().availableProcessors() * 2;
    /**
     * sleep time
     */
    public static final int     SLEEP_TIME_MILLIS              = 1000;
    /**
     * 100 mil second
     */
    public static final int     HUNDRED_TIME_MILLIS            = 100;
    /**
     * cpus
     */
    public static final int     CPUS                           = Runtime.getRuntime().availableProcessors();
    /**
     * netty epoll enable switch
     */
    public static final String  NETTY_EPOLL_ENABLE             = System.getProperty("netty.epoll.enable", "true");
    /**
     * OS Name
     */
    public static final String  OS_NAME                        = System.getProperty("os.name");
    /**
     * warm up time
     */
    public static final int     WARM_UP_TIME                   = 10 * 60 * 1000;
    /**
     * kubernetes
     */
    public static final Boolean KUBERNETES_MODE                = !StringUtils.isEmpty(System.getenv("KUBERNETES_SERVICE_HOST")) && !StringUtils.isEmpty(System.getenv("KUBERNETES_SERVICE_PORT"));
    /**
     * common properties path
     */
    public static final String  COMMON_PROPERTIES_PATH         = "/common.properties";
    /**
     * network interface preferred
     */
    public static final String  DP_NETWORK_INTERFACE_PREFERRED = "datapower.network.interface.preferred";
    /**
     * network IP gets priority, default inner outer
     */
    public static final String  DP_NETWORK_PRIORITY_STRATEGY   = "datapower.network.priority.strategy";
    /**
     * 注册中心 heartbeat 状态
     */
    public static final int     NORMAL_NODE_STATUS             = 0;
    public static final int     ABNORMAL_NODE_STATUS           = 1;
    public static final int     BUSY_NODE_STATUS               = 2;
    /**
     * heartbeat for zk info length
     */
    public static final int     HEARTBEAT_ZK_INFO_LENGTH       = 14;

    /**
     * kerberos
     */
    public static final String KERBEROS            = "kerberos";
    public static final String CACHE_KEY_VALUE_ALL = "'all'";

    private Constants() {
        throw new IllegalStateException(Constants.class.getName());
    }

}

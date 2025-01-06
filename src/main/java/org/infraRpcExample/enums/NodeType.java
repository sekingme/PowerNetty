 /* power by sekingme */
package org.infraRpcExample.enums;

import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;

/**
 * @author sekingme
 */

@Getter
@ToString
public enum NodeType {
    NETTY_TEST("netty_test"),
    OLD_DATAPOWER("old_datapower"),
//    WEB("web"),
//    ANALYSIS("analysis"),
//    VIS("visualization"),
//    RBAC("rbac")
    ;

    private final String serverName;

    NodeType(String serverName) {
        this.serverName = serverName;
    }

    public static NodeType of(String serverName) {
        return Arrays.stream(NodeType.values()).filter(nodeType -> nodeType.serverName.equalsIgnoreCase(serverName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such server type"));
    }
}

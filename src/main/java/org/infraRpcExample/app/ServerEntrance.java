package org.infraRpcExample.app;

import org.infraRpcExample.config.NettyServerConfig;
import org.infraRpcExample.remote.NettyServer;

/**
 * @author sekingme
 */
public class ServerEntrance {
    // 多进程通信真实服务端
    public static void main(String[] args) {
        NettyServer nettyServer = null;
        try {
            nettyServer = new NettyServer(NettyServerConfig.builder().build());
            nettyServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            if (nettyServer != null) {
//                nettyServer.close();
//            }
        }
    }
}

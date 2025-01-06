package org.remoteRpcExample.app;

import org.remoteRpcExample.config.NettyServerConfig;
import org.remoteRpcExample.remote.NettyServer;

/**
 * @author sekingme
 */
public class ServerEntrance {
    // 多进程通信真实服务端
    public static void main(String[] args) {
        NettyServer nettyServer = null;
        try {
            nettyServer = new NettyServer(new NettyServerConfig());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            if (nettyServer != null) {
//                nettyServer.close();
//            }
        }
    }
}

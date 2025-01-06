 /* power by sekingme */

package org.infraRpcExample.utils;

import io.netty.channel.Channel;
import org.infraRpcExample.host.Host;

import java.net.InetSocketAddress;

/**
 * channel utils
 */
public class ChannelUtils {

    private ChannelUtils() {
        throw new IllegalStateException(ChannelUtils.class.getName());
    }

    /**
     * get local address
     *
     * @param channel channel
     * @return local address
     */
    public static String getLocalAddress(Channel channel) {
        return NetUtils.getHost(((InetSocketAddress) channel.localAddress()).getAddress());
    }

    /**
     * get remote address
     *
     * @param channel channel
     * @return remote address
     */
    public static String getRemoteAddress(Channel channel) {
        return NetUtils.getHost(((InetSocketAddress) channel.remoteAddress()).getAddress());
    }

    /**
     * channel to address
     *
     * @param channel channel
     * @return address
     */
    public static Host toAddress(Channel channel) {
        InetSocketAddress socketAddress = ((InetSocketAddress) channel.remoteAddress());
        return new Host(NetUtils.getHost(socketAddress.getAddress()), socketAddress.getPort());
    }

}

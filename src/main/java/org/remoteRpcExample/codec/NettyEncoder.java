 /* power by sekingme */

package org.remoteRpcExample.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.remoteRpcExample.RpcProtocol.MessageHeader;
import org.remoteRpcExample.RpcProtocol.RpcProtocol;


/**
 * NettyEncoder
 */
public class NettyEncoder extends MessageToByteEncoder<RpcProtocol<Object>> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcProtocol<Object> msg, ByteBuf byteBuf) throws Exception {
        MessageHeader msgHeader = msg.getMsgHeader();
        byteBuf.writeShort(msgHeader.getMagic());
        byteBuf.writeByte(msgHeader.getEventType());
        byteBuf.writeByte(msgHeader.getVersion());
        byteBuf.writeByte(msgHeader.getSerialization());
        byteBuf.writeLong(msgHeader.getRequestId());
        byte[] data = new byte[0];
        int msgLength = msgHeader.getMsgLength();
        Serializer rpcSerializer = RpcSerializer.getSerializerByType(msgHeader.getSerialization());
        if (null != rpcSerializer) {
            data = rpcSerializer.serialize(msg.getBody());
            msgLength = data.length;
        }
        byteBuf.writeInt(msgLength);
        byteBuf.writeBytes(data);
    }
}

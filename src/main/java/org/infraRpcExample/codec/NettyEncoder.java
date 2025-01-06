 /* power by sekingme */

package org.infraRpcExample.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.infraRpcExample.exceptions.RemotingException;
import org.infraRpcExample.protocol.MessageHeader;
import org.infraRpcExample.protocol.RpcProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * NettyEncoder
 *
 * @author sekingme
 */
public class NettyEncoder extends MessageToByteEncoder<RpcProtocol<Object>> {

    private final Logger logger = LoggerFactory.getLogger(NettyEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcProtocol<Object> msg, ByteBuf byteBuf) throws Exception {
        if (msg == null) {
            throw new RemotingException("encode msg is null");
        }
        try {
            MessageHeader msgHeader = msg.getMsgHeader();
            byteBuf.writeByte(MessageHeader.MAGIC);
            byteBuf.writeByte(msgHeader.getEventType());
            byteBuf.writeLong(msgHeader.getOpaque());
            byteBuf.writeByte(msgHeader.getUserName());
            byteBuf.writeByte(msgHeader.getUserWorkId());
            Serializer rpcSerializer = RpcSerializer.getSerializerByType(RpcSerializer.KRYO.getType());
            byte[]     data          = rpcSerializer.serialize(msg.getBody());
            int        msgLength     = data.length;
            byteBuf.writeInt(msgLength);
            byteBuf.writeBytes(data);
        } catch (Exception e) {
            logger.error("Failed to encode netty msg. ", e);
        }

    }
}
 /* power by sekingme */

package org.remoteRpcExample.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.remoteRpcExample.RpcProtocol.EventType;
import org.remoteRpcExample.RpcProtocol.MessageHeader;
import org.remoteRpcExample.RpcProtocol.RpcProtocol;
import org.remoteRpcExample.RpcProtocol.RpcProtocolConstants;


import java.util.List;

/**
 * NettyDecoder
 */
public class NettyDecoder extends ByteToMessageDecoder {

    private Class<?> genericClass;

    public NettyDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < RpcProtocolConstants.HEADER_LENGTH) {
            return;
        }

        byteBuf.markReaderIndex();

        short magic = byteBuf.readShort();

        if (RpcProtocolConstants.MAGIC != magic) {
            throw new IllegalArgumentException("magic number is illegal, " + magic);
        }
        byte eventType = byteBuf.readByte();
        byte version = byteBuf.readByte();
        byte serialization = byteBuf.readByte();
        long requestId = byteBuf.readLong();
        int dataLength = byteBuf.readInt();
        byte[] data = new byte[dataLength];

        RpcProtocol rpcProtocol = new RpcProtocol();

        MessageHeader header = new MessageHeader();
        header.setVersion(version);
        header.setSerialization(serialization);
        header.setRequestId(requestId);
        header.setEventType(eventType);
        header.setMsgLength(dataLength);
        byteBuf.readBytes(data);
        rpcProtocol.setMsgHeader(header);
        if (eventType != EventType.HEARTBEAT.getType()) {
            Serializer serializer = RpcSerializer.getSerializerByType(serialization);
            Object obj = serializer.deserialize(data, genericClass);
            rpcProtocol.setBody(obj);
        }
        list.add(rpcProtocol);
    }

}

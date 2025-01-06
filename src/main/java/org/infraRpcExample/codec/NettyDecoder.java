 /* power by sekingme */

package org.infraRpcExample.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.infraRpcExample.protocol.EventType;
import org.infraRpcExample.protocol.MessageHeader;
import org.infraRpcExample.protocol.RpcProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * NettyDecoder
 *
 * @author sekingme
 */
public class NettyDecoder extends ByteToMessageDecoder {

    private final Logger logger = LoggerFactory.getLogger(NettyDecoder.class);

    private Class<?> genericClass;

    public NettyDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        try {
            byteBuf.markReaderIndex();

            // 15 bytes for header （不够最低数据长度，重置读索引）
            if (byteBuf.readableBytes() < 15) {
                byteBuf.resetReaderIndex();
                return;
            }

            short magic = byteBuf.readByte();

            if (MessageHeader.MAGIC != magic) {
                throw new IllegalArgumentException("magic number is illegal, " + magic);
            }
            byte eventType  = byteBuf.readByte();
            long opaque     = byteBuf.readLong();
            byte userName   = byteBuf.readByte();
            byte userWorkId = byteBuf.readByte();
            int  dataLength = byteBuf.readInt();

            /**
             * 重置读索引，等待更多数据重新读取(后续考虑优化为NettyDecoderBetter)
             * 1. TCP 是一种流协议，不保证消息的边界。消息可能被分割成多个 TCP 数据包发送，也可能在接收时合并成一个包。
             * （例如，两个消息可能会被合并成一个 TCP 数据包发送，或者一个大的消息可能会被分成多个数据包。解码器在读取时可能无法立即获取完整的消息，导致数据不足)
             * 2. 在高并发场景下，多个请求可能同时到达服务器。Netty 会将这些请求放入队列，逐个调用 decode 方法进行处理。如果处理顺序不一，某些请求可能在数据未完全到达时就被解码
             * (例如，一个应用程序可能会同时处理多个用户的请求，用户 A 的请求包含多个消息，用户 B 的请求也包含多个消息。由于网络延迟和处理顺序，用户 A 的第二个消息可能在第一个消息之后到达，但由于网络条件，用户 A 的第一个消息可能在第二个消息未完全到达时被处理)
             */
            if (byteBuf.readableBytes() < dataLength) {
                byteBuf.resetReaderIndex();
                return;
            }

            byte[] data = new byte[dataLength];

            RpcProtocol rpcProtocol = new RpcProtocol();

            MessageHeader header = new MessageHeader();
            header.setOpaque(opaque);
            header.setEventType(eventType);
            header.setMsgLength(dataLength);
            header.setUserName(userName);
            header.setUserWorkId(userWorkId);
            byteBuf.readBytes(data);
            rpcProtocol.setMsgHeader(header);
            if (eventType != EventType.HEARTBEAT.getType()) {
                Serializer serializer = RpcSerializer.getSerializerByType(RpcSerializer.KRYO.getType());
                Object     obj        = serializer.deserialize(data, genericClass);
                rpcProtocol.setBody(obj);
            }
            list.add(rpcProtocol);
        } catch (Exception e) {
            logger.error("Failed to decode netty msg. ", e);
        }

    }

}

///*
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *    http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.infraRpcExample.codec;
//
//import io.netty.buffer.ByteBuf;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.handler.codec.ReplayingDecoder;
//import org.infraRpcExample.protocol.MessageHeader;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.List;
//
///**
// * netty decoder
// */
//public class NettyDecoderBetter extends ReplayingDecoder<NettyDecoderBetter.State> {
//    private static final Logger logger = LoggerFactory.getLogger(NettyDecoder.class);
//
//    public NettyDecoderBetter() {
//        super(State.MAGIC);
//    }
//
//    private final MessageHeader header = new MessageHeader();
//
//    /**
//     * decode
//     *
//     * @param ctx channel handler context
//     * @param in byte buffer
//     * @param out out content
//     */
//    @Override
//    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
//        switch (state()) {
//            case MAGIC:
//                checkMagic(in.readByte());
//                checkpoint(State.VERSION);
//                // fallthru
//            case COMMAND:
//                commandHeader.setType(in.readByte());
//                checkpoint(State.OPAQUE);
//                // fallthru
//            case OPAQUE:
//                commandHeader.setOpaque(in.readLong());
//                checkpoint(State.CONTEXT_LENGTH);
//                // fallthru
//            case CONTEXT_LENGTH:
//                commandHeader.setContextLength(in.readInt());
//                checkpoint(State.CONTEXT);
//                // fallthru
//            case CONTEXT:
//                byte[] context = new byte[commandHeader.getContextLength()];
//                in.readBytes(context);
//                commandHeader.setContext(context);
//                checkpoint(State.BODY_LENGTH);
//                // fallthru
//            case BODY_LENGTH:
//                commandHeader.setBodyLength(in.readInt());
//                checkpoint(State.BODY);
//                // fallthru
//            case BODY:
//                byte[] body = new byte[commandHeader.getBodyLength()];
//                in.readBytes(body);
//                //
//                Command packet = new Command();
//                packet.setType(commandType(commandHeader.getType()));
//                packet.setOpaque(commandHeader.getOpaque());
//                packet.setContext(CommandContext.valueOf(commandHeader.getContext()));
//                packet.setBody(body);
//                out.add(packet);
//                //
//                checkpoint(State.MAGIC);
//                break;
//            default:
//                logger.warn("unknown decoder state {}", state());
//        }
//    }
//
//    /**
//     * get command type
//     *
//     * @param type type
//     */
//    private CommandType commandType(byte type) {
//        for (CommandType ct : CommandType.values()) {
//            if (ct.ordinal() == type) {
//                return ct;
//            }
//        }
//        return null;
//    }
//
//    /**
//     * check magic
//     *
//     * @param magic magic
//     */
//    private void checkMagic(byte magic) {
//        if (magic != MessageHeader.MAGIC) {
//            throw new IllegalArgumentException("illegal packet [magic]" + magic);
//        }
//    }
//
//    enum State {
//        MAGIC,
//        COMMAND,
//        OPAQUE,
//        CONTEXT_LENGTH,
//        CONTEXT,
//        BODY_LENGTH,
//        BODY;
//    }
//}

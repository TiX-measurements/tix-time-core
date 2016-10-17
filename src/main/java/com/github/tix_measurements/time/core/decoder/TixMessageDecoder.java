package com.github.tix_measurements.time.core.decoder;

import com.github.tix_measurements.time.core.data.TixDataPacket;
import com.github.tix_measurements.time.core.data.TixPacket;
import com.github.tix_measurements.time.core.data.TixPacketType;
import com.github.tix_measurements.time.core.util.TixCoreUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * {@link MessageToMessageDecoder} that decodes a {@link DatagramPacket} to a TiX Packet, either {@link TixPacket} or
 * {@link TixDataPacket}.
 */
public class TixMessageDecoder extends MessageToMessageDecoder<DatagramPacket> {
	private final Logger logger = LogManager.getLogger(this.getClass());

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void decode(ChannelHandlerContext ctx, DatagramPacket msg,
			List<Object> out) throws Exception {
		logger.entry(ctx, msg, out);
		ByteBuf payload = msg.content();
		TixPacket tixPacket;
		final TixPacketType packetType = payload.readableBytes() == TixPacketType.SHORT.getSize() ?
				TixPacketType.SHORT : TixPacketType.LONG;
		final InetSocketAddress from = msg.sender();
		final InetSocketAddress to = msg.recipient();
		final long initialTimestamp = TixPacket.TIMESTAMP_READER.apply(payload);
		final long receivedTimestamp = TixPacket.TIMESTAMP_READER.apply(payload);
		final long sentTimestamp = TixPacket.TIMESTAMP_READER.apply(payload);
		final long finalTimestamp = TixPacket.TIMESTAMP_READER.apply(payload);
		if (packetType == TixPacketType.LONG && isDataPacket(payload)) {
			byte[] publicKey = payload.readBytes(TixCoreUtils.PUBLCK_KEY_BYTES_LENGTH).array();
			checkDelimiterOrThrowException(payload);
			int messageLength = payload.indexOf(payload.readerIndex(), payload.array().length, TixDataPacket.DATA_DELIMITER.getBytes()[0]) - payload.readerIndex();
			byte[] message = TixCoreUtils.DECODER.apply(new String(payload.readBytes(messageLength).array())).getBytes();
			checkDelimiterOrThrowException(payload);
			byte[] signature = payload.readBytes(TixCoreUtils.SIGNATURE_BYTES_SIZE).array();
			checkDelimiterOrThrowException(payload);
			tixPacket = new TixDataPacket(from, to, initialTimestamp, publicKey, message, signature);
		} else {
			tixPacket = new TixPacket(from, to, packetType, initialTimestamp);
		}
		tixPacket.setReceptionTimestamp(receivedTimestamp);
		tixPacket.setSentTimestamp(sentTimestamp);
		tixPacket.setFinalTimestamp(finalTimestamp);
		out.add(tixPacket);
		logger.exit(tixPacket);
	}

	private void checkDelimiterOrThrowException(ByteBuf payload) {
		if (!checkDelimiter(payload)) {
			String message = "Malformed data package";
			logger.error(message);
			throw new IllegalArgumentException(message);
		}
	}

	private boolean checkDelimiter(ByteBuf payload) {
		ByteBuf delimiterBytes = payload.readBytes(TixDataPacket.DATA_DELIMITER.getBytes().length);
		return TixDataPacket.DATA_DELIMITER.equals(new String(delimiterBytes.array()));
	}

	private boolean isDataPacket(ByteBuf payload) {
		ByteBuf headerBytes = payload.readBytes(TixDataPacket.DATA_HEADER.getBytes().length);
		return TixDataPacket.DATA_HEADER.equals(new String(headerBytes.array())) && checkDelimiter(payload);
	}

}

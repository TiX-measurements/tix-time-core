package com.github.tix_measurements.time.core.decoder;

import com.github.tix_measurements.time.core.data.TixDataPacket;
import com.github.tix_measurements.time.core.data.TixTimestampPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * {@link MessageToMessageDecoder} that decodes a {@link DatagramPacket} to a TiX Packet, either {@link TixTimestampPacket} or {@link TixDataPacket}.
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
		TixTimestampPacket tixPackage;
		final InetSocketAddress from = msg.sender();
		final InetSocketAddress to = msg.recipient();
		final long initialTimestamp = TixTimestampPacket.TIMESTAMP_READER.apply(payload);
		final long receivedTimestamp = TixTimestampPacket.TIMESTAMP_READER.apply(payload);
		final long sentTimestamp = TixTimestampPacket.TIMESTAMP_READER.apply(payload);
		final long finalTimestamp = TixTimestampPacket.TIMESTAMP_READER.apply(payload);
		if (payload.isReadable()) {
			String rawData = payload.readBytes(payload.readableBytes()).toString(CharsetUtil.UTF_8).trim();
			if (StringUtil.isNullOrEmpty(rawData)) {
				logger.error("Empty data on a TixDataPacket");
				throw new IllegalArgumentException("empty data on TixDataPacket");
			}
			String[] data = rawData.split(TixDataPacket.DATA_DELIMITER);
			if (data[0].equals(TixDataPacket.DATA_HEADER)) {
				final String publicKey = TixDataPacket.DECODER.apply(data[1].trim()).trim();
				final String logFileName = TixDataPacket.DECODER.apply(data[2].trim()).trim();
				final String message = TixDataPacket.DECODER.apply(data[3].trim()).trim();
				final byte[] signature = TixDataPacket.BYTE_DECODER.apply(data[4].trim());
				tixPackage = new TixDataPacket(from, to, initialTimestamp, publicKey, logFileName, message, signature);
			} else {
				logger.error("Malformed data package received {}", rawData);
				throw new IllegalArgumentException("Malformed data package received " + rawData);
			}
		} else {
			tixPackage = new TixTimestampPacket(from, to, initialTimestamp);
		}
		tixPackage.setReceptionTimestamp(receivedTimestamp);
		tixPackage.setSentTimestamp(sentTimestamp);
		tixPackage.setFinalTimestamp(finalTimestamp);
		out.add(tixPackage);
		logger.exit(tixPackage);
	}

}

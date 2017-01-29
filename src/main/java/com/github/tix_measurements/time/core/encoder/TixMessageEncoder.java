package com.github.tix_measurements.time.core.encoder;

import com.github.tix_measurements.time.core.data.TixDataPacket;
import com.github.tix_measurements.time.core.data.TixPacket;
import com.github.tix_measurements.time.core.data.TixPacketType;
import com.github.tix_measurements.time.core.util.TixCoreUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.apache.commons.lang3.RandomUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * {@link MessageToMessageEncoder} that encodes TiX Packet, either {@link TixPacket} or {@link TixDataPacket} into a {@link DatagramPacket}.
 */
public class TixMessageEncoder extends MessageToMessageEncoder<TixPacket> {
	private final Logger logger = LogManager.getLogger(this.getClass());

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void encode(ChannelHandlerContext ctx, TixPacket msg, List<Object> out) throws Exception {
		logger.entry(ctx, msg, out);
		DatagramPacket datagramPacket = new DatagramPacket(Unpooled.buffer(), msg.getTo(), msg.getFrom());
		TixPacket.TIMESTAMP_WRITER.apply(datagramPacket.content(), msg.getInitialTimestamp());
		TixPacket.TIMESTAMP_WRITER.apply(datagramPacket.content(), msg.getReceptionTimestamp());
		TixPacket.TIMESTAMP_WRITER.apply(datagramPacket.content(), msg.getSentTimestamp());
		TixPacket.TIMESTAMP_WRITER.apply(datagramPacket.content(), msg.getFinalTimestamp());
		if (msg.getType() == TixPacketType.LONG) {
			if (msg instanceof TixDataPacket) {
				TixDataPacket dataPacket = (TixDataPacket) msg;
				datagramPacket.content().writeBytes(TixDataPacket.DATA_HEADER.getBytes());
				datagramPacket.content().writeBytes(TixDataPacket.DATA_DELIMITER.getBytes());
				TixPacket.TIMESTAMP_WRITER.apply(datagramPacket.content(), dataPacket.getUserId());
				TixPacket.TIMESTAMP_WRITER.apply(datagramPacket.content(), dataPacket.getInstallationId());
				datagramPacket.content().writeBytes(TixDataPacket.DATA_DELIMITER.getBytes());
				for (byte[] bytes : new byte[][]{
						dataPacket.getPublicKey(),
						TixCoreUtils.ENCODER.apply(dataPacket.getMessage()).getBytes(),
						dataPacket.getSignature()}){
					datagramPacket.content().writeBytes(bytes);
					datagramPacket.content().writeBytes(TixDataPacket.DATA_DELIMITER.getBytes());
				}
			}
			int randomBytesToWrite = TixPacketType.LONG.getSize() - datagramPacket.content().readableBytes();
			byte[] fillingBytes = RandomUtils.nextBytes(randomBytesToWrite);
			datagramPacket.content().writeBytes(fillingBytes);
		}
		out.add(datagramPacket);
		logger.exit(out);
	}
}

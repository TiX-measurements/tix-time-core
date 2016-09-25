package com.github.tix_measurements.time.core.encoder;

import com.github.tix_measurements.time.core.data.TixDataPacket;
import com.github.tix_measurements.time.core.data.TixTimestampPacket;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * {@link MessageToMessageEncoder} that encodes TiX Packet, either {@link TixTimestampPacket} or {@link TixDataPacket} into a {@link DatagramPacket}.
 */
public class TixMessageEncoder extends MessageToMessageEncoder<TixTimestampPacket> {
	private final Logger logger = LogManager.getLogger(this.getClass());

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void encode(ChannelHandlerContext ctx, TixTimestampPacket msg, List<Object> out) throws Exception {
		logger.entry(ctx, msg, out);
		DatagramPacket packet = new DatagramPacket(Unpooled.buffer(), msg.getTo(), msg.getFrom());
		TixTimestampPacket.TIMESTAMP_WRITER.apply(packet.content(), msg.getInitialTimestamp());
		TixTimestampPacket.TIMESTAMP_WRITER.apply(packet.content(), msg.getReceptionTimestamp());
		TixTimestampPacket.TIMESTAMP_WRITER.apply(packet.content(), msg.getSentTimestamp());
		TixTimestampPacket.TIMESTAMP_WRITER.apply(packet.content(), msg.getFinalTimestamp());
		if (msg instanceof TixDataPacket) {
			String data = TixDataPacket.DATA_HEADER +
					TixDataPacket.DATA_DELIMITER +
					TixDataPacket.STR_ENCODER.apply(((TixDataPacket) msg).getPublicKey()) +
					TixDataPacket.DATA_DELIMITER +
					TixDataPacket.STR_ENCODER.apply(((TixDataPacket) msg).getFilename()) +
					TixDataPacket.DATA_DELIMITER +
					TixDataPacket.STR_ENCODER.apply(((TixDataPacket) msg).getMessage()) +
					TixDataPacket.DATA_DELIMITER +
					TixDataPacket.ENCODER.apply(((TixDataPacket) msg).getSignature()) +
					TixDataPacket.DATA_DELIMITER ;
			packet.content().writeBytes(data.getBytes(CharsetUtil.UTF_8));
		}
		out.add(packet);
		logger.exit(out);
	}
}

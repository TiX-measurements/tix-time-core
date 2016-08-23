package ar.edu.itba.tix.time.core.encoder;

import ar.edu.itba.tix.time.core.data.TixDataPackage;
import ar.edu.itba.tix.time.core.data.TixTimestampPackage;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class TixMessageEncoder extends MessageToMessageEncoder<TixTimestampPackage> {
	private final Logger logger = LogManager.getLogger(this.getClass());

	@Override
	protected void encode(ChannelHandlerContext ctx, TixTimestampPackage msg, List<Object> out) throws Exception {
		logger.entry(ctx, msg, out);
		DatagramPacket packet = new DatagramPacket(Unpooled.buffer(), msg.getTo(), msg.getFrom());
		TixTimestampPackage.TIMESTAMP_WRITER.apply(packet.content(), msg.getInitalTimestamp());
		TixTimestampPackage.TIMESTAMP_WRITER.apply(packet.content(), msg.getReceptionTimestamp());
		TixTimestampPackage.TIMESTAMP_WRITER.apply(packet.content(), msg.getSentTimestamp());
		TixTimestampPackage.TIMESTAMP_WRITER.apply(packet.content(), msg.getFinalTimestamp());
		if (msg instanceof TixDataPackage) {
			String data = TixDataPackage.DATA_HEADER +
					TixDataPackage.DATA_DELIMITER +
					TixDataPackage.STR_ENCODER.apply(((TixDataPackage) msg).getPublicKey()) +
					TixDataPackage.DATA_DELIMITER +
					TixDataPackage.STR_ENCODER.apply(((TixDataPackage) msg).getFilename()) +
					TixDataPackage.DATA_DELIMITER +
					TixDataPackage.STR_ENCODER.apply(((TixDataPackage) msg).getMessage()) +
					TixDataPackage.DATA_DELIMITER +
					TixDataPackage.ENCODER.apply(((TixDataPackage) msg).getSignature()) +
					TixDataPackage.DATA_DELIMITER ;
			packet.content().writeBytes(data.getBytes(CharsetUtil.UTF_8));
		}
		out.add(packet);
		logger.exit(out);
	}
}

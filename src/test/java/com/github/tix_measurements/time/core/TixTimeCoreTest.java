package com.github.tix_measurements.time.core;

import com.github.tix_measurements.time.core.data.TixDataPacket;
import com.github.tix_measurements.time.core.data.TixTimestampPacket;
import com.github.tix_measurements.time.core.decoder.TixMessageDecoder;
import com.github.tix_measurements.time.core.encoder.TixMessageEncoder;
import com.github.tix_measurements.time.core.util.TixTimeUtils;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.DatagramPacket;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.security.*;
import java.util.Base64;

import static org.assertj.core.api.Assertions.*;

public class TixTimeCoreTest {

	private EmbeddedChannel embeddedChannel;
	private InetSocketAddress from;
	private InetSocketAddress to;
	private String publicKey;
	private String filename;
	private String message;
	private byte[] signature;

	@Before
	public void setUp() throws Exception {
		embeddedChannel = new EmbeddedChannel(new TixMessageEncoder(), new TixMessageDecoder());
		from = InetSocketAddress.createUnresolved("localhost", 4500);
		to = InetSocketAddress.createUnresolved("localhost", 4501);
		setUpData();
	}

	private void setUpData() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
		KeyPair keyPair = TixTimeUtils.NEW_KEY_PAIR.get();
		publicKey = TixDataPacket.ENCODER.apply(keyPair.getPublic().getEncoded());
		filename = "a";
		message = "a";
		signature = TixTimeUtils.sign(message, keyPair);
	}

	@SuppressWarnings("unchecked")
	private <T extends TixTimestampPacket> T passThroughChannel(T message) {
		assertThat(embeddedChannel.writeOutbound(message)).isTrue();
		Object o = embeddedChannel.readOutbound();
		assertThat(o).isNotNull();
		DatagramPacket datagramPacket = (DatagramPacket)o;
		assertThat(embeddedChannel.writeInbound(datagramPacket)).isTrue();
		Object returnedMessage = embeddedChannel.readInbound();
		assertThat(returnedMessage).isNotNull();
		return (T)returnedMessage;
	}

	@Test
	public void shouldEncodeAndDecodeTixTimestampPackage() throws Exception {
		long initialTimestamp = TixTimeUtils.NANOS_OF_DAY.get();
		TixTimestampPacket timestampPackage = new TixTimestampPacket(from, to, initialTimestamp);
		timestampPackage.setReceptionTimestamp(initialTimestamp + 1);
		timestampPackage.setSentTimestamp(initialTimestamp + 2);
		timestampPackage.setFinalTimestamp(initialTimestamp + 3);
		TixTimestampPacket returnedTimestampPackage = passThroughChannel(timestampPackage);
		assertThat(timestampPackage).isNotSameAs(returnedTimestampPackage)
									.isEqualTo(returnedTimestampPackage);
	}

	@Test
	public void shouldEncodeAndDecodeTixDataPackage() throws Exception {
		long initialTimestamp = TixTimeUtils.NANOS_OF_DAY.get();
		TixDataPacket dataPackage = new TixDataPacket(from, to, initialTimestamp,
				publicKey, filename, message, signature);
		dataPackage.setReceptionTimestamp(initialTimestamp + 1);
		dataPackage.setSentTimestamp(initialTimestamp + 2);
		dataPackage.setFinalTimestamp(initialTimestamp + 3);
		TixDataPacket returnedDataPackage = passThroughChannel(dataPackage);
		assertThat(returnedDataPackage).isEqualTo(dataPackage)
				                       .isNotSameAs(dataPackage);
	}
}

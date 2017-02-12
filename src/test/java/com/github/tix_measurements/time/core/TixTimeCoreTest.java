package com.github.tix_measurements.time.core;

import com.github.tix_measurements.time.core.data.TixDataPacket;
import com.github.tix_measurements.time.core.data.TixDataPacketTest;
import com.github.tix_measurements.time.core.data.TixPacket;
import com.github.tix_measurements.time.core.data.TixPacketType;
import com.github.tix_measurements.time.core.decoder.TixMessageDecoder;
import com.github.tix_measurements.time.core.encoder.TixMessageEncoder;
import com.github.tix_measurements.time.core.util.TixCoreUtils;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.DatagramPacket;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.security.*;

import static org.assertj.core.api.Assertions.*;

public class TixTimeCoreTest {

	private EmbeddedChannel embeddedChannel;
	private InetSocketAddress from;
	private InetSocketAddress to;
	private byte[] publicKey;
	private byte[] message;
	private byte[] signature;
	private long userId;
	private long installationId;

	@Before
	public void setUp() throws Exception {
		embeddedChannel = new EmbeddedChannel(new TixMessageEncoder(), new TixMessageDecoder());
		from = InetSocketAddress.createUnresolved("localhost", 4500);
		to = InetSocketAddress.createUnresolved("localhost", 4501);
		setUpData();
	}

	private void setUpData() throws InterruptedException {
		userId = 1L;
		installationId = 1L;
		KeyPair keyPair = TixCoreUtils.NEW_KEY_PAIR.get();
		publicKey = keyPair.getPublic().getEncoded();
		message = TixDataPacketTest.generateMessage();
		signature = TixCoreUtils.sign(message, keyPair);
	}

	@SuppressWarnings("unchecked")
	private <T extends TixPacket> T passThroughChannel(T message) {
		assertThat(embeddedChannel.writeOutbound(message)).isTrue();
		Object o = embeddedChannel.readOutbound();
		assertThat(o).isNotNull();
		DatagramPacket datagramPacket = (DatagramPacket)o;
		assertThat(embeddedChannel.writeInbound(datagramPacket)).isTrue();
		Object returnedMessage = embeddedChannel.readInbound();
		assertThat(returnedMessage).isNotNull();
		return (T) returnedMessage;
	}

	private <T extends TixPacket> void testPassThroughChannel(T packet) {
		packet.setReceptionTimestamp(packet.getInitialTimestamp() + 1);
		packet.setSentTimestamp(packet.getInitialTimestamp() + 2);
		packet.setFinalTimestamp(packet.getInitialTimestamp() + 3);
		T returnedTimestampPackage = passThroughChannel(packet);
		assertThat(packet).isNotSameAs(returnedTimestampPackage)
				.isEqualTo(returnedTimestampPackage);
	}

	@Test
	public void shouldEncodeAndDecodeTixShortPacket() throws Exception {
		TixPacket packet = new TixPacket(from, to, TixPacketType.SHORT, TixCoreUtils.NANOS_OF_DAY.get());
		testPassThroughChannel(packet);
	}

	@Test
	public void shouldEncodeAndDecodeTixLongPacket() throws Exception {
		TixPacket packet = new TixPacket(from, to, TixPacketType.LONG, TixCoreUtils.NANOS_OF_DAY.get());
		testPassThroughChannel(packet);
	}

	@Test
	public void shouldEncodeAndDecodeTixDataPackage() throws Exception {
		TixDataPacket dataPackage = new TixDataPacket(from, to, TixCoreUtils.NANOS_OF_DAY.get(), userId, installationId, publicKey, message, signature);
		testPassThroughChannel(dataPackage);
	}
}

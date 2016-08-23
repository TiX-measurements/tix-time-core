package ar.edu.itba.tix.time.core;

import ar.edu.itba.tix.time.core.data.TixDataPackage;
import ar.edu.itba.tix.time.core.data.TixTimestampPackage;
import ar.edu.itba.tix.time.core.decoder.TixMessageDecoder;
import ar.edu.itba.tix.time.core.encoder.TixMessageEncoder;
import ar.edu.itba.tix.time.core.util.TixTimeUitl;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.DatagramPacket;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.security.*;
import java.util.Base64;

import static org.junit.Assert.*;

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

	private void setUpData() {
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(512);
			KeyPair keyPair = generator.genKeyPair();
			publicKey = new String(Base64.getEncoder().encode(keyPair.getPublic().getEncoded()));
			filename = "a";
			message = "a";
			Signature signer = Signature.getInstance("SHA1WithRSA");
			signer.initSign(keyPair.getPrivate());
			signer.update(message.getBytes());
			signature = signer.sign();
		} catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
			e.printStackTrace();
		}

	}

	private <T extends TixTimestampPackage> T passThroughChannel(T message) {
		assertTrue(embeddedChannel.writeOutbound(message));
		Object o = embeddedChannel.readOutbound();
		assertNotNull(o);
		DatagramPacket datagramPacket = (DatagramPacket)o;
		assertTrue(embeddedChannel.writeInbound(datagramPacket));
		Object returnedMessage = embeddedChannel.readInbound();
		assertNotNull(returnedMessage);
		return (T)returnedMessage;
	}

	@Test
	public void shouldEncodeAndDecodeTixTimestampPackage() throws Exception {
		long initialTimestamp = TixTimeUitl.NANOS_OF_DAY.get();
		TixTimestampPackage timestampPackage = new TixTimestampPackage(from, to, initialTimestamp);
		timestampPackage.setReceptionTimestamp(initialTimestamp + 1);
		timestampPackage.setSentTimestamp(initialTimestamp + 2);
		timestampPackage.setFinalTimestamp(initialTimestamp + 3);
		TixTimestampPackage returnedTimestampPackage = passThroughChannel(timestampPackage);
		assertFalse(timestampPackage == returnedTimestampPackage);
		assertEquals(timestampPackage, returnedTimestampPackage);
	}

	@Test
	public void shouldEncodeAndDecodeTixDataPackage() throws Exception {
		long initialTimestamp = TixTimeUitl.NANOS_OF_DAY.get();
		TixDataPackage dataPackage = new TixDataPackage(from, to, initialTimestamp,
				publicKey, filename, message, signature);
		dataPackage.setReceptionTimestamp(initialTimestamp + 1);
		dataPackage.setSentTimestamp(initialTimestamp + 2);
		dataPackage.setFinalTimestamp(initialTimestamp + 3);
		TixDataPackage returnedDataPackage = passThroughChannel(dataPackage);
		assertFalse(dataPackage == returnedDataPackage);
		assertEquals(dataPackage, returnedDataPackage);
	}
}

package com.github.tix_measurements.time.core.data;

import com.github.tix_measurements.time.core.util.TixTimeUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class TixDataPacketTest {
	private static final KeyPair KEY_PAIR;
	private static final InetSocketAddress FROM;
	private static final InetSocketAddress TO;
	private static final String PUBLIC_KEY;
	private static final String FILENAME;
	private static final String MESSAGE;
	private static final byte[] SIGNATURE;
	private static final long INITIAL_TIMESTAMP;
	private static final long RECEPTION_TIMESTAMP;
	private static final long SENT_TIMESTAMP;
	private static final long FINAL_TIMESTAMP;

	private static String generateMessage() throws InterruptedException {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 4; j++) {
				sb.append(Long.toString(TixTimeUtils.NANOS_OF_DAY.get()));
				Thread.sleep(5L);
			}
		}
		return sb.toString();
	}

	static {
		try {
			FROM = new InetSocketAddress(InetAddress.getByName("8.8.8.8"), 4500);
			TO = new InetSocketAddress(InetAddress.getLocalHost(), 4500);
			KEY_PAIR = TixTimeUtils.NEW_KEY_PAIR.get();
			PUBLIC_KEY = TixDataPacket.ENCODER.apply(KEY_PAIR.getPublic().getEncoded());
			FILENAME = RandomStringUtils.randomAlphanumeric(10);
			MESSAGE = generateMessage();
			SIGNATURE = TixTimeUtils.sign(MESSAGE, KEY_PAIR);
			INITIAL_TIMESTAMP = TixTimeUtils.NANOS_OF_DAY.get();
			Thread.sleep(5L);
			RECEPTION_TIMESTAMP = TixTimeUtils.NANOS_OF_DAY.get();
			Thread.sleep(5L);
			SENT_TIMESTAMP = TixTimeUtils.NANOS_OF_DAY.get();
			Thread.sleep(5L);
			FINAL_TIMESTAMP = TixTimeUtils.NANOS_OF_DAY.get();
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	private TixDataPacket dataPacket;

	@Before
	public void setup() {
		dataPacket = new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, PUBLIC_KEY, FILENAME, MESSAGE, SIGNATURE);
	}

	@Test
	public void testConstructor() {
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixDataPacket(null, TO, INITIAL_TIMESTAMP, PUBLIC_KEY, FILENAME, MESSAGE, SIGNATURE));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixDataPacket(FROM, null, INITIAL_TIMESTAMP, PUBLIC_KEY, FILENAME, MESSAGE, SIGNATURE));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixDataPacket(FROM, TO, -1, PUBLIC_KEY, FILENAME, MESSAGE, SIGNATURE));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, StringUtils.EMPTY, FILENAME, MESSAGE, SIGNATURE));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, StringUtils.SPACE, FILENAME, MESSAGE, SIGNATURE));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, null, FILENAME, MESSAGE, SIGNATURE));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, PUBLIC_KEY, StringUtils.EMPTY, MESSAGE, SIGNATURE));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, PUBLIC_KEY, StringUtils.SPACE, MESSAGE, SIGNATURE));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, PUBLIC_KEY, null, MESSAGE, SIGNATURE));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, PUBLIC_KEY, FILENAME, StringUtils.EMPTY, SIGNATURE));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, PUBLIC_KEY, FILENAME, StringUtils.SPACE, SIGNATURE));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, PUBLIC_KEY, FILENAME, null, SIGNATURE));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, PUBLIC_KEY, FILENAME, MESSAGE, null));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, PUBLIC_KEY, FILENAME, MESSAGE, new byte[10]));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> {
					byte[] signature = Arrays.copyOf(SIGNATURE, SIGNATURE.length);
					signature[0]++;
					new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, PUBLIC_KEY, FILENAME, MESSAGE, signature);
				});
	}

	@Test
	public void testConstructorProperties() {
		assertThat(dataPacket.getFrom()).isEqualTo(FROM);
		assertThat(dataPacket.getTo()).isEqualTo(TO);
		assertThat(dataPacket.getInitialTimestamp()).isEqualTo(INITIAL_TIMESTAMP);
		assertThat(dataPacket.getFilename()).isEqualTo(FILENAME);
		assertThat(dataPacket.getMessage()).isEqualTo(MESSAGE);
		assertThat(dataPacket.getPublicKey()).isEqualTo(PUBLIC_KEY);
		assertThat(dataPacket.getSignature()).isEqualTo(SIGNATURE);
	}

	@Test
	public void testSetAndGetReceptionTimestamp() {
		dataPacket.setReceptionTimestamp(RECEPTION_TIMESTAMP);
		assertThat(dataPacket.getReceptionTimestamp()).isEqualTo(RECEPTION_TIMESTAMP);
	}

	@Test
	public void testSetAndGetSentTimestamp() {
		dataPacket.setSentTimestamp(SENT_TIMESTAMP);
		assertThat(dataPacket.getSentTimestamp()).isEqualTo(SENT_TIMESTAMP);
	}

	@Test
	public void testSetAndGetFinalTimestamp() {
		dataPacket.setFinalTimestamp(FINAL_TIMESTAMP);
		assertThat(dataPacket.getFinalTimestamp()).isEqualTo(FINAL_TIMESTAMP);
	}

	@Test
	public void testHashCode() throws UnknownHostException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, InterruptedException {
		int hashCode = dataPacket.hashCode();
		// Assert that hashcode does not change over time
		assertThat(hashCode).isEqualTo(dataPacket.hashCode());
		// Assert that hashcode doesn't change when the object mutates
		dataPacket.setReceptionTimestamp(RECEPTION_TIMESTAMP);
		assertThat(hashCode).isEqualTo(dataPacket.hashCode());
		dataPacket.setSentTimestamp(SENT_TIMESTAMP);
		assertThat(hashCode).isEqualTo(dataPacket.hashCode());
		dataPacket.setFinalTimestamp(FINAL_TIMESTAMP);
		assertThat(hashCode).isEqualTo(dataPacket.hashCode());
		// Assert that hashcode changes when initial object's values changes
		// Inversion of from - to
		TixDataPacket other = new TixDataPacket(TO, FROM, INITIAL_TIMESTAMP, PUBLIC_KEY, FILENAME, MESSAGE, SIGNATURE);
		assertThat(dataPacket.hashCode()).isNotEqualTo(other.hashCode());
		// Different from/to IPs
		InetSocketAddress otherAddress = new InetSocketAddress(InetAddress.getByName("8.8.8.4"), 4500);
		other = new TixDataPacket(otherAddress, TO, INITIAL_TIMESTAMP, PUBLIC_KEY, FILENAME, MESSAGE, SIGNATURE);
		assertThat(dataPacket.hashCode()).isNotEqualTo(other.hashCode());
		other = new TixDataPacket(FROM, otherAddress, INITIAL_TIMESTAMP, PUBLIC_KEY, FILENAME, MESSAGE, SIGNATURE);
		assertThat(dataPacket.hashCode()).isNotEqualTo(other.hashCode());
		// Different initial timestamp
		other = new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP + 1, PUBLIC_KEY, FILENAME, MESSAGE, SIGNATURE);
		assertThat(dataPacket.hashCode()).isNotEqualTo(other.hashCode());
		// Different filename
		String otherFilename = RandomStringUtils.random(10);
		other = new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, PUBLIC_KEY, otherFilename, MESSAGE, SIGNATURE);
		assertThat(dataPacket.hashCode()).isNotEqualTo(other.hashCode());
		// Different public key - signature
		KeyPair otherKeyPair = TixTimeUtils.NEW_KEY_PAIR.get();
		String otherPublicKey = TixDataPacket.ENCODER.apply(otherKeyPair.getPublic().getEncoded());
		byte[] otherSignature = TixTimeUtils.sign(MESSAGE, otherKeyPair);
		other = new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, otherPublicKey, FILENAME, MESSAGE, otherSignature);
		assertThat(dataPacket.hashCode()).isNotEqualTo(other.hashCode());
		// Different message - signature
		String otherMessage = generateMessage();
		otherSignature = TixTimeUtils.sign(otherMessage, KEY_PAIR);
		other = new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, PUBLIC_KEY, FILENAME, otherMessage, otherSignature);
		assertThat(dataPacket.hashCode()).isNotEqualTo(other.hashCode());
	}

	@Test
	public void testEquals() {
		assertThat(dataPacket).isNotEqualTo(null);
		assertThat(dataPacket).isNotEqualTo(new Object());
		assertThat(dataPacket).isEqualTo(dataPacket);
		TixDataPacket other = new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, PUBLIC_KEY, FILENAME, MESSAGE, SIGNATURE);
		assertThat(dataPacket).isEqualTo(other);
		other.setReceptionTimestamp(RECEPTION_TIMESTAMP);
		assertThat(dataPacket).isNotEqualTo(other);
		dataPacket.setReceptionTimestamp(RECEPTION_TIMESTAMP);
		assertThat(dataPacket).isEqualTo(other);
		other.setSentTimestamp(SENT_TIMESTAMP);
		assertThat(dataPacket).isNotEqualTo(other);
		dataPacket.setSentTimestamp(SENT_TIMESTAMP);
		assertThat(dataPacket).isEqualTo(other);
		other.setFinalTimestamp(FINAL_TIMESTAMP);
		assertThat(dataPacket).isNotEqualTo(other);
		dataPacket.setFinalTimestamp(FINAL_TIMESTAMP);
		assertThat(dataPacket).isEqualTo(other);
	}
}

package com.github.tix_measurements.time.core.data;

import com.github.tix_measurements.time.core.util.TixCoreUtils;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
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
	private static final byte[] PUBLIC_KEY;
	private static final byte[] MESSAGE;
	private static final byte[] SIGNATURE;
	private static final long INITIAL_TIMESTAMP;
	private static final long RECEPTION_TIMESTAMP;
	private static final long SENT_TIMESTAMP;
	private static final long FINAL_TIMESTAMP;
	private static final long USER_ID;
	private static final long INSTALLATION_ID;

	public static byte[] generateMessage() throws InterruptedException {
		int reports = 10;
		int timestamps = 4;
		int timestampSize = Long.BYTES;
		int rowSize = timestamps * timestampSize;
		byte[] message = new byte[reports * rowSize];
		for (int i = 0; i < reports; i++) {
			for (int j = 0; j < timestamps; j++) {
				byte[] nanosInBytes = ByteBuffer.allocate(timestampSize).putLong(TixCoreUtils.NANOS_OF_DAY.get()).array();
				for (int k = 0; k < timestampSize; k++) {
					message[i * rowSize + j * timestampSize + k] = nanosInBytes[k];
				}
				Thread.sleep(5L);
			}
		}
		return message;
	}

	static {
		try {
			FROM = new InetSocketAddress(InetAddress.getByName("8.8.8.8"), 4500);
			TO = new InetSocketAddress(InetAddress.getLocalHost(), 4500);
			KEY_PAIR = TixCoreUtils.NEW_KEY_PAIR.get();
			PUBLIC_KEY = KEY_PAIR.getPublic().getEncoded();
			MESSAGE = generateMessage();
			SIGNATURE = TixCoreUtils.sign(MESSAGE, KEY_PAIR);
			INITIAL_TIMESTAMP = TixCoreUtils.NANOS_OF_DAY.get();
			Thread.sleep(5L);
			RECEPTION_TIMESTAMP = TixCoreUtils.NANOS_OF_DAY.get();
			Thread.sleep(5L);
			SENT_TIMESTAMP = TixCoreUtils.NANOS_OF_DAY.get();
			Thread.sleep(5L);
			FINAL_TIMESTAMP = TixCoreUtils.NANOS_OF_DAY.get();
			USER_ID = 1L;
			INSTALLATION_ID = 1L;
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	private TixDataPacket dataPacket;

	@Before
	public void setup() {
		dataPacket = new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, USER_ID, INSTALLATION_ID, PUBLIC_KEY, MESSAGE, SIGNATURE);
	}

	@Test
	public void testConstructor() {
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixDataPacket(null, TO, INITIAL_TIMESTAMP, USER_ID, INSTALLATION_ID, PUBLIC_KEY, MESSAGE, SIGNATURE));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixDataPacket(FROM, null, INITIAL_TIMESTAMP, USER_ID, INSTALLATION_ID, PUBLIC_KEY, MESSAGE, SIGNATURE));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixDataPacket(FROM, TO, -1, USER_ID, INSTALLATION_ID, PUBLIC_KEY, MESSAGE, SIGNATURE));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, -1L, INSTALLATION_ID, PUBLIC_KEY, MESSAGE, SIGNATURE));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, USER_ID, -1L, PUBLIC_KEY, MESSAGE, SIGNATURE));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, USER_ID, INSTALLATION_ID, new byte[0], MESSAGE, SIGNATURE));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, USER_ID, INSTALLATION_ID, null, MESSAGE, SIGNATURE));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, USER_ID, INSTALLATION_ID, PUBLIC_KEY, new byte[0], SIGNATURE));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, USER_ID, INSTALLATION_ID, PUBLIC_KEY, null, SIGNATURE));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, USER_ID, INSTALLATION_ID, PUBLIC_KEY, MESSAGE, null));
	}

	@Test
	public void testConstructorProperties() {
		assertThat(dataPacket.getFrom()).isEqualTo(FROM);
		assertThat(dataPacket.getTo()).isEqualTo(TO);
		assertThat(dataPacket.getType()).isEqualTo(TixPacketType.LONG);
		assertThat(dataPacket.getInitialTimestamp()).isEqualTo(INITIAL_TIMESTAMP);
		assertThat(dataPacket.getUserId()).isEqualTo(USER_ID);
		assertThat(dataPacket.getInstallationId()).isEqualTo(INSTALLATION_ID);
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
		TixDataPacket other = new TixDataPacket(TO, FROM, INITIAL_TIMESTAMP, USER_ID, INSTALLATION_ID, PUBLIC_KEY, MESSAGE, SIGNATURE);
		assertThat(dataPacket.hashCode()).isNotEqualTo(other.hashCode());
		// Different from/to IPs
		InetSocketAddress otherAddress = new InetSocketAddress(InetAddress.getByName("8.8.8.4"), 4500);
		other = new TixDataPacket(otherAddress, TO, INITIAL_TIMESTAMP, USER_ID, INSTALLATION_ID, PUBLIC_KEY, MESSAGE, SIGNATURE);
		assertThat(dataPacket.hashCode()).isNotEqualTo(other.hashCode());
		other = new TixDataPacket(FROM, otherAddress, INITIAL_TIMESTAMP, USER_ID, INSTALLATION_ID, PUBLIC_KEY, MESSAGE, SIGNATURE);
		assertThat(dataPacket.hashCode()).isNotEqualTo(other.hashCode());
		// Different initial timestamp
		other = new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP + 1, USER_ID, INSTALLATION_ID, PUBLIC_KEY, MESSAGE, SIGNATURE);
		assertThat(dataPacket.hashCode()).isNotEqualTo(other.hashCode());
		// Different user
		other = new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, USER_ID + 1, INSTALLATION_ID, PUBLIC_KEY, MESSAGE, SIGNATURE);
		assertThat(dataPacket.hashCode()).isNotEqualTo(other.hashCode());
		// Different installation
		other = new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, USER_ID, INSTALLATION_ID + 1, PUBLIC_KEY, MESSAGE, SIGNATURE);
		assertThat(dataPacket.hashCode()).isNotEqualTo(other.hashCode());
		// Different public key - signature
		KeyPair otherKeyPair = TixCoreUtils.NEW_KEY_PAIR.get();
		byte[] otherPublicKey = otherKeyPair.getPublic().getEncoded();
		byte[] otherSignature = TixCoreUtils.sign(MESSAGE, otherKeyPair);
		other = new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, USER_ID, INSTALLATION_ID, otherPublicKey, MESSAGE, otherSignature);
		assertThat(dataPacket.hashCode()).isNotEqualTo(other.hashCode());
		// Different message - signature
		byte[] otherMessage = generateMessage();
		otherSignature = TixCoreUtils.sign(otherMessage, KEY_PAIR);
		other = new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, USER_ID, INSTALLATION_ID, PUBLIC_KEY, otherMessage, otherSignature);
		assertThat(dataPacket.hashCode()).isNotEqualTo(other.hashCode());
	}

	@Test
	public void testIsValidReturnsTrueOnGoodSignature() {
		assertThat(dataPacket.isValid()).isTrue();
	}

	@Test
	public void testIsValidReturnsFalseOnBadSignature() {
		byte[] badSignature = Arrays.copyOf(SIGNATURE, SIGNATURE.length);
		badSignature[0]++;
		TixDataPacket packet = new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, USER_ID, INSTALLATION_ID, PUBLIC_KEY, MESSAGE, badSignature);
		assertThat(packet.isValid()).isFalse();
	}

	@Test
	public void testExceptionThrownOnInvalidSignature() {
		assertThatExceptionOfType(IllegalStateException.class)
				.isThrownBy(() -> {
					TixDataPacket packet = new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, USER_ID, INSTALLATION_ID, PUBLIC_KEY, MESSAGE, new byte[0]);
					packet.isValid();
				});
	}

	@Test
	public void testEquals() {
		assertThat(dataPacket).isNotEqualTo(null);
		assertThat(dataPacket).isNotEqualTo(new Object());
		assertThat(dataPacket).isEqualTo(dataPacket);
		TixDataPacket other = new TixDataPacket(FROM, TO, INITIAL_TIMESTAMP, USER_ID, INSTALLATION_ID, PUBLIC_KEY, MESSAGE, SIGNATURE);
		assertThat(dataPacket).isEqualTo(other);
		other.setReceptionTimestamp(RECEPTION_TIMESTAMP);
		assertThat(dataPacket).isNotEqualTo(other);
		dataPacket.setReceptionTimestamp(RECEPTION_TIMESTAMP);
		assertThat(dataPacket).isEqualTo(other);
		other.setSentTimestamp(SENT_TIMESTAMP);
		assertThat(dataPacket).isNotEqualTo(other);
		dataPacket.setSentTimestamp(SENT_TIMESTAMP);
		assertThat(dataPacket).isEqualTo(other);
	}
}

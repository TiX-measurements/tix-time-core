package com.github.tix_measurements.time.core.data;

import com.github.tix_measurements.time.core.util.TixCoreUtils;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import static org.assertj.core.api.Assertions.*;

public class TixPacketTest {
	private static final InetSocketAddress FROM;
	private static final InetSocketAddress TO;
	private static final long INITIAL_TIMESTAMP;
	private static final long RECEPTION_TIMESTAMP;
	private static final long SENT_TIMESTAMP;
	private static final long FINAL_TIMESTAMP;

	static {
		try {
			FROM = new InetSocketAddress(InetAddress.getByName("181.167.94.31"), 4500);
			TO = new InetSocketAddress(InetAddress.getLocalHost(), 4500);
			INITIAL_TIMESTAMP = TixCoreUtils.NANOS_OF_DAY.get();
			Thread.sleep(5L);
			RECEPTION_TIMESTAMP = TixCoreUtils.NANOS_OF_DAY.get();
			Thread.sleep(5L);
			SENT_TIMESTAMP = TixCoreUtils.NANOS_OF_DAY.get();
			Thread.sleep(5L);
			FINAL_TIMESTAMP = TixCoreUtils.NANOS_OF_DAY.get();
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	private TixPacket shortPacket;
	private TixPacket longPacket;
	
	@Before
	public void setup() {
		shortPacket = new TixPacket(FROM, TO, TixPacketType.SHORT, INITIAL_TIMESTAMP);
		longPacket = new TixPacket(FROM, TO, TixPacketType.LONG, INITIAL_TIMESTAMP);
	}
	
	@Test
	public void testConstructor() {
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixPacket(null, TO, TixPacketType.SHORT, INITIAL_TIMESTAMP));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixPacket(FROM, null, TixPacketType.SHORT, INITIAL_TIMESTAMP));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixPacket(FROM, TO, null, INITIAL_TIMESTAMP));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixPacket(FROM, TO, TixPacketType.SHORT, -1));
	}
	
	@Test
	public void testConstructorProperties() {
		assertThat(shortPacket.getFrom()).isEqualTo(FROM);
		assertThat(shortPacket.getTo()).isEqualTo(TO);
		assertThat(shortPacket.getInitialTimestamp()).isEqualTo(INITIAL_TIMESTAMP);
		assertThat(shortPacket.getType()).isEqualTo(TixPacketType.SHORT);
		assertThat(shortPacket.getReceptionTimestamp()).isZero();
		assertThat(shortPacket.getSentTimestamp()).isZero();
		assertThat(shortPacket.getFinalTimestamp()).isZero();
		assertThat(longPacket.getType()).isEqualTo(TixPacketType.LONG);
	}

	@Test
	public void testSetAndGetReceptionTimestamp() {
		shortPacket.setReceptionTimestamp(RECEPTION_TIMESTAMP);
		assertThat(shortPacket.getReceptionTimestamp()).isEqualTo(RECEPTION_TIMESTAMP);
	}

	@Test
	public void testSetAndGetSentTimestamp() {
		shortPacket.setSentTimestamp(SENT_TIMESTAMP);
		assertThat(shortPacket.getSentTimestamp()).isEqualTo(SENT_TIMESTAMP);
	}

	@Test
	public void testSetAndGetFinalTimestamp() {
		shortPacket.setFinalTimestamp(FINAL_TIMESTAMP);
		assertThat(shortPacket.getFinalTimestamp()).isEqualTo(FINAL_TIMESTAMP);
	}
	
	@Test
	public void testHashCode() throws UnknownHostException {
		int hashCode = shortPacket.hashCode();
		// Assert that hashcode does not change over time
		assertThat(hashCode).isEqualTo(shortPacket.hashCode());
		// Assert that hashcode doesn't change when the object mutates
		shortPacket.setReceptionTimestamp(RECEPTION_TIMESTAMP);
		assertThat(hashCode).isEqualTo(shortPacket.hashCode());
		shortPacket.setSentTimestamp(SENT_TIMESTAMP);
		assertThat(hashCode).isEqualTo(shortPacket.hashCode());
		shortPacket.setFinalTimestamp(FINAL_TIMESTAMP);
		assertThat(hashCode).isEqualTo(shortPacket.hashCode());
		// Assert that hashcode changes when initial object's values changes
		// Inversion of from - to
		TixPacket other = new TixPacket(TO, FROM, TixPacketType.SHORT, INITIAL_TIMESTAMP);
		assertThat(shortPacket.hashCode()).isNotEqualTo(other.hashCode());
		// Different from/to IPs
		InetSocketAddress otherAddress = new InetSocketAddress(InetAddress.getByName("8.8.8.4"), 4500);
		other = new TixPacket(otherAddress, TO, TixPacketType.SHORT, INITIAL_TIMESTAMP);
		assertThat(shortPacket.hashCode()).isNotEqualTo(other.hashCode());
		other = new TixPacket(FROM, otherAddress, TixPacketType.SHORT, INITIAL_TIMESTAMP);
		assertThat(shortPacket.hashCode()).isNotEqualTo(other.hashCode());
		// Different packet type
		other = new TixPacket(FROM, TO, TixPacketType.LONG, INITIAL_TIMESTAMP);
		assertThat(shortPacket.hashCode()).isNotEqualTo(other.hashCode());
		// Different initial timestamp
		other = new TixPacket(FROM, TO, TixPacketType.SHORT, INITIAL_TIMESTAMP + 1);
		assertThat(shortPacket.hashCode()).isNotEqualTo(other.hashCode());

	}
	
	@Test
	public void testEquals() {
		assertThat(shortPacket).isNotEqualTo(null);
		assertThat(shortPacket).isNotEqualTo(new Object());
		assertThat(shortPacket).isEqualTo(shortPacket);
		assertThat(shortPacket).isNotEqualTo(longPacket);
		TixPacket other = new TixPacket(FROM, TO, TixPacketType.SHORT, INITIAL_TIMESTAMP);
		assertThat(shortPacket).isEqualTo(other);
		other.setReceptionTimestamp(RECEPTION_TIMESTAMP);
		assertThat(shortPacket).isNotEqualTo(other);
		shortPacket.setReceptionTimestamp(RECEPTION_TIMESTAMP);
		assertThat(shortPacket).isEqualTo(other);
		other.setSentTimestamp(SENT_TIMESTAMP);
		assertThat(shortPacket).isNotEqualTo(other);
		shortPacket.setSentTimestamp(SENT_TIMESTAMP);
		assertThat(shortPacket).isEqualTo(other);
		other.setFinalTimestamp(FINAL_TIMESTAMP);
		assertThat(shortPacket).isNotEqualTo(other);
		shortPacket.setFinalTimestamp(FINAL_TIMESTAMP);
		assertThat(shortPacket).isEqualTo(other);
	}
}

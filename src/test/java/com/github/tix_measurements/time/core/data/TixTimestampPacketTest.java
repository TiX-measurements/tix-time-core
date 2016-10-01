package com.github.tix_measurements.time.core.data;

import com.github.tix_measurements.time.core.util.TixTimeUtils;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import static org.assertj.core.api.Assertions.*;

public class TixTimestampPacketTest {
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
	
	private TixTimestampPacket timestampPacket;
	
	@Before
	public void setup() {
		timestampPacket = new TixTimestampPacket(FROM, TO, INITIAL_TIMESTAMP);
	}
	
	@Test
	public void testConstructor() {
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixTimestampPacket(null, TO, INITIAL_TIMESTAMP));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixTimestampPacket(FROM, null, INITIAL_TIMESTAMP));
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TixTimestampPacket(FROM, TO, -1));
	}
	
	@Test
	public void testConstructorProperties() {
		assertThat(timestampPacket.getFrom()).isEqualTo(FROM);
		assertThat(timestampPacket.getTo()).isEqualTo(TO);
		assertThat(timestampPacket.getInitialTimestamp()).isEqualTo(INITIAL_TIMESTAMP);
	}

	@Test
	public void testSetAndGetReceptionTimestamp() {
		timestampPacket.setReceptionTimestamp(RECEPTION_TIMESTAMP);
		assertThat(timestampPacket.getReceptionTimestamp()).isEqualTo(RECEPTION_TIMESTAMP);
	}

	@Test
	public void testSetAndGetSentTimestamp() {
		timestampPacket.setSentTimestamp(SENT_TIMESTAMP);
		assertThat(timestampPacket.getSentTimestamp()).isEqualTo(SENT_TIMESTAMP);
	}

	@Test
	public void testSetAndGetFinalTimestamp() {
		timestampPacket.setFinalTimestamp(FINAL_TIMESTAMP);
		assertThat(timestampPacket.getFinalTimestamp()).isEqualTo(FINAL_TIMESTAMP);
	}
	
	@Test
	public void testHashCode() throws UnknownHostException {
		int hashCode = timestampPacket.hashCode();
		// Assert that hashcode is positive and does not change over time
		assertThat(hashCode).isPositive();
		assertThat(hashCode).isEqualTo(timestampPacket.hashCode());
		// Assert that hashcode doesn't change when the object mutates
		timestampPacket.setReceptionTimestamp(RECEPTION_TIMESTAMP);
		assertThat(hashCode).isEqualTo(timestampPacket.hashCode());
		timestampPacket.setSentTimestamp(SENT_TIMESTAMP);
		assertThat(hashCode).isEqualTo(timestampPacket.hashCode());
		timestampPacket.setFinalTimestamp(FINAL_TIMESTAMP);
		assertThat(hashCode).isEqualTo(timestampPacket.hashCode());
		// Assert that hashcode changes when initial object's values changes
		// Inversion of from - to
		TixTimestampPacket other = new TixTimestampPacket(TO, FROM, INITIAL_TIMESTAMP);
		assertThat(timestampPacket.hashCode()).isNotEqualTo(other.hashCode());
		// Different from/to IPs
		InetSocketAddress otherAddress = new InetSocketAddress(InetAddress.getByName("8.8.8.4"), 4500);
		other = new TixTimestampPacket(otherAddress, TO, INITIAL_TIMESTAMP);
		assertThat(timestampPacket.hashCode()).isNotEqualTo(other.hashCode());
		other = new TixTimestampPacket(FROM, otherAddress, INITIAL_TIMESTAMP);
		assertThat(timestampPacket.hashCode()).isNotEqualTo(other.hashCode());
		// Different initial timestamp
		other = new TixTimestampPacket(FROM, TO, INITIAL_TIMESTAMP + 1);
		assertThat(timestampPacket.hashCode()).isNotEqualTo(other.hashCode());

	}
	
	@Test
	public void testEquals() {
		assertThat(timestampPacket).isNotEqualTo(null);
		assertThat(timestampPacket).isNotEqualTo(new Object());
		assertThat(timestampPacket).isEqualTo(timestampPacket);
		TixTimestampPacket other = new TixTimestampPacket(FROM, TO, INITIAL_TIMESTAMP);
		assertThat(timestampPacket).isEqualTo(other);
		other.setReceptionTimestamp(RECEPTION_TIMESTAMP);
		assertThat(timestampPacket).isNotEqualTo(other);
		timestampPacket.setReceptionTimestamp(RECEPTION_TIMESTAMP);
		assertThat(timestampPacket).isEqualTo(other);
		other.setSentTimestamp(SENT_TIMESTAMP);
		assertThat(timestampPacket).isNotEqualTo(other);
		timestampPacket.setSentTimestamp(SENT_TIMESTAMP);
		assertThat(timestampPacket).isEqualTo(other);
		other.setFinalTimestamp(FINAL_TIMESTAMP);
		assertThat(timestampPacket).isNotEqualTo(other);
		timestampPacket.setFinalTimestamp(FINAL_TIMESTAMP);
		assertThat(timestampPacket).isEqualTo(other);
	}
}

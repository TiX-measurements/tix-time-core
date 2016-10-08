package com.github.tix_measurements.time.core.data;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.net.InetSocketAddress;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Basic TiX Packet. It contains four timestamps that represents the number of nanoseconds since the start of the day in
 * local timezone, and exposes the sender and recipient of the packet with two {@link InetSocketAddress}.
 *
 * It also exposes some variables and lambda functions to allow an easier handling when marshalling into the network.
 */
public class TixTimestampPacket {

	/**
	 * Expected size of the payload this packet will have once it's in the UDP Packet.
	 */
	public static final int TIX_TIMESTAMP_PACKET_SIZE = Long.BYTES * 4;

	/**
	 * Lambda function that reads a timestamp from the {@link ByteBuf}.
	 */
	public static final Function<ByteBuf, Long> TIMESTAMP_READER = ByteBuf::readLong;

	/**
	 * Lambda function that writes a timestamp into a {@link ByteBuf}.
	 */
	public static final BiFunction<ByteBuf, Long, ByteBuf> TIMESTAMP_WRITER = ByteBuf::writeLong;

	/**
	 * {@link InetSocketAddress} expressing the sender of the packet.
	 */
	private final InetSocketAddress from;
	/**
	 * {@link InetSocketAddress} expressing the recipient of the packet.
	 */
	private final InetSocketAddress to;

	/**
	 * {@code long} representing the number of nanoseconds since the start of start of the day at local timezone when the packet was sent from the client.
	 */
	private final long initialTimestamp;

	/**
	 * {@code long} representing the number of nanoseconds since the start of start of the day at local timezone when the packet was received by the server.
	 */
	private long receptionTimestamp;

	/**
	 * {@code long} representing the number of nanoseconds since the start of start of the day at local timezone when the packet was sent to the client.
	 */

	private long sentTimestamp;

	/**
	 * {@code long} representing the number of nanoseconds since the start of start of the day at local timezone when the packet was received by the client.
	 */
	private long finalTimestamp;

	/**
	 * Main constructor of the class {@code TixTimestampPacket}. It creates a packet with the definitions passed in the arguments.
	 *
	 * @param from Sender of the packet.
	 * @param to Recipient of the packet.
	 * @param initialTimestamp {@link #initialTimestamp}
	 */
	public TixTimestampPacket(InetSocketAddress from, InetSocketAddress to, long initialTimestamp) {
		try {
			assertThat(from).isNotNull();
			assertThat(to).isNotNull();
			assertThat(initialTimestamp).isNotNegative();
		} catch (AssertionError ae) {
			throw new IllegalArgumentException(ae);
		}
		this.from = from;
		this.to = to;
		this.initialTimestamp = initialTimestamp;
	}

	/**
	 * Returns {@link #from}.
	 * @return {@link #from}
	 */
	public InetSocketAddress getFrom() {
		return from;
	}

	/**
	 * Returns {@link #to}.
	 * @return {@link #to}
	 */
	public InetSocketAddress getTo() {
		return to;
	}

	/**
	 * Returns {@link #initialTimestamp}.
	 * @return {@link #initialTimestamp}
	 */
	public long getInitialTimestamp() {
		return initialTimestamp;
	}

	/**
	 * Returns {@link #finalTimestamp}.
	 * @return {@link #finalTimestamp}
	 */
	public long getFinalTimestamp() {
		return finalTimestamp;
	}

	/**
	 * Returns {@link #receptionTimestamp}.
	 * @return {@link #receptionTimestamp}
	 */
	public long getReceptionTimestamp() {
		return receptionTimestamp;
	}

	/**
	 * Returns {@link #sentTimestamp}.
	 * @return {@link #sentTimestamp}
	 */
	public long getSentTimestamp() {
		return sentTimestamp;
	}

	/**
	 * Sets {@link #sentTimestamp}.
	 * @param sentTimestamp {@link #finalTimestamp}
	 */
	public void setSentTimestamp(long sentTimestamp) {
		assertThat(sentTimestamp).isNotNegative();
		this.sentTimestamp = sentTimestamp;
	}

	/**
	 * Sets {@link #receptionTimestamp}.
	 * @param receptionTimestamp {@link #finalTimestamp}
	 */
	public void setReceptionTimestamp(long receptionTimestamp) {
		assertThat(receptionTimestamp).isNotNegative();
		this.receptionTimestamp = receptionTimestamp;
	}

	/**
	 * Sets {@link #finalTimestamp}.
	 * @param finalTimestamp {@link #finalTimestamp}
	 */
	public void setFinalTimestamp(long finalTimestamp) {
		assertThat(finalTimestamp).isNotNegative();
		this.finalTimestamp = finalTimestamp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !obj.getClass().equals(this.getClass())) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		TixTimestampPacket other = (TixTimestampPacket) obj;
		return new EqualsBuilder()
				.append(this.getInitialTimestamp(), other.getInitialTimestamp())
				.append(this.getReceptionTimestamp(), other.getReceptionTimestamp())
				.append(this.getSentTimestamp(), other.getSentTimestamp())
				.append(this.getFinalTimestamp(), other.getFinalTimestamp())
				.append(this.getFrom(), other.getFrom())
				.append(this.getTo(), other.getTo())
				.isEquals();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(this.getInitialTimestamp())
				.append(this.getFrom())
				.append(this.getTo())
				.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
				.append("initialTimestamp", this.getInitialTimestamp())
				.append("receptionTimestamp", this.getReceptionTimestamp())
				.append("sentTimestamp", this.getSentTimestamp())
				.append("finalTimestamp", this.getFinalTimestamp())
				.toString();
	}
}

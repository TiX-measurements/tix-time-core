package com.github.tix_measurements.time.core.data;

import com.github.tix_measurements.time.core.util.TixCoreUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.*;

/**
 * The  TiX Data Packet is a long packet type. With the four timestamps that represents the number of nanoseconds since
 * the start of the day in local timezone and the sender and recipient of the packet exposed with two {@link InetSocketAddress},
 * it also contains the public key of the client, the filename that the log file had in the client and the log file
 * content. It also contains the signature of the content with the client's private key, to ensure data correctness and
 * avoid forgery.
 *
 * It also exposes some variables and lambda functions to allow an easier handling when marshalling into the network.
 */
public class TixDataPacket extends TixPacket {

	/**
	 * Constant exposing the header that announces the existence of a data in the payload of the UDP Packet
	 */
	public static final String DATA_HEADER = "DATA";

	/**
	 * Constant exposing the delimiter of the {@link String} values contained in the payload of the UDP Packet
	 */
	public static final String DATA_DELIMITER = ";;";

	/**
	 * User's public key generated by the {@value TixCoreUtils#KEY_ALGORITHM}
	 * algorithm.
	 */
	private byte[] publicKey;

	/**
	 * User's file that contains the last 10 measurements.
	 */
	private byte[] message;

	/**
	 * Signature of the of the log file contents made with the user's private key with the
	 * {@value TixCoreUtils#SIGNING_ALGORITHM} signing algorithm, and verifiable by {@link #publicKey}.
	 */
	private byte[] signature;

	TixDataPacket() {/* Used by Jackson to serialize this packet */ }

	/**
	 * Main constructor of the class {@code TixDataPacket}. It creates a packet with the definitions passed in the arguments.
	 *
	 * @param from Sender of the packet
	 * @param to Recipient of the packet
	 * @param initialTimestamp {@link #initialTimestamp}
	 * @param publicKey {@link #publicKey}
	 * @param message {@link #message}
	 * @param signature {@link #signature}
	 */
	public TixDataPacket(InetSocketAddress from, InetSocketAddress to, long initialTimestamp, byte[] publicKey,
	                     byte[] message, byte[] signature) {
		super(from, to, TixPacketType.LONG, initialTimestamp);
		try {
			assertThat(publicKey).isNotNull();
			assertThat(publicKey).isNotEmpty();
			assertThat(publicKey).hasSize(TixCoreUtils.PUBLCK_KEY_BYTES_LENGTH);
			assertThat(message).isNotNull();
			assertThat(message).isNotEmpty();
			assertThat(signature).isNotNull();
		} catch (AssertionError ae) {
			throw new IllegalArgumentException(ae);
		}
		this.publicKey = publicKey;
		this.signature = signature;
		this.message = message;
	}



	/**
	 * Returns the {@link #publicKey}
	 * @return {@link #publicKey}
	 */
	public byte[] getPublicKey() {
		return publicKey;
	}

	/**
	 * Returns the {@link #signature}
	 * @return {@link #signature}
	 */
	public byte[] getSignature() {
		return signature;
	}

	/**
	 * Returns the {@link #message}
	 * @return {@link #message}
	 */
	public byte[] getMessage() {
		return message;
	}

	/**
	 * Returns {@code true} if this packet's {@link #message} can be verified with the its {@link #signature} by its
	 * {@link #publicKey}. Returns {@code false} otherwise.
	 * @return {@code boolean}
	 */
	public boolean isValid() {
		try {
			return TixCoreUtils.verify(message, publicKey, signature);
		} catch (IllegalArgumentException iae) {
			throw new IllegalStateException(iae);
		}
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
		TixDataPacket other = (TixDataPacket) obj;
		return new EqualsBuilder()
				.appendSuper(super.equals(other))
				.append(this.getPublicKey(), other.getPublicKey())
				.append(this.getSignature(), other.getSignature())
				.append(this.getMessage(), other.getMessage())
				.isEquals();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(super.hashCode())
				.append(this.getPublicKey())
				.append(this.getSignature())
				.append(this.getMessage())
				.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
				.appendSuper(super.toString())
				.append("publicKey", this.getPublicKey())
				.append("signature", this.getSignature())
				.append("message", this.getMessage())
				.toString();
	}
}

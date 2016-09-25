package com.github.tix_measurements.time.core.data;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.net.InetSocketAddress;
import java.util.Base64;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

/**
 * Long TiX Packet Representation. Along with the four timestamps that represents the number of nanoseconds since the start
 * of the day in local timezone and the sender and recipient of the packet exposed with two {@link InetSocketAddress},
 * it also contains the public key of the client, the filename that the log file had in the client and the log file
 * content. It also contains the signature of the content with the client's private key, to ensure data correctness and
 * avoid forgery.
 *
 * It also exposes some variables and lambda functions to allow an easier handling when marshalling into the network.
 */
public class TixDataPacket extends TixTimestampPacket {

	/**
	 * Expected size of the payload this packet will have once it's in the UDP Packet.
	 */
	public static final int TIX_DATA_PACKET_SIZE = TIX_TIMESTAMP_PACKET_SIZE + 4400;

	/**
	 * Lambda function that decodes into a {@link String} from a base 64 encoded {@link String}.
	 */
	public static final Function<String, String> DECODER = (String s) -> new String(Base64.getDecoder().decode(s));

	/**
	 * Lambda function that decodes into a {@code byte[]} from a base 64 encoded {@link String}.
	 */
	public static final Function<String, byte[]> BYTE_DECODER = (String s) -> Base64.getDecoder().decode(s);

	/**
	 * Lambda function that encodes to a base 64 {@link String} a simple {@link String}.
	 */
	public static final Function<String, String> STR_ENCODER = (String s) -> Base64.getEncoder().encodeToString(s.getBytes());

	/**
	 * Lambda function that encodes to a base 64 {@link String} a simple {@code byte[]}.
	 */
	public static final Function<byte[], String> ENCODER = (byte[] bytes) -> Base64.getEncoder().encodeToString(bytes);

	/**
	 * Constant exposing the delimiter of the {@link String} values contained in the payload of the UDP Packet
	 */
	public static final String DATA_DELIMITER = ";;";

	/**
	 * Constant exposing the header that announces the existence of a data in the payload of the UDP Packet
	 */
	public static final String DATA_HEADER = "DATA";

	/**
	 * User's public key
	 */
	private final String publicKey;

	/**
	 * User's log filename that contains the last 10 measurements.
	 */
	private final String filename;

	/**
	 * User's file that contains the last 10 measurements.
	 */
	private final String message;

	/**
	 * Signature of the of the log file contents
	 */
	private final byte[] signature;

	/**
	 * Main constructor of the class {@code TixDataPacket}. It creates a packet with the definitions passed in the arguments.
	 *
	 * @param from Sender of the packet
	 * @param to Recipient of the packet
	 * @param initialTimestamp {@link #initialTimestamp}
	 * @param publicKey {@link #publicKey}
	 * @param filename {@link #filename}
	 * @param message {@link #message}
	 * @param signature {@link #signature}
	 */
	public TixDataPacket(InetSocketAddress from, InetSocketAddress to, long initialTimestamp, String publicKey,
	                     String filename, String message, byte[] signature) {
		super(from, to, initialTimestamp);
		assertThat(publicKey).isNotNull().isNotEmpty();
		assertThat(signature).isNotNull().isNotEmpty();
		assertThat(filename).isNotNull().isNotEmpty();
		assertThat(message).isNotNull().isNotEmpty();
		this.publicKey = publicKey;
		this.signature = signature;
		this.filename = filename;
		this.message = message;
	}

	/**
	 * Returns the {@link #publicKey}
	 * @return {@link #publicKey}
	 */
	public String getPublicKey() {
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
	 * Returns the {@link #filename}
	 * @return {@link #filename}
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Returns the {@link #message}
	 * @return {@link #message}
	 */
	public String getMessage() {
		return message;
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
				.append(this.getFilename(), other.getFilename())
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
				.append(this.getFilename())
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
				.append("filename", this.getFilename())
				.append("message", this.getMessage())
				.toString();
	}
}

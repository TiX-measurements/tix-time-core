package ar.edu.itba.tix.time.core.data;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.net.InetSocketAddress;
import java.util.Base64;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

public class TixDataPackage extends TixTimestampPackage {
	public static final int TIX_DATA_PACKAGE_SIZE = TIX_TIMESTAMP_PACKAGE_SIZE + 4400;
	public static final Function<String, String> DECODER = (String s) -> new String(Base64.getDecoder().decode(s));
	public static final Function<String, byte[]> BYTE_DECODER = (String s) -> Base64.getDecoder().decode(s);
	public static final Function<String, String> STR_ENCODER = (String s) -> Base64.getEncoder().encodeToString(s.getBytes());
	public static final Function<byte[], String> ENCODER = (byte[] bytes) -> Base64.getEncoder().encodeToString(bytes);
	public static final String DATA_DELIMITER = ";;";
	public static final String DATA_HEADER = "DATA";

	private final String publicKey;
	private final String filename;
	private final String message;
	private final byte[] signature;

	public TixDataPackage(InetSocketAddress from, InetSocketAddress to, long initialTimestamp, String publicKey,
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

	public String getPublicKey() {
		return publicKey;
	}

	public byte[] getSignature() {
		return signature;
	}

	public String getFilename() {
		return filename;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !obj.getClass().equals(this.getClass())) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		TixDataPackage other = (TixDataPackage) obj;
		return new EqualsBuilder()
				.appendSuper(super.equals(other))
				.append(this.getPublicKey(), other.getPublicKey())
				.append(this.getSignature(), other.getSignature())
				.append(this.getFilename(), other.getFilename())
				.append(this.getMessage(), other.getMessage())
				.isEquals();
	}

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

package com.github.tix_measurements.time.core.util;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalTime;
import java.util.Base64;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Class that contains all the utilities functions and methods that are used across all the <code>tix-time-core</code> lib project.
 */
public class TixCoreUtils {
	/**
	 * Constant exposing the asymmetric key generation algorithm
	 */
	public static final String KEY_ALGORITHM = "RSA";

	/**
	 * Constant exposing the asymmetric key signing algorithm
	 */
	public static final String SIGNING_ALGORITHM = "SHA1WithRSA";

	/**
	 * Constant exposing the size of the asymmetric key in bits
	 */
	public static final int KEY_PAIR_BITS_LENGTH = 2048;

	/**
	 * Constant exposing the size of the public key in bytes
	 */
	public static final int PUBLCK_KEY_BYTES_LENGTH = 294;

	/**
	 * Constant exposing the size of the signature in bytes
	 */
	public static final int SIGNATURE_BYTES_SIZE = KEY_PAIR_BITS_LENGTH / 8;

	/**
	 * Returns the number of nanoseconds since the start of the day at local time.
	 */
	public static final Supplier<Long> NANOS_OF_DAY = () -> LocalTime.now().toNanoOfDay();

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
	 * Returns a new {@link KeyPair} generated with the {@value #KEY_ALGORITHM} algorithm of {@value #KEY_PAIR_BITS_LENGTH} bits
	 */
	public static final Supplier<KeyPair> NEW_KEY_PAIR = () -> {
		KeyPairGenerator generator = null;
		try {
			generator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		generator.initialize(KEY_PAIR_BITS_LENGTH);
		return generator.genKeyPair();
	};

	/**
	 * Signs a message {@link String} with a {@value #SIGNING_ALGORITHM} algorithm of {@link Signature} using the specified {@link KeyPair}
	 *
	 * @param message {@link String} representing the message to sign
	 * @param keyPair {@link KeyPair} with which to sign the {@code message}
	 * @return {@code byte[]} representing the SHA-1 with RSA signature of the {@code message}
	 */
	public static byte[] sign (byte[] message, KeyPair keyPair) {
		try {
			Signature signer = Signature.getInstance(SIGNING_ALGORITHM);
			signer.initSign(keyPair.getPrivate());
			signer.update(message);
			return signer.sign();
		} catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Verifies a message {@link String} with the supplied bytes of the {@link PublicKey} encoded using {@link PublicKey#getEncoded()} and the signature. The key must be made with the algorithm {@value #KEY_ALGORITHM} and the signature with {@value SIGNING_ALGORITHM}.
	 * @param message {@code byte[]} representing the message to verify
	 * @param encodedPublicKey {@code byte[]} resulting of using {@link PublicKey#getEncoded()} in an {@value #KEY_ALGORITHM} public key.
	 * @param signature {@code byte[]} representing the signature made with {@value #SIGNING_ALGORITHM}
	 * @return {@code true} if the signatures verifies the message with the provided public key, {@code false} otherwise.
	 */
	public static boolean verify(byte[] message, byte[] encodedPublicKey, byte[] signature) {
		try {
			PublicKey publicKey = KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(new X509EncodedKeySpec(encodedPublicKey));
			Signature signer = Signature.getInstance(SIGNING_ALGORITHM);
			signer.initVerify(publicKey);
			signer.update(message);
			return signer.verify(signature);
		} catch (InvalidKeySpecException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			throw new IllegalArgumentException(e);
		}
	}

}

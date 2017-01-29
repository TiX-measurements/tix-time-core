package com.github.tix_measurements.time.core.data;

/**
 * Enum defining the types of Tix Packets available and their sizes.
 */
public enum TixPacketType {
	/**
	 * SHORT Packet types
	 */
	SHORT(Long.BYTES * 4),
	/**
	 * Long Packet types
	 */
	LONG(Long.BYTES * 6 + 4400);

	/**
	 * Expected size in bytes of the payload this packet type will have once it is in the UDP Frame
	 */
	private final int size;

	TixPacketType(int size) {
		this.size = size;
	}

	/**
	 * Returns this Packet Type size in bytes.
	 * @return {@link #size}
	 */
	public int getSize() {
		return size;
	}
}

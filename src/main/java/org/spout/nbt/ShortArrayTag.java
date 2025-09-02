package org.spout.nbt;

import java.util.Arrays;

public class ShortArrayTag extends Tag {
	/**
	 * The value.
	 */
	private final short[] value;

	/**
	 * Creates the tag.
	 * @param name The name.
	 * @param value The value.
	 */
	public ShortArrayTag(String name, short[] value) {
		super(name);
		this.value = value;
	}

	@Override
	public short[] getValue() {
		return value;
	}

	@Override
	public String toString() {
		StringBuilder hex = new StringBuilder();
		for (short s : value) {
			String hexDigits = Integer.toHexString(s).toUpperCase();
			if (hexDigits.length() == 1) {
				hex.append("0");
			}
			hex.append(hexDigits).append(" ");
		}

		String name = getName();
		String append = "";
		if (name != null && !name.equals("")) {
			append = "(\"" + this.getName() + "\")";
		}
		return "TAG_Short_Array" + append + ": " + hex.toString();
	}

	public ShortArrayTag clone() {
		short[] clonedArray = cloneArray(value);

		return new ShortArrayTag(getName(), clonedArray);
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ShortArrayTag)) {
			return false;

		}

		ShortArrayTag tag = (ShortArrayTag) other;
		return Arrays.equals(value, tag.value) &&
				getName().equals(tag.getName());

	}

	private short[] cloneArray(short[] shortArray) {
		if (shortArray == null) {
			return null;
		} else {
			int length = shortArray.length;
			short[] newArray = new short[length];
            System.arraycopy(shortArray, 0, newArray, 0, length);
			return shortArray;
		}
	}
}

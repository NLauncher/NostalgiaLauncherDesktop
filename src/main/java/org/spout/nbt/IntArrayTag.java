package org.spout.nbt;

import java.util.Arrays;

public class IntArrayTag extends Tag {
	/**
	 * The value.
	 */
	private final int[] value;

	/**
	 * Creates the tag.
	 * @param name The name.
	 * @param value The value.
	 */
	public IntArrayTag(String name, int[] value) {
		super(name);
		this.value = value;
	}

	@Override
	public int[] getValue() {
		return value;
	}

	@Override
	public String toString() {
		StringBuilder hex = new StringBuilder();
		for (int s : value) {
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

	public IntArrayTag clone() {
		int[] clonedArray = cloneArray(value);

		return new IntArrayTag(getName(), clonedArray);
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof IntArrayTag)) {
			return false;

		}

		IntArrayTag tag = (IntArrayTag) other;
		return Arrays.equals(value, tag.value) &&
				getName().equals(tag.getName());

	}

	private int[] cloneArray(int[] intArray) {
		if (intArray == null) {
			return null;
		} else {
			int length = intArray.length;
			byte[] newArray = new byte[length];
            System.arraycopy(intArray, 0, newArray, 0, length);
			return intArray;
		}
	}
}

/*
 * This file is part of SpoutNBT (http://www.spout.org/).
 *
 * SpoutNBT is licensed under the SpoutDev License Version 1.
 *
 * SpoutNBT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the SpoutDev License Version 1.
 *
 * SpoutNBT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the SpoutDev License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://www.spout.org/SpoutDevLicenseV1.txt> for the full license,
 * including the MIT license.
 */
package org.spout.nbt.stream;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.spout.nbt.ByteArrayTag;
import org.spout.nbt.ByteTag;
import org.spout.nbt.CompoundTag;
import org.spout.nbt.DoubleTag;
import org.spout.nbt.EndTag;
import org.spout.nbt.FloatTag;
import org.spout.nbt.IntArrayTag;
import org.spout.nbt.IntTag;
import org.spout.nbt.ListTag;
import org.spout.nbt.LongTag;
import org.spout.nbt.NBTConstants;
import org.spout.nbt.NBTUtils;
import org.spout.nbt.ShortArrayTag;
import org.spout.nbt.ShortTag;
import org.spout.nbt.StringTag;
import org.spout.nbt.Tag;

/**
 * This class writes NBT, or Named Binary Tag, {@link Tag} objects to an
 * underlying {@link java.io.OutputStream}.
 * <p />
 * The NBT format was created by Markus Persson, and the specification may be
 * found at <a href="http://www.minecraft.net/docs/NBT.txt">
 * http://www.minecraft.net/docs/NBT.txt</a>.
 * 
 * @author Graham Edgecombe
 */
public final class NBTOutputStream implements Closeable {
	/**
	 * The output stream.
	 */
	private final DataOutputStream os;

	/**
	 * Controls whether this NBTInputStream writes numbers in little-endian format.
	 */
	private final boolean littleEndian;

	/**
	 * Creates a new {@link NBTOutputStream}, which will write data to the
	 * specified underlying output stream. This assumes the output stream should
	 * be compressed with GZIP.
	 * 
	 * @param os The output stream.
	 * @throws java.io.IOException if an I/O error occurs.
	 */
	public NBTOutputStream(OutputStream os) throws IOException {
		this(os, true, false);
	}

	/**
	 * Creates a new {@link NBTOutputStream}, which will write data to the
	 * specified underlying output stream. A flag indicates if the output should
	 * be compressed with GZIP or not.
	 * 
	 * @param os The output stream.
	 * @param boolean A flag that indicates if the output should be compressed.
	 * @throws java.io.IOException if an I/O error occurs.
	 */
	public NBTOutputStream(OutputStream os, boolean compressed) throws IOException {
		this(os, compressed, false);
	}

	/**
	 * Creates a new {@link NBTOutputStream}, which will write data to the
	 * specified underlying output stream. A flag indicates if the output should
	 * be compressed with GZIP or not.
	 * 
	 * @param os The output stream.
	 * @param boolean A flag that indicates if the output should be compressed.
	 * @param littleEndian A flag that indicates if numbers in the output should be output in little-endian format.
	 * @throws java.io.IOException if an I/O error occurs.
	 */
	public NBTOutputStream(OutputStream os, boolean compressed, boolean littleEndian) throws IOException {
		this.littleEndian = littleEndian;
		this.os = new DataOutputStream(compressed ? new GZIPOutputStream(os) : os);
	}

	/**
	 * Writes a tag.
	 * 
	 * @param tag The tag to write.
	 * @throws java.io.IOException if an I/O error occurs.
	 */
	public void writeTag(Tag tag) throws IOException {
		int type = NBTUtils.getTypeCode(tag.getClass());
		String name = tag.getName();
		byte[] nameBytes = name.getBytes(NBTConstants.CHARSET.name());

		os.writeByte(type);
		os.writeShort(littleEndian? Short.reverseBytes((short) nameBytes.length) : nameBytes.length);
		os.write(nameBytes);

		if (type == NBTConstants.TYPE_END) {
			throw new IOException("Named TAG_End not permitted.");
		}

		writeTagPayload(tag);
	}

	/**
	 * Writes tag payload.
	 * 
	 * @param tag The tag.
	 * @throws java.io.IOException if an I/O error occurs.
	 */
	private void writeTagPayload(Tag tag) throws IOException {
		int type = NBTUtils.getTypeCode(tag.getClass());
		switch (type) {
			case NBTConstants.TYPE_END:
				writeEndTagPayload((EndTag) tag);
				break;

			case NBTConstants.TYPE_BYTE:
				writeByteTagPayload((ByteTag) tag);
				break;

			case NBTConstants.TYPE_SHORT:
				writeShortTagPayload((ShortTag) tag);
				break;

			case NBTConstants.TYPE_INT:
				writeIntTagPayload((IntTag) tag);
				break;

			case NBTConstants.TYPE_LONG:
				writeLongTagPayload((LongTag) tag);
				break;

			case NBTConstants.TYPE_FLOAT:
				writeFloatTagPayload((FloatTag) tag);
				break;

			case NBTConstants.TYPE_DOUBLE:
				writeDoubleTagPayload((DoubleTag) tag);
				break;

			case NBTConstants.TYPE_BYTE_ARRAY:
				writeByteArrayTagPayload((ByteArrayTag) tag);
				break;

			case NBTConstants.TYPE_STRING:
				writeStringTagPayload((StringTag) tag);
				break;

			case NBTConstants.TYPE_LIST:
				writeListTagPayload((ListTag<?>) tag);
				break;

			case NBTConstants.TYPE_COMPOUND:
				writeCompoundTagPayload((CompoundTag) tag);
				break;

			case NBTConstants.TYPE_INT_ARRAY:
				writeIntArrayTagPayload((IntArrayTag) tag);
				break;

			case NBTConstants.TYPE_SHORT_ARRAY:
				writeShortArrayTagPayload((ShortArrayTag) tag);
				break;

			default:
				throw new IOException("Invalid tag type: " + type + ".");
		}
	}

	/**
	 * Writes a {@code TAG_Byte} tag.
	 * 
	 * @param tag The tag.
	 * @throws java.io.IOException if an I/O error occurs.
	 */
	private void writeByteTagPayload(ByteTag tag) throws IOException {
		os.writeByte(tag.getValue());
	}

	/**
	 * Writes a {@code TAG_Byte_Array} tag.
	 * 
	 * @param tag The tag.
	 * @throws java.io.IOException if an I/O error occurs.
	 */
	private void writeByteArrayTagPayload(ByteArrayTag tag) throws IOException {
		byte[] bytes = tag.getValue();
		os.writeInt(littleEndian? Integer.reverseBytes(bytes.length) : bytes.length);
		os.write(bytes);
	}

	/**
	 * Writes a {@code TAG_Compound} tag.
	 * 
	 * @param tag The tag.
	 * @throws java.io.IOException if an I/O error occurs.
	 */
	private void writeCompoundTagPayload(CompoundTag tag) throws IOException {
		for (Tag childTag : tag.getValue()) {
			writeTag(childTag);
		}
		os.writeByte((byte) 0); // end tag - better way?
	}

	/**
	 * Writes a {@code TAG_List} tag.
	 * 
	 * @param tag The tag.
	 * @throws java.io.IOException if an I/O error occurs.
	 */
	@SuppressWarnings("unchecked")
	private void writeListTagPayload(ListTag<?> tag) throws IOException {
		Class<? extends Tag> clazz = tag.getType();
		List<Tag> tags = (List<Tag>) tag.getValue();
		int size = tags.size();

		os.writeByte(NBTUtils.getTypeCode(clazz));
		os.writeInt(littleEndian? Integer.reverseBytes(size) : size);
		for (Tag tag1 : tags) {
			writeTagPayload(tag1);
		}
	}

	/**
	 * Writes a {@code TAG_String} tag.
	 * 
	 * @param tag The tag.
	 * @throws java.io.IOException if an I/O error occurs.
	 */
	private void writeStringTagPayload(StringTag tag) throws IOException {
		byte[] bytes = tag.getValue().getBytes(NBTConstants.CHARSET.name());
		os.writeShort(littleEndian? Short.reverseBytes((short) bytes.length) : bytes.length);
		os.write(bytes);
	}

	/**
	 * Writes a {@code TAG_Double} tag.
	 * 
	 * @param tag The tag.
	 * @throws java.io.IOException if an I/O error occurs.
	 */
	private void writeDoubleTagPayload(DoubleTag tag) throws IOException {
		if (littleEndian) {
			os.writeLong(Long.reverseBytes(Double.doubleToLongBits(tag.getValue())));
		} else {
			os.writeDouble(tag.getValue());
		}
	}

	/**
	 * Writes a {@code TAG_Float} tag.
	 * 
	 * @param tag The tag.
	 * @throws java.io.IOException if an I/O error occurs.
	 */
	private void writeFloatTagPayload(FloatTag tag) throws IOException {
		if (littleEndian) {
			os.writeInt(Integer.reverseBytes(Float.floatToIntBits(tag.getValue())));
		} else {
			os.writeFloat(tag.getValue());
		}
	}

	/**
	 * Writes a {@code TAG_Long} tag.
	 * 
	 * @param tag The tag.
	 * @throws java.io.IOException if an I/O error occurs.
	 */
	private void writeLongTagPayload(LongTag tag) throws IOException {
		os.writeLong(littleEndian? Long.reverseBytes(tag.getValue()) : tag.getValue());
	}

	/**
	 * Writes a {@code TAG_Int} tag.
	 * 
	 * @param tag The tag.
	 * @throws java.io.IOException if an I/O error occurs.
	 */
	private void writeIntTagPayload(IntTag tag) throws IOException {
		os.writeInt(littleEndian? Integer.reverseBytes(tag.getValue()) : tag.getValue());
	}

	/**
	 * Writes a {@code TAG_Short} tag.
	 * 
	 * @param tag The tag.
	 * @throws java.io.IOException if an I/O error occurs.
	 */
	private void writeShortTagPayload(ShortTag tag) throws IOException {
		os.writeShort(littleEndian? Short.reverseBytes(tag.getValue()) : tag.getValue());
	}

	/**
	 * Writes a {@code TAG_Int_Array} tag.
	 * 
	 * @param tag The tag.
	 * @throws java.io.IOException if an I/O error occurs.
	 */
	private void writeIntArrayTagPayload(IntArrayTag tag) throws IOException {
		int[] ints = tag.getValue();
		os.writeInt(littleEndian? Integer.reverseBytes(ints.length) : ints.length);
		for(int i = 0; i < ints.length; i++) {
			os.writeInt(littleEndian? Integer.reverseBytes(ints[i]) : ints[i]);
		}
	}

	/**
	 * Writes a {@code TAG_Short_Array} tag.
	 * 
	 * @param tag The tag.
	 * @throws java.io.IOException if an I/O error occurs.
	 */
	private void writeShortArrayTagPayload(ShortArrayTag tag) throws IOException {
		short[] shorts = tag.getValue();
		os.writeInt(littleEndian? Integer.reverseBytes(shorts.length) : shorts.length);
		for(int i = 0; i < shorts.length; i++) {
			os.writeShort(littleEndian? Short.reverseBytes(shorts[i]) : shorts[i]);
		}
	}

	/**
	 * Writes a {@code TAG_Empty} tag.
	 * 
	 * @param tag The tag.
	 * @throws java.io.IOException if an I/O error occurs.
	 */
	private void writeEndTagPayload(EndTag tag) {
		/* empty */
	}

	public void close() throws IOException {
		os.close();
	}

	/**
	 * @return whether this NBTInputStream writes numbers in little-endian format.
	 */
	public boolean isLittleEndian() {
		return littleEndian;
	}
}

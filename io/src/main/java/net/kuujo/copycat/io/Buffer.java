/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kuujo.copycat.io;

/**
 * Navigable byte buffer for input/output operations.
 * <p>
 * The byte buffer provides a fluent interface for reading bytes from and writing bytes to some underlying storage
 * implementation. The {@code Buffer} type is agnostic about the specific underlying storage implementation, but different
 * buffer implementations may be designed for specific storage layers.
 * <p>
 * Aside from the underlying storage implementation, this buffer works very similarly to Java's
 * {@link java.nio.ByteBuffer}. It intentionally exposes methods that can be easily understood by any developer with
 * experience with {@code ByteBuffer}.
 * <p>
 * In order to support reading and writing from buffers, {@code NavigableBuffer} implementations maintain a series of
 * pointers to aid in navigating the buffer.
 * <p>
 * Most notable of these pointers is the {@code position}. When values are written to or read from the buffer, the
 * buffer increments its internal {@code position} according to the number of bytes read. This allows users to iterate
 * through the bytes in the buffer without maintaining external pointers.
 * <p>
 * <pre>
 *   {@code
 *      Buffer buffer = NativeBuffer.allocate(1024);
 *      buffer.writeInt(1);
 *      buffer.flip();
 *      assert buffer.readInt() == 1;
 *   }
 * </pre>
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface Buffer extends BytesInput<Buffer>, BufferInput<Buffer>, BytesOutput<Buffer>, BufferOutput<Buffer>, AutoCloseable {

  /**
   * Returns the buffer's capacity.
   * <p>
   * The capacity represents the total amount of storage space allocated to the buffer by the underlying storage
   * implementation. As such, capacity is defined at the time of the object construction and cannot change.
   *
   * @return The buffer's capacity.
   */
  long capacity();

  /**
   * Returns the buffer's current read/write position.
   * <p>
   * The position is an internal cursor that tracks where to write/read bytes in the underlying storage implementation.
   *
   * @return The buffer's current position.
   */
  long position();

  /**
   * Sets the buffer's current read/write position.
   * <p>
   * The position is an internal cursor that tracks where to write/read bytes in the underlying storage implementation.
   *
   * @param position The position to set.
   * @return This buffer.
   * @throws IllegalArgumentException If the given position is less than {@code 0} or more than {@link Buffer#limit()}
   */
  Buffer position(long position);

  /**
   * Returns tbe buffer's read/write limit.
   * <p>
   * The limit dictates the highest position to which bytes can be read from or written to the buffer. By default, the
   * limit is initialized to {@link Buffer#capacity()}, but it may be explicitly set via
   * {@link Buffer#limit(long)} or {@link Buffer#flip()}.
   *
   * @return The buffer's limit.
   */
  long limit();

  /**
   * Sets the buffer's read/write limit.
   * <p>
   * The limit dictates the highest position to which bytes can be read from or written to the buffer. The limit must
   * be within the bounds of the buffer, i.e. greater than {@code 0} and less than or equal to {@link Buffer#capacity()}
   *
   * @param limit The limit to set.
   * @return This buffer.
   * @throws IllegalArgumentException If the given limit is less than {@code 0} or more than {@link Buffer#capacity()}
   */
  Buffer limit(long limit);

  /**
   * Returns the number of bytes remaining in the buffer until the {@link Buffer#limit()} is reached.
   * <p>
   * The bytes remaining is calculated by {@code buffer.limit() - buffer.position()}
   *
   * @return The number of bytes remaining in the buffer.
   */
  long remaining();

  /**
   * Returns a boolean indicating whether the buffer has bytes remaining.
   * <p>
   * If {@link Buffer#remaining()} is greater than {@code 0} then this method will return {@code true}, otherwise
   * {@code false}
   *
   * @return Indicates whether bytes are remaining in the buffer. {@code true} if {@link Buffer#remaining()} is
   *         greater than {@code 0}, {@code false} otherwise.
   */
  boolean hasRemaining();

  /**
   * Flips the buffer. The limit is set to the current position and then the position is set to zero. If the mark is
   * defined then it is discarded.
   *
   * @return This buffer.
   */
  Buffer flip();

  /**
   * Sets a mark at the current position.
   * <p>
   * The mark is a simple internal reference to the buffer's current position. Marks can be used to reset the buffer
   * to a specific position after some operation.
   * <p>
   * <pre>
   *   {@code
   *   buffer.mark();
   *   buffer.writeInt(1).writeBoolean(true);
   *   buffer.reset();
   *   assert buffer.readInt() == 1;
   *   }
   * </pre>
   *
   * @return This buffer.
   */
  Buffer mark();

  /**
   * Resets the buffer's position to the previously-marked position.
   * <p>
   * Invoking this method neither changes nor discards the mark's value.
   *
   * @return This buffer.
   */
  Buffer reset();

  /**
   * Rewinds the buffer. The position is set to zero and the mark is discarded.
   *
   * @return This buffer.
   */
  Buffer rewind();

  /**
   * Clears the buffer. The position is set to zero, the limit is set to the capacity, and the mark is discarded.
   *
   * @return This buffer.
   */
  Buffer clear();

  /**
   * Returns the bytes underlying the buffer.
   * <p>
   * The buffer is a wrapper around {@link Bytes} that handles writing sequences of bytes by tracking positions and
   * limits. This method returns the {@link Bytes} that this buffer wraps.
   *
   * @return The underlying bytes.
   */
  Bytes bytes();

  /**
   * Returns a new buffer starting at the current position.
   *
   * @return A slice of this buffer.
   */
  Buffer slice();

  /**
   * Reads bytes into the given buffer.
   * <p>
   * Bytes will be read starting at the current buffer position until either {@link Buffer#limit()} has been reached.
   * If {@link Buffer#remaining()} is less than the {@link Buffer#remaining()} of the given buffer, a
   * {@link java.nio.BufferUnderflowException} will be thrown.
   *
   * @param buffer The buffer into which to read bytes.
   * @return The buffer.
   * @throws java.nio.BufferUnderflowException If the given byte array's {@code length} is greater than
   *         {@link Buffer#remaining()}
   */
  Buffer read(Buffer buffer);

  /**
   * Reads bytes into the given byte array.
   * <p>
   * Bytes will be read starting at the current buffer position until either the byte array {@code length} or the
   * {@link Buffer#limit()} has been reached. If {@link Buffer#remaining()}
   * is less than the {@code length} of the given byte array, a {@link java.nio.BufferUnderflowException} will be
   * thrown.
   *
   * @param bytes The byte array into which to read bytes.
   * @return The buffer.
   * @throws java.nio.BufferUnderflowException If the given byte array's {@code length} is greater than
   *         {@link Buffer#remaining()}
   */
  @Override
  Buffer read(Bytes bytes);

  /**
   * Reads bytes into the given byte array.
   * <p>
   * Bytes will be read starting at the current buffer position until either the byte array {@code length} or the
   * {@link Buffer#limit()} has been reached. If {@link Buffer#remaining()}
   * is less than the {@code length} of the given byte array, a {@link java.nio.BufferUnderflowException} will be
   * thrown.
   *
   * @param bytes The byte array into which to read bytes.
   * @return The buffer.
   * @throws java.nio.BufferUnderflowException If the given byte array's {@code length} is greater than
   *         {@link Buffer#remaining()}
   */
  @Override
  Buffer read(byte[] bytes);

  /**
   * Reads bytes into the given byte array starting at the given offset up to the given length.
   * <p>
   * Bytes will be read from the given starting offset up to the given length. If the provided {@code length} is
   * greater than {@link Buffer#remaining()} then a {@link java.nio.BufferUnderflowException} will
   * be thrown. If the {@code offset} is out of bounds of the buffer then an {@link IndexOutOfBoundsException}
   * will be thrown.
   *
   * @param bytes The byte array into which to read bytes.
   * @param offset The offset from which to start reading bytes.
   * @param length The total number of bytes to read.
   * @return The buffer.
   * @throws java.nio.BufferUnderflowException If {@code length} is greater than {@link Buffer#remaining()}
   * @throws IndexOutOfBoundsException If the given offset is out of the bounds of the buffer. Note that
   *         bounds are determined by the buffer's {@link Buffer#limit()} rather than capacity.
   */
  @Override
  Buffer read(byte[] bytes, long offset, long length);

  /**
   * Reads a byte from the buffer at the current position.
   * <p>
   * When the byte is read from the buffer, the buffer's {@code position} will be advanced by {@link Byte#BYTES}. If
   * there are no bytes remaining in the buffer then a {@link java.nio.BufferUnderflowException} will be thrown.
   *
   * @return The read byte.
   * @throws java.nio.BufferUnderflowException If {@link Buffer#remaining()} is less than {@link Byte#BYTES}
   */
  @Override
  int readByte();

  /**
   * Reads a byte from the buffer at the given offset.
   * <p>
   * The byte will be read from the given offset. If the given index is out of the bounds of the buffer then a
   * {@link IndexOutOfBoundsException} will be thrown.
   *
   * @param offset The offset at which to read the byte.
   * @return The read byte.
   * @throws IndexOutOfBoundsException If the given offset is out of the bounds of the buffer. Note that
   *         bounds are determined by the buffer's {@link Buffer#limit()} rather than capacity.
   */
  @Override
  int readByte(long offset);

  /**
   * Reads a 16-bit character from the buffer at the current position.
   * <p>
   * When the character is read from the buffer, the buffer's {@code position} will be advanced by {@link Character#BYTES}.
   * If there are less than {@link Character#BYTES} bytes remaining in the buffer then a
   * {@link java.nio.BufferUnderflowException} will be thrown.
   *
   * @return The read character.
   * @throws java.nio.BufferUnderflowException If {@link Buffer#remaining()} is less than {@link Character#BYTES}
   */
  @Override
  char readChar();

  /**
   * Reads a 16-bit character from the buffer at the given offset.
   * <p>
   * The character will be read from the given offset. If the given index is out of the bounds of the buffer then a
   * {@link IndexOutOfBoundsException} will be thrown.
   *
   * @param offset The offset at which to read the character.
   * @return The read character.
   * @throws IndexOutOfBoundsException If the given offset is out of the bounds of the buffer. Note that
   *         bounds are determined by the buffer's {@link Buffer#limit()} rather than capacity.
   */
  @Override
  char readChar(long offset);

  /**
   * Reads a 16-bit signed integer from the buffer at the current position.
   * <p>
   * When the short is read from the buffer, the buffer's {@code position} will be advanced by {@link Short#BYTES}.
   * If there are less than {@link Short#BYTES} bytes remaining in the buffer then a
   * {@link java.nio.BufferUnderflowException} will be thrown.
   *
   * @return The read short.
   * @throws java.nio.BufferUnderflowException If {@link Buffer#remaining()} is less than {@link Short#BYTES}
   */
  @Override
  short readShort();

  /**
   * Reads a 16-bit signed integer from the buffer at the given offset.
   * <p>
   * The short will be read from the given offset. If the given index is out of the bounds of the buffer then a
   * {@link IndexOutOfBoundsException} will be thrown.
   *
   * @param offset The offset at which to read the short.
   * @return The read short.
   * @throws IndexOutOfBoundsException If the given offset is out of the bounds of the buffer. Note that
   *         bounds are determined by the buffer's {@link Buffer#limit()} rather than capacity.
   */
  @Override
  short readShort(long offset);

  /**
   * Reads a 32-bit signed integer from the buffer at the current position.
   * <p>
   * When the integer is read from the buffer, the buffer's {@code position} will be advanced by {@link Integer#BYTES}.
   * If there are less than {@link Integer#BYTES} bytes remaining in the buffer then a
   * {@link java.nio.BufferUnderflowException} will be thrown.
   *
   * @return The read integer.
   * @throws java.nio.BufferUnderflowException If {@link Buffer#remaining()} is less than {@link Integer#BYTES}
   */
  @Override
  int readInt();

  /**
   * Reads a 32-bit signed integer from the buffer at the given offset.
   * <p>
   * The integer will be read from the given offset. If the given index is out of the bounds of the buffer then a
   * {@link IndexOutOfBoundsException} will be thrown.
   *
   * @param offset The offset at which to read the integer.
   * @return The read integer.
   * @throws IndexOutOfBoundsException If the given offset is out of the bounds of the buffer. Note that
   *         bounds are determined by the buffer's {@link Buffer#limit()} rather than capacity.
   */
  @Override
  int readInt(long offset);

  /**
   * Reads a 64-bit signed integer from the buffer at the current position.
   * <p>
   * When the long is read from the buffer, the buffer's {@code position} will be advanced by {@link Long#BYTES}.
   * If there are less than {@link Long#BYTES} bytes remaining in the buffer then a
   * {@link java.nio.BufferUnderflowException} will be thrown.
   *
   * @return The read long.
   * @throws java.nio.BufferUnderflowException If {@link Buffer#remaining()} is less than {@link Long#BYTES}
   */
  @Override
  long readLong();

  /**
   * Reads a 64-bit signed integer from the buffer at the given offset.
   * <p>
   * The long will be read from the given offset. If the given index is out of the bounds of the buffer then a
   * {@link IndexOutOfBoundsException} will be thrown.
   *
   * @param offset The offset at which to read the long.
   * @return The read long.
   * @throws IndexOutOfBoundsException If the given offset is out of the bounds of the buffer. Note that
   *         bounds are determined by the buffer's {@link Buffer#limit()} rather than capacity.
   */
  @Override
  long readLong(long offset);

  /**
   * Reads a single-precision 32-bit floating point number from the buffer at the current position.
   * <p>
   * When the float is read from the buffer, the buffer's {@code position} will be advanced by {@link Float#BYTES}.
   * If there are less than {@link Float#BYTES} bytes remaining in the buffer then a
   * {@link java.nio.BufferUnderflowException} will be thrown.
   *
   * @return The read float.
   * @throws java.nio.BufferUnderflowException If {@link Buffer#remaining()} is less than {@link Float#BYTES}
   */
  @Override
  float readFloat();

  /**
   * Reads a single-precision 32-bit floating point number from the buffer at the given offset.
   * <p>
   * The float will be read from the given offset. If the given index is out of the bounds of the buffer then a
   * {@link IndexOutOfBoundsException} will be thrown.
   *
   * @param offset The offset at which to read the float.
   * @return The read float.
   * @throws IndexOutOfBoundsException If the given offset is out of the bounds of the buffer. Note that
   *         bounds are determined by the buffer's {@link Buffer#limit()} rather than capacity.
   */
  @Override
  float readFloat(long offset);

  /**
   * Reads a double-precision 64-bit floating point number from the buffer at the current position.
   * <p>
   * When the double is read from the buffer, the buffer's {@code position} will be advanced by {@link Double#BYTES}.
   * If there are less than {@link Double#BYTES} bytes remaining in the buffer then a
   * {@link java.nio.BufferUnderflowException} will be thrown.
   *
   * @return The read double.
   * @throws java.nio.BufferUnderflowException If {@link Buffer#remaining()} is less than {@link Double#BYTES}
   */
  @Override
  double readDouble();

  /**
   * Reads a double-precision 64-bit floating point number from the buffer at the given offset.
   * <p>
   * The double will be read from the given offset. If the given index is out of the bounds of the buffer then a
   * {@link IndexOutOfBoundsException} will be thrown.
   *
   * @param offset The offset at which to read the double.
   * @return The read double.
   * @throws IndexOutOfBoundsException If the given offset is out of the bounds of the buffer. Note that
   *         bounds are determined by the buffer's {@link Buffer#limit()} rather than capacity.
   */
  @Override
  double readDouble(long offset);

  /**
   * Reads a 1 byte boolean from the buffer at the current position.
   * <p>
   * When the short is read from the buffer, the buffer's {@code position} will be advanced by {@code 1}.
   * If there are no bytes remaining in the buffer then a {@link java.nio.BufferUnderflowException} will be thrown.
   *
   * @return The read boolean.
   * @throws java.nio.BufferUnderflowException If {@link Buffer#remaining()} is less than {@code 1}
   */
  @Override
  boolean readBoolean();

  /**
   * Reads a 1 byte boolean from the buffer at the given offset.
   * <p>
   * The boolean will be read from the given offset. If the given index is out of the bounds of the buffer then a
   * {@link IndexOutOfBoundsException} will be thrown.
   *
   * @param offset The offset at which to read the boolean.
   * @return The read boolean.
   * @throws IndexOutOfBoundsException If the given offset is out of the bounds of the buffer. Note that
   *         bounds are determined by the buffer's {@link Buffer#limit()} rather than capacity.
   */
  @Override
  boolean readBoolean(long offset);

  /**
   * Writes a buffer to the buffer.
   * <p>
   * When the buffer is written to the buffer, the buffer's {@code position} will be advanced by the number of bytes
   * in the provided buffer. If the provided {@link Buffer#remaining()} exceeds {@link Buffer#remaining()} then an
   * {@link java.nio.BufferOverflowException} will be thrown.
   *
   * @param buffer The array of bytes to write.
   * @return The written buffer.
   * @throws java.nio.BufferOverflowException If the number of bytes exceeds the buffer's remaining bytes.
   */
  Buffer write(Buffer buffer);

  /**
   * Writes an array of bytes to the buffer.
   * <p>
   * When the bytes are written to the buffer, the buffer's {@code position} will be advanced by the number of bytes
   * in the provided byte array. If the number of bytes exceeds {@link Buffer#limit()} then an
   * {@link java.nio.BufferOverflowException} will be thrown.
   *
   * @param bytes The array of bytes to write.
   * @return The written buffer.
   * @throws java.nio.BufferOverflowException If the number of bytes exceeds the buffer's remaining bytes.
   */
  @Override
  Buffer write(Bytes bytes);

  /**
   * Writes an array of bytes to the buffer.
   * <p>
   * When the bytes are written to the buffer, the buffer's {@code position} will be advanced by the number of bytes
   * in the provided byte array. If the number of bytes exceeds {@link Buffer#limit()} then an
   * {@link java.nio.BufferOverflowException} will be thrown.
   *
   * @param bytes The array of bytes to write.
   * @return The written buffer.
   * @throws java.nio.BufferOverflowException If the number of bytes exceeds the buffer's remaining bytes.
   */
  @Override
  Buffer write(byte[] bytes);

  /**
   * Writes an array of bytes to the buffer.
   * <p>
   * The bytes will be written starting at the given offset up to the given length. If the length of the byte array
   * is larger than the provided {@code length} then only {@code length} bytes will be read from the array. If the
   * provided {@code length} is greater than the remaining bytes in this buffer then a {@link java.nio.BufferOverflowException}
   * will be thrown.
   *
   * @param bytes The array of bytes to write.
   * @param offset The offset at which to start writing the bytes.
   * @param length The number of bytes from the provided byte array to write to the buffer.
   * @return The written buffer.
   * @throws java.nio.BufferOverflowException If there are not enough bytes remaining in the buffer.
   * @throws IndexOutOfBoundsException If the given offset is out of the bounds of the buffer.
   */
  @Override
  Buffer write(byte[] bytes, long offset, long length);

  /**
   * Writes a byte to the buffer at the current position.
   * <p>
   * When the byte is written to the buffer, the buffer's {@code position} will be advanced by {@link Byte#BYTES}. If
   * there are no bytes remaining in the buffer then a {@link java.nio.BufferOverflowException} will be thrown.
   *
   * @param b The byte to write.
   * @return The written buffer.
   * @throws java.nio.BufferOverflowException If there are no bytes remaining in the buffer.
   */
  @Override
  Buffer writeByte(int b);

  /**
   * Writes a byte to the buffer at the given offset.
   * <p>
   * The byte will be written at the given offset. If there are no bytes remaining in the buffer then a
   * {@link java.nio.BufferOverflowException} will be thrown.
   *
   * @param offset The offset at which to write the byte.
   * @param b The byte to write.
   * @return The written buffer.
   * @throws java.nio.BufferOverflowException If there are not enough bytes remaining in the buffer.
   * @throws IndexOutOfBoundsException If the given offset is out of the bounds of the buffer. Note that
   *         bounds are determined by the buffer's {@link Buffer#limit()} rather than capacity.
   */
  @Override
  Buffer writeByte(long offset, int b);

  /**
   * Writes a 16-bit character to the buffer at the current position.
   * <p>
   * When the character is written to the buffer, the buffer's {@code position} will be advanced by
   * {@link Character#BYTES}. If less than {@code 2} bytes are remaining in the buffer then a
   * {@link java.nio.BufferOverflowException} will be thrown.
   *
   * @param c The character to write.
   * @return The written buffer.
   * @throws java.nio.BufferOverflowException If {@link Buffer#remaining()} is less than {@link Character#BYTES}.
   */
  @Override
  Buffer writeChar(char c);

  /**
   * Writes a 16-bit character to the buffer at the given offset.
   * <p>
   * The character will be written at the given offset. If there are less than {@link Character#BYTES} bytes remaining
   * in the buffer then a {@link java.nio.BufferOverflowException} will be thrown.
   *
   * @param offset The offset at which to write the character.
   * @param c The character to write.
   * @return The written buffer.
   * @throws java.nio.BufferOverflowException If {@link Buffer#remaining()} is less than {@link Character#BYTES}.
   * @throws IndexOutOfBoundsException If the given offset is out of the bounds of the buffer. Note that
   *         bounds are determined by the buffer's {@link Buffer#limit()} rather than capacity.
   */
  @Override
  Buffer writeChar(long offset, char c);

  /**
   * Writes a 16-bit signed integer to the buffer at the current position.
   * <p>
   * When the short is written to the buffer, the buffer's {@code position} will be advanced by {@link Short#BYTES}. If
   * less than {@link Short#BYTES} bytes are remaining in the buffer then a {@link java.nio.BufferOverflowException}
   * will be thrown.
   *
   * @param s The short to write.
   * @return The written buffer.
   * @throws java.nio.BufferOverflowException If {@link Buffer#remaining()} is less than {@link Short#BYTES}.
   */
  @Override
  Buffer writeShort(short s);

  /**
   * Writes a 16-bit signed integer to the buffer at the given offset.
   * <p>
   * The short will be written at the given offset. If there are less than {@link Short#BYTES} bytes remaining in the buffer
   * then a {@link java.nio.BufferOverflowException} will be thrown.
   *
   * @param offset The offset at which to write the short.
   * @param s The short to write.
   * @return The written buffer.
   * @throws java.nio.BufferOverflowException If {@link Buffer#remaining()} is less than {@link Short#BYTES}.
   * @throws IndexOutOfBoundsException If the given offset is out of the bounds of the buffer. Note that
   *         bounds are determined by the buffer's {@link Buffer#limit()} rather than capacity.
   */
  @Override
  Buffer writeShort(long offset, short s);

  /**
   * Writes a 32-bit signed integer to the buffer at the current position.
   * <p>
   * When the integer is written to the buffer, the buffer's {@code position} will be advanced by {@link Integer#BYTES}.
   * If less than {@link Integer#BYTES} bytes are remaining in the buffer then a {@link java.nio.BufferOverflowException}
   * will be thrown.
   *
   * @param i The integer to write.
   * @return The written buffer.
   * @throws java.nio.BufferOverflowException If {@link Buffer#remaining()} is less than {@link Integer#BYTES}.
   */
  @Override
  Buffer writeInt(int i);

  /**
   * Writes a 32-bit signed integer to the buffer at the given offset.
   * <p>
   * The integer will be written at the given offset. If there are less than {@link Integer#BYTES} bytes remaining
   * in the buffer then a {@link java.nio.BufferOverflowException} will be thrown.
   *
   * @param offset The offset at which to write the integer.
   * @param i The integer to write.
   * @return The written buffer.
   * @throws java.nio.BufferOverflowException If {@link Buffer#remaining()} is less than {@link Integer#BYTES}.
   * @throws IndexOutOfBoundsException If the given offset is out of the bounds of the buffer. Note that
   *         bounds are determined by the buffer's {@link Buffer#limit()} rather than capacity.
   */
  @Override
  Buffer writeInt(long offset, int i);

  /**
   * Writes a 64-bit signed integer to the buffer at the current position.
   * <p>
   * When the long is written to the buffer, the buffer's {@code position} will be advanced by {@link Long#BYTES}.
   * If less than {@link Long#BYTES} bytes are remaining in the buffer then a {@link java.nio.BufferOverflowException}
   * will be thrown.
   *
   * @param l The long to write.
   * @return The written buffer.
   * @throws java.nio.BufferOverflowException If {@link Buffer#remaining()} is less than {@link Long#BYTES}.
   */
  @Override
  Buffer writeLong(long l);

  /**
   * Writes a 64-bit signed integer to the buffer at the given offset.
   * <p>
   * The long will be written at the given offset. If there are less than {@link Long#BYTES} bytes remaining
   * in the buffer then a {@link java.nio.BufferOverflowException} will be thrown.
   *
   * @param offset The offset at which to write the long.
   * @param l The long to write.
   * @return The written buffer.
   * @throws java.nio.BufferOverflowException If {@link Buffer#remaining()} is less than {@link Long#BYTES}.
   * @throws IndexOutOfBoundsException If the given offset is out of the bounds of the buffer. Note that
   *         bounds are determined by the buffer's {@link Buffer#limit()} rather than capacity.
   */
  @Override
  Buffer writeLong(long offset, long l);

  /**
   * Writes a single-precision 32-bit floating point number to the buffer at the current position.
   * <p>
   * When the float is written to the buffer, the buffer's {@code position} will be advanced by {@link Float#BYTES}.
   * If less than {@link Float#BYTES} bytes are remaining in the buffer then a {@link java.nio.BufferOverflowException}
   * will be thrown.
   *
   * @param f The float to write.
   * @return The written buffer.
   * @throws java.nio.BufferOverflowException If {@link Buffer#remaining()} is less than {@link Float#BYTES}.
   */
  @Override
  Buffer writeFloat(float f);

  /**
   * Writes a single-precision 32-bit floating point number to the buffer at the given offset.
   * <p>
   * The float will be written at the given offset. If there are less than {@link Float#BYTES} bytes remaining
   * in the buffer then a {@link java.nio.BufferOverflowException} will be thrown.
   *
   * @param offset The offset at which to write the float.
   * @param f The float to write.
   * @return The written buffer.
   * @throws java.nio.BufferOverflowException If {@link Buffer#remaining()} is less than {@link Float#BYTES}.
   * @throws IndexOutOfBoundsException If the given offset is out of the bounds of the buffer. Note that
   *         bounds are determined by the buffer's {@link Buffer#limit()} rather than capacity.
   */
  @Override
  Buffer writeFloat(long offset, float f);

  /**
   * Writes a double-precision 64-bit floating point number to the buffer at the current position.
   * <p>
   * When the double is written to the buffer, the buffer's {@code position} will be advanced by {@link Double#BYTES}.
   * If less than {@link Double#BYTES} bytes are remaining in the buffer then a {@link java.nio.BufferOverflowException}
   * will be thrown.
   *
   * @param d The double to write.
   * @return The written buffer.
   * @throws java.nio.BufferOverflowException If {@link Buffer#remaining()} is less than {@link Double#BYTES}.
   */
  @Override
  Buffer writeDouble(double d);

  /**
   * Writes a double-precision 64-bit floating point number to the buffer at the given offset.
   * <p>
   * The double will be written at the given offset. If there are less than {@link Double#BYTES} bytes remaining
   * in the buffer then a {@link java.nio.BufferOverflowException} will be thrown.
   *
   * @param offset The offset at which to write the double.
   * @param d The double to write.
   * @return The written buffer.
   * @throws java.nio.BufferOverflowException If {@link Buffer#remaining()} is less than {@link Double#BYTES}.
   * @throws IndexOutOfBoundsException If the given offset is out of the bounds of the buffer. Note that
   *         bounds are determined by the buffer's {@link Buffer#limit()} rather than capacity.
   */
  @Override
  Buffer writeDouble(long offset, double d);

  /**
   * Writes a 1 byte boolean to the buffer at the current position.
   * <p>
   * When the boolean is written to the buffer, the buffer's {@code position} will be advanced by {@code 1}.
   * If there are no bytes remaining in the buffer then a {@link java.nio.BufferOverflowException}
   * will be thrown.
   *
   * @param b The boolean to write.
   * @return The written buffer.
   * @throws java.nio.BufferOverflowException If the number of bytes exceeds the buffer's remaining bytes.
   */
  @Override
  Buffer writeBoolean(boolean b);

  /**
   * Writes a 1 byte boolean to the buffer at the given offset.
   * <p>
   * The boolean will be written as a single byte at the given offset. If there are no bytes remaining in the buffer
   * then a {@link java.nio.BufferOverflowException} will be thrown.
   *
   * @param offset The offset at which to write the boolean.
   * @param b The boolean to write.
   * @return The written buffer.
   * @throws java.nio.BufferOverflowException If {@link Buffer#remaining()} is less than {@code 1}.
   * @throws IndexOutOfBoundsException If the given offset is out of the bounds of the buffer. Note that
   *         bounds are determined by the buffer's {@link Buffer#limit()} rather than capacity.
   */
  @Override
  Buffer writeBoolean(long offset, boolean b);

}

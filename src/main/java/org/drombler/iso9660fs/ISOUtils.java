/*
 *         COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Notice
 *
 * The contents of this file are subject to the COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL)
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/cddl1.txt
 *
 * The Original Code is Drombler.org. The Initial Developer of the
 * Original Code is Florian Brunner (Sourceforge.net user: puce).
 * Copyright 2014 Drombler.org. All Rights Reserved.
 *
 * Contributor(s): .
 */
package org.drombler.iso9660fs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;

/**
 *
 * @author puce
 */
public final class ISOUtils {

    private static final short MAX_UNSIGNED_BYTE = 0xFF;
//    private static final long MAX_UNSIGNED_INT32 = 0xFFFFFFFF;
//    private static final int NUM_LONG_BITS = 8;
//    private static final int NUM_INTEGER_BYTES = 4;
//    private static final int NUM_SHORT_BITS = 2;

    static void getStringDTrimmed(ByteBuffer byteBuffer, String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private ISOUtils() {
    }

    private static String getStringA(ByteBuffer byteBuffer, int length) {
        byte[] dst = getBytes(byteBuffer, length);
        return new String(dst);
    }

    public static byte[] getBytes(ByteBuffer byteBuffer, int length) {
        byte[] dst = new byte[length];
        byteBuffer.get(dst);
        return dst;
    }

    public static String getStringATrimmed(ByteBuffer byteBuffer, int length) {
        return getStringA(byteBuffer, length).trim();
    }

    private static String getStringD(ByteBuffer byteBuffer, int length) {
        return getStringA(byteBuffer, length);
    }

    public static String getStringDTrimmed(ByteBuffer byteBuffer, int length) {
        return getStringD(byteBuffer, length).trim();
    }

    public static int getUnsignedInt16LSBMSB(ByteBuffer byteBuffer) {
//        byte[] bytes = getBytes(byteBuffer, 2 * NUM_SHORT_BITS);
//        int value = 0;
//        for (int i = 0; i < NUM_SHORT_BITS; i++) {
//            value += getUnsignedByte(bytes[i]) << (NUM_INTEGER_BYTES * i);
//        }
//        return value;
        int unsignedInt16LSB = getUnsignedInt16LSB(byteBuffer);
        int unsignedInt16MSB = getUnsignedInt16MSB(byteBuffer);
        if (unsignedInt16LSB != unsignedInt16MSB) {
            throw new IllegalArgumentException("unsignedInt16LSB (was: " + unsignedInt16LSB
                    + ") must be equal to unsignedInt16MSB (but was: " + unsignedInt16MSB + ")");
        }
        return unsignedInt16LSB;
    }

    public static long getUnsignedInt32LSBMSB(ByteBuffer byteBuffer) {
//        byte[] bytes = getBytes(byteBuffer, 2 * NUM_INTEGER_BYTES);
//        long value = 0;
//        for (int i = 0; i < NUM_INTEGER_BYTES; i++) {
//            value += getUnsignedByte(bytes[i]) << (NUM_LONG_BITS * i);
//        }
//        return value;
        long unsignedInt32LSB = getUnsignedInt32LSB(byteBuffer);
        long unsignedInt32MSB = getUnsignedInt32MSB(byteBuffer);
        if (unsignedInt32LSB != unsignedInt32MSB) {
            throw new IllegalArgumentException("unsignedInt16LSB (was: " + unsignedInt32LSB
                    + ") must be equal to unsignedInt16MSB (but was: " + unsignedInt32MSB + ")");
        }
        return unsignedInt32LSB;
    }

    public static int getUnsignedInt16LSB(ByteBuffer byteBuffer) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        short shortValue = byteBuffer.getShort();
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        return Short.toUnsignedInt(shortValue);
    }

    public static int getUnsignedInt16MSB(ByteBuffer byteBuffer) {
        short shortValue = byteBuffer.getShort();
        return Short.toUnsignedInt(shortValue);
    }

    public static long getUnsignedInt32LSB(ByteBuffer byteBuffer) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        int intValue = byteBuffer.getInt();
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        return Integer.toUnsignedLong(intValue);
    }

    public static long getUnsignedInt32MSB(ByteBuffer byteBuffer) {
        int intValue = byteBuffer.getInt();
        return Integer.toUnsignedLong(intValue);
    }

    public static short getUnsignedByte(ByteBuffer byteBuffer) {
//        return (short) (value & MAX_UNSIGNED_BYTE);
        return (short) Byte.toUnsignedInt(byteBuffer.get());
    }

    public static ZonedDateTime getDecDateTime(ByteBuffer byteBuffer) {
        int year = Integer.parseInt(getStringD(byteBuffer, 4));
        int month = Integer.parseInt(getStringD(byteBuffer, 2));
        int day = Integer.parseInt(getStringD(byteBuffer, 2));
        int hour = Integer.parseInt(getStringD(byteBuffer, 2));
        int minute = Integer.parseInt(getStringD(byteBuffer, 2));
        int second = Integer.parseInt(getStringD(byteBuffer, 2));
        int centiSecond = Integer.parseInt(getStringD(byteBuffer, 2));
        byte quarterHourZoneOffset = byteBuffer.get();
        return month != 0 ? ZonedDateTime.of(year, month, day, hour, minute, second, centiSecond * 10000000, getZoneOffset(quarterHourZoneOffset)) : null;
    }

    public static ZonedDateTime getDirectoryDateTime(ByteBuffer byteBuffer) {
        byte year = byteBuffer.get();
        byte month = byteBuffer.get();
        byte day = byteBuffer.get();
        byte hour = byteBuffer.get();
        byte minute = byteBuffer.get();
        byte second = byteBuffer.get();
        byte quarterHourZoneOffset = byteBuffer.get();
        return month != 0 ? ZonedDateTime.of(1900 + year, month, day, hour, minute, second, 0, getZoneOffset(quarterHourZoneOffset)) : null;
    }

    private static ZoneOffset getZoneOffset(byte quarterHourZoneOffset) {
        return ZoneOffset.ofTotalSeconds(quarterHourZoneOffset * 15 * 60);
    }

    public static void readUnused(ByteBuffer byteBuffer, int length) {
        byte[] dst = new byte[length];
        byteBuffer.get(dst);

        for (byte unused : dst) {
            if (unused != 0) {
                throw new IllegalArgumentException("0s expected but was: " + Arrays.toString(dst));
            }
        }
    }

    public static boolean isEven(int intValue) {
        return (intValue) % 2 == 0;
    }

    public static boolean isOdd(int intValue) {
        return !isEven(intValue);
    }

    public static ByteBuffer createByteBuffer(SeekableByteChannel byteChannel, long location, long dataLength, int logicalBlockSize) throws IOException {
        long newPosition = location * logicalBlockSize;
        byteChannel.position(newPosition);
        ByteBuffer byteBuffer = ByteBuffer.allocate((int) dataLength);
        final int numBytes = byteChannel.read(byteBuffer);
        if (numBytes != dataLength) {
            throw new IOException("Too few data to read: " + numBytes);
        }
        byteBuffer.position(0);
        return byteBuffer;
    }

}

/*
 *         COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Notice
 *
 * The contents of this file are subject to the COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL)
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/cddl1.txt
 *
 * The Original Code is provided by Drombler GmbH. The Initial Developer of the
 * Original Code is Florian Brunner (GitHub user: puce77).
 * Copyright 2020 Drombler GmbH. All Rights Reserved.
 *
 * Contributor(s): .
 */
package org.drombler.iso9660fs;

import java.nio.ByteBuffer;

/**
 * @author puce
 */
public enum ISOEncodingType {
    LSB {
        @Override
        public int getUnsignedInt16(ByteBuffer byteBuffer) {
            return ISOUtils.getUnsignedInt16LSB(byteBuffer);
        }

        @Override
        public long getUnsignedInt32(ByteBuffer byteBuffer) {
            return ISOUtils.getUnsignedInt32LSB(byteBuffer);
        }
    },

    MSB {
        @Override
        public int getUnsignedInt16(ByteBuffer byteBuffer) {
            return ISOUtils.getUnsignedInt16MSB(byteBuffer);
        }

        @Override
        public long getUnsignedInt32(ByteBuffer byteBuffer) {
            return ISOUtils.getUnsignedInt32MSB(byteBuffer);
        }
    };

    public abstract int getUnsignedInt16(ByteBuffer byteBuffer);

    public abstract long getUnsignedInt32(ByteBuffer byteBuffer);
}

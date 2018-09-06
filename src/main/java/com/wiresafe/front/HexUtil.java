package com.wiresafe.front;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class HexUtil {

    public static String encode(String value) {
        return Hex.encodeHexString(value.getBytes(StandardCharsets.UTF_8), true);
    }

    public static String decode(String value) {
        if (Objects.isNull(value)) {
            return null;
        }

        try {
            return new String(Hex.decodeHex(value), StandardCharsets.UTF_8);
        } catch (DecoderException e) {
            throw new IllegalArgumentException(e);
        }
    }

}

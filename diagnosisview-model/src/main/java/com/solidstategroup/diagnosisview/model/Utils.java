package com.solidstategroup.diagnosisview.model;

import lombok.extern.java.Log;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Helper utilities used by DTOs and tests.
 */
@Log
public final class Utils {
    /**
     * Hide from instantiation.
     */
    private Utils() { }

    /**
     * Get a BigDecimal from a String.
     * @param value String value to convert
     * @return BigDecimal converted from String
     */
    public static BigDecimal getBigDecimal(final String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return new BigDecimal(value);
    }

    /**
     * Get an Integer from a String, returning null if error.
     * @param value String value to convert
     * @return Integer converted from String
     */
    public static Integer getInteger(final String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }

        try {
            return new Integer(value);
        } catch (NumberFormatException nfe) {
            log.severe("Error converting String, setting to null, continuing");
            return null;
        }
    }

    /**
     * Get a salt for a password.
     *
     * @return The string of the salt
     * @throws NoSuchAlgorithmException when salt cannot be generated
     */
    public static String generateSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);

        return toHex(salt);
    }

    /**
     * Checks whether given plaintext password corresponds
     * to a stored salted hash of the password.
     *
     * @param password       the entered password to check
     * @param storedSalt     the stored user salt
     * @param storedPassword the stored user password
     * @return Boolean whether the password matches
     * @throws Exception when hashing errors
     */
    public static boolean checkPassword(final String password, final String storedSalt, final String storedPassword)
            throws Exception {
        return storedPassword.equals(
                DigestUtils.sha256Hex(password + storedSalt));
    }

    /**
     * Converts a byte array into a hexadecimal string.
     *
     * @param array the byte array to convert
     * @return a length*2 character string encoding the byte array
     */
    private static String toHex(final byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        } else {
            return hex;
        }
    }
}

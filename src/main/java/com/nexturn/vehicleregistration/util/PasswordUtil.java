package com.nexturn.vehicleregistration.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import com.nexturn.vehicleregistration.exception.PasswordHashException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hashes and verifies passwords using PBKDF2-HMAC-SHA256.
 * Preserves the existing Base64 salt:hash format and iteration count.
 */
public final class PasswordUtil {
  /**
   * Produces a salted hash compatible with existing account passwords.
   * @param value password to hash
   * @return Base64 salt and hash separated by a colon
   * @throws PasswordHashException when password derivation fails
   */
  public static String hash(String value) {
    byte[] salt = new byte[16];
    new SecureRandom().nextBytes(salt);
    return Base64.getEncoder().encodeToString(salt)
        + ":"
        + Base64.getEncoder().encodeToString(derive(value, salt));
  }

  private static byte[] derive(String value, byte[] salt) {
    try {
      return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
          .generateSecret(new PBEKeySpec(value.toCharArray(), salt, 210000, 256))
          .getEncoded();
    } catch (Exception e) {
      throw new PasswordHashException(e);
    }
  }

  /**
   * Verifies a password without comparing derived bytes as ordinary strings.
   * @param value submitted password
   * @param stored existing salted password hash
   * @return true for a matching password, false for a mismatch or invalid stored value
   */
  public static boolean matches(String value, String stored) {
    try {
      String[] parts = stored.split(":");
      return MessageDigest.isEqual(
          derive(value, Base64.getDecoder().decode(parts[0])),
          Base64.getDecoder().decode(parts[1]));
    } catch (Exception e) {
      return false;
    }
  }
}

package pairro.network.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;

public final class Codec {
  public static String toBase64(byte[] bytes) {
    return Base64.getEncoder().encodeToString(bytes);
  }

  public static String toBase64(String text) {
    return toBase64(text.getBytes(StandardCharsets.UTF_8));
  }

  public static byte[] fromBase64(String value) {
    return Base64.getDecoder().decode(value);
  }

  public static String textFromBase64(String value) {
    return new String(fromBase64(value), StandardCharsets.UTF_8);
  }

  public static byte[] sha256(byte[] data) {
    try {
      return MessageDigest.getInstance("SHA-256").digest(data);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is unavailable", e);
    }
  }

  public static byte[] sha256(String text) {
    return sha256(text.getBytes(StandardCharsets.UTF_8));
  }

  public static String sha256Hex(byte[] data) {
    return HexFormat.of().formatHex(sha256(data));
  }
}

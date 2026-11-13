package pairro.network.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class AuthHandshake {
  public static final String ROLE_SERVER = "server";
  public static final String ROLE_CLIENT = "client";

  private static final String CONTEXT = "auth-v1";
  private static final String HMAC_ALGORITHM = "HmacSHA256";
  private static final int NONCE_BYTES = 32;
  private static final SecureRandom RANDOM = new SecureRandom();

  private AuthHandshake() {}

  public static String newNonce() {
    byte[] nonce = new byte[NONCE_BYTES];
    RANDOM.nextBytes(nonce);

    return Codec.toBase64(nonce);
  }

  public static boolean isValidNonce(String nonce) {
    if (nonce == null) {
      return false;
    }

    try {
      return Codec.fromBase64(nonce).length == NONCE_BYTES;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static String proof(String token, String role, PendingAuth auth) {
    try {
      Mac mac = Mac.getInstance(HMAC_ALGORITHM);
      mac.init(new SecretKeySpec(token.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));

      String transcript = String.join("|",
          CONTEXT,
          role,
          auth.deviceId(),
          auth.clientNonce(),
          auth.serverNonce(),
          auth.certificateFingerprint());

      return Codec.toBase64(mac.doFinal(transcript.getBytes(StandardCharsets.UTF_8)));
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Could not compute authentication proof", e);
    }
  }

  public static boolean matches(String expected, String actual) {
    if (expected == null || actual == null) {
      return false;
    }

    return MessageDigest.isEqual(
        expected.getBytes(StandardCharsets.UTF_8),
        actual.getBytes(StandardCharsets.UTF_8));
  }
}

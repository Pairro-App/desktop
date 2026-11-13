package pairro.network.security;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.KeyAgreement;

public class CryptoUtils {

  public static KeyPair generateKeyPair() {
    try {
      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");

      keyPairGenerator.initialize(new ECGenParameterSpec("secp256r1"));

      return keyPairGenerator.generateKeyPair();
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Could not generate key pair", e);
    }
  }

  public static byte[] deriveKeyFromToken(String token) {
    return Codec.sha256(token);
  }

  public static PublicKey decodePublicKey(String base64Key) {
    try {
      byte[] keyBytes = Codec.fromBase64(base64Key);

      return KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(keyBytes));
    } catch (GeneralSecurityException | IllegalArgumentException e) {
      return null;
    }
  }

  public static String encodePublicKey(PublicKey publicKey) {
    return Codec.toBase64(publicKey.getEncoded());
  }

  public static String fingerprint(Certificate certificate) {
    try {
      return Codec.sha256Hex(certificate.getEncoded());
    } catch (GeneralSecurityException e) {
      return null;
    }
  }

  public static String calculatePin(PrivateKey localPrivateKey, PublicKey remotePublicKey) {
    try {
      KeyAgreement agreement = KeyAgreement.getInstance("ECDH");
      agreement.init(localPrivateKey);
      agreement.doPhase(remotePublicKey, true);

      byte[] hash = Codec.sha256(agreement.generateSecret());

      int positiveHash = ByteBuffer.wrap(hash).getInt() & 0x7FFFFFFF;
      int pinNumber = positiveHash % 1000000;

      return String.format("%06d", pinNumber);
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Could not compute pairing PIN", e);
    }
  }
}

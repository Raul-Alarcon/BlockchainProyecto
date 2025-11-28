/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;

/**
 *
 * @author PC
 */
public class Encriptar {
    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    // ====================== LLAVES ======================

    public static PublicKey cargarLlavePublica(String rutaPem) throws Exception {
        try (Reader reader = new FileReader(rutaPem);
             PEMParser pemParser = new PEMParser(reader)) {
            Object obj = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

            if (obj instanceof org.bouncycastle.asn1.x509.SubjectPublicKeyInfo) {
                return converter.getPublicKey((org.bouncycastle.asn1.x509.SubjectPublicKeyInfo) obj);
            } else {
                throw new IllegalArgumentException("Formato de llave pública no soportado");
            }
        }
    }

    public static PrivateKey cargarLlavePrivada(String rutaPem, char[] password) throws Exception {
        try (Reader reader = new InputStreamReader(new FileInputStream(new File(rutaPem)));
             PEMParser pemParser = new PEMParser(reader)) {

            Object obj = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

            if (obj instanceof PEMKeyPair) {
                KeyPair kp = converter.getKeyPair((PEMKeyPair) obj);
                return kp.getPrivate();
            } else if (obj instanceof PKCS8EncryptedPrivateKeyInfo) {
                PKCS8EncryptedPrivateKeyInfo encryptedInfo = (PKCS8EncryptedPrivateKeyInfo) obj;
                InputDecryptorProvider decryptor = new JceOpenSSLPKCS8DecryptorProviderBuilder().build(password);
                PrivateKeyInfo keyInfo = encryptedInfo.decryptPrivateKeyInfo(decryptor);
                return converter.getPrivateKey(keyInfo);
            } else if (obj instanceof PrivateKeyInfo) {
                return converter.getPrivateKey((PrivateKeyInfo) obj);
            } else {
                throw new IllegalArgumentException("Formato de llave privada no soportado");
            }
        }
    }

    public static char[] pedirContrasenia(String mensaje) {
        JPasswordField campo = new JPasswordField();
        int r = JOptionPane.showConfirmDialog(null, campo, mensaje, JOptionPane.OK_CANCEL_OPTION);
        return (r == JOptionPane.OK_OPTION) ? campo.getPassword() : new char[0];
    }

    // ====================== AES ======================

    public static SecretKey generarClaveAES(int bits) throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(bits);
        return kg.generateKey();
    }

    public static IvParameterSpec generarIV() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public static String cifrarAES(String mensaje, SecretKey key, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cifrado = cipher.doFinal(mensaje.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(cifrado);
    }

    public static String descifrarAES(String cifrado, SecretKey key, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] bytes = Base64.getDecoder().decode(cifrado);
        byte[] descifrado = cipher.doFinal(bytes);
        return new String(descifrado, StandardCharsets.UTF_8);
    }

    // ====================== RSA ======================

    public static byte[] cifrarRSA(byte[] datos, PublicKey pub) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, pub);
        return cipher.doFinal(datos);
    }

    public static byte[] descifrarRSA(byte[] datos, PrivateKey priv) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, priv);
        return cipher.doFinal(datos);
    }

    // ====================== HÍBRIDO JSON ======================

    public static String cifrarJSON(String json, PublicKey pub) throws Exception {
        SecretKey claveAES = generarClaveAES(256);
        IvParameterSpec iv = generarIV();

        // AES
        String datosCifrados = cifrarAES(json, claveAES, iv);

        // RSA para clave AES
        String claveCifrada = Base64.getEncoder().encodeToString(cifrarRSA(claveAES.getEncoded(), pub));

        // IV
        String ivBase64 = Base64.getEncoder().encodeToString(iv.getIV());

        // Formato: claveRSA;iv;base64(AES)
        return claveCifrada + ";" + ivBase64 + ";" + datosCifrados;
    }

    public static String descifrarJSON(String cifradoCompleto, PrivateKey priv) throws Exception {
        String[] partes = cifradoCompleto.split(";", 3);
        if (partes.length != 3) throw new IllegalArgumentException("Formato de datos incorrecto");

        // Clave AES
        byte[] claveAESBytes = descifrarRSA(Base64.getDecoder().decode(partes[0]), priv);
        SecretKey claveAES = new SecretKeySpec(claveAESBytes, "AES");

        // IV
        byte[] ivBytes = Base64.getDecoder().decode(partes[1]);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        // Datos
        return descifrarAES(partes[2], claveAES, iv);
    }
}

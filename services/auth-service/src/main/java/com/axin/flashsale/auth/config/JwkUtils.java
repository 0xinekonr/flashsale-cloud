package com.axin.flashsale.auth.config;

import com.nimbusds.jose.jwk.RSAKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

public class JwkUtils {

    private static final Logger log = LoggerFactory.getLogger(JwkUtils.class);
    private static final Path KEY_DIR = Paths.get(System.getProperty("user.home"), ".flashsale");
    private static final Path PUBLIC_KEY_FILE = KEY_DIR.resolve("rsa-public.key");
    private static final Path PRIVATE_KEY_FILE = KEY_DIR.resolve("rsa-private.key");
    private static final Path KEY_ID_FILE = KEY_DIR.resolve("rsa-kid.txt");

    public static RSAKey generateRSA() {
        try {
            if (Files.exists(PUBLIC_KEY_FILE) && Files.exists(PRIVATE_KEY_FILE) && Files.exists(KEY_ID_FILE)) {
                log.info("从文件加载已有 RSA 密钥对: {}", KEY_DIR);
                return loadFromFile();
            }
        } catch (Exception e) {
            log.warn("加载 RSA 密钥对失败, 将重新生成", e);
        }

        log.info("首次启动, 生成 RSA 密钥对并持久化到: {}", KEY_DIR);
        return generateAndPersist();
    }

    private static RSAKey loadFromFile() throws Exception {
        KeyFactory kf = KeyFactory.getInstance("RSA");

        byte[] pubBytes = Base64.getDecoder().decode(Files.readString(PUBLIC_KEY_FILE).trim());
        RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(pubBytes));

        byte[] privBytes = Base64.getDecoder().decode(Files.readString(PRIVATE_KEY_FILE).trim());
        RSAPrivateKey privateKey = (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(privBytes));

        String kid = Files.readString(KEY_ID_FILE).trim();

        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(kid)
                .build();
    }

    private static RSAKey generateAndPersist() {
        KeyPair keyPair = generateRSAKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        String kid = UUID.randomUUID().toString();

        try {
            Files.createDirectories(KEY_DIR);
            Files.writeString(PUBLIC_KEY_FILE, Base64.getEncoder().encodeToString(publicKey.getEncoded()));
            Files.writeString(PRIVATE_KEY_FILE, Base64.getEncoder().encodeToString(privateKey.getEncoded()));
            Files.writeString(KEY_ID_FILE, kid);
            log.info("RSA 密钥对已持久化到: {}", KEY_DIR);
        } catch (IOException e) {
            log.error("持久化 RSA 密钥对失败", e);
        }

        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(kid)
                .build();
    }

    private static KeyPair generateRSAKey() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}

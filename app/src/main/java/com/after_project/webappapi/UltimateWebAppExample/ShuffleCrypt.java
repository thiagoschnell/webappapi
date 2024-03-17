package com.after_project.webappapi;
// Copyright (c) Thiago Schnell | https://github.com/thiagoschnell/webappapi/blob/main/LICENSE
// Licensed under the MIT License.
//version 2.3
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
public class ShuffleCrypt {
    private static final String CIPHER_ALGORITHM = "BLOWFISH/CBC/NoPadding";
    private static final int KEY_LENGTH = 448; //448-bits
    private static final int KEY_SIZE = 42;// up to 56 (KEY_LENGTH / 8)
    private static final int BLOCK_SIZE = 8;// or cipher.getBlockSize()
    private int TIME_UNITS = String.valueOf(System.currentTimeMillis()).length();
    private Cipher cipher;
    private SecretKeySpec keySpec;
    private IvParameterSpec ivspec;
    private final SecureRandom random = new SecureRandom();
    ShuffleCrypt(String password) throws Exception {
        createCipher(getKey(password.getBytes(StandardCharsets.UTF_8)),getIV(null));
    }
    ShuffleCrypt(String password, String ivparam) throws Exception {
        createCipher(getKey(password.getBytes(StandardCharsets.UTF_8)), getIV(ivparam.getBytes(StandardCharsets.UTF_8)));
    }
    ShuffleCrypt(int randomPasswordLength) throws Exception {
        createCipher(getRandomKey(randomPasswordLength),getIV(null));
    }
    ShuffleCrypt(int randomPasswordLength, String ivparam) throws Exception {
        createCipher(getRandomKey(randomPasswordLength),getIV(ivparam.getBytes(StandardCharsets.UTF_8)));
    }
    private void createCipher(SecretKeySpec key, IvParameterSpec iv) throws Exception {
        cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        keySpec = key;
        ivspec = iv;
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivspec);
    }
    private byte[] decrypt(byte[] bytes) throws Exception{
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivspec);
        return cipher.doFinal(bytes);
    }
    protected String decryptFromBase64String(String str) throws Exception {
        byte[] bytes = fromBase64(str);
        return new String(decrypt(bytes), StandardCharsets.UTF_8);
    }
    protected String decryptFromHexString(String str) throws Exception {
        byte[] bytes = hexToBytes(str);
        return new String(decrypt(bytes), StandardCharsets.UTF_8);
    }
    private byte[] encrypt(String text) throws Exception {
        if(CIPHER_ALGORITHM.contains("/NoPadding")){
            text = padString(text);
        }
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivspec);
        return cipher.doFinal(text.getBytes("UTF-8"));
    }
    protected String encryptStringToBase64(String encryptString) throws Exception {
        return toBase64(encrypt(encryptString));
    }
    protected String encryptStringToHex(String encryptString) throws Exception {
        return bytesToHex(encrypt(encryptString));
    }
    private IvParameterSpec getIV(byte[] iv){
        if(iv==null) {
            iv = TIME_UNITS == 13 ? getUniqueIV(BLOCK_SIZE) : getRandomIV(BLOCK_SIZE);
        }
        return new IvParameterSpec(iv);
    }
    private byte[] getUniqueIV(int length) {
        byte[] iv = new byte[length];
        long time = System.currentTimeMillis();
        for (int i = 0; i < 8; i++) {
            iv[i] = (byte) (time >> (i * 8));
        }
        return iv;
    }
    private byte[] getRandomBytes(int length){
        byte[] b = new byte[length];
        random.nextBytes(b);
        return b;
    }
    private byte[] getRandomIV(int length) {
        return getRandomBytes(length);
    }
    private CropResult cropBytes(final byte[] b, final int x, final int y){
        final int p1 = x;
        final int p2 = y;
        {
            byte[] kb = new byte[b.length];
            System.arraycopy(b, 0, kb, 0, kb.length);
            byte[] kb1 = new byte[p1];
            System.arraycopy(b, 0, kb1, 0, p1);
            byte[] kb2 = new byte[p2];
            System.arraycopy(b, p1, kb2, 0, p2);
            byte[] kb3 = new byte[b.length - (p1+p2)];
            System.arraycopy(b, p1+p2, kb3, 0,  b.length - (p1+p2));
            byte[] kbfinal =  new byte[b.length-(p2)];
            System.arraycopy(b, 0, kbfinal, 0, p1);
            System.arraycopy(b, p1+p2, kbfinal, p1, b.length - (p1+p2));
            return new CropResult(b,kb2,kbfinal);
        }
    }
    //cropFixed:  to sync local and remote(x,y), the get the configuration and the X is the fixed position into remaining bytes, y is the cropsize, ex: cropBytes(bytes,x=0,y=16) this will run two times to shuffle back 32bytes
    private byte[] shufflePasswordKey(byte[] bytes) throws Exception{
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        digest.update(bytes);
        final byte[] b = digest.digest();
        byte[] bresult = new byte[KEY_SIZE]; //hash the password and shuffle the bytes back here
        int blength = 0;
        int count = 0;
        CropResult cropResult = null;
        int total_count = 3;
        while(count < total_count)
        {
            cropResult = cropBytes(cropResult==null?b:cropResult.remaining,randomInt(0,22),14); //64  - (14 * 3) is your minHigh random =22. 64 is the digest.getDigestLength(). key_size is (14*3)
            System.arraycopy(cropResult.cropped, 0, bresult, blength, cropResult.cropped.length);
            blength = blength + cropResult.cropped.length;
            count++;
        }
        return bresult;
    }
    private SecretKeySpec getKey(byte[] password) throws Exception {
        byte[] shuffledPassword = shufflePasswordKey(password);
        return new SecretKeySpec(shuffledPassword, "AES");
    }
    private SecretKeySpec getRandomKey(int length) throws Exception {
        byte[] randomBytes = getRandomBytes(length);
        return getKey(randomBytes);
    }
    private String padString(String source) {
        char paddingChar = ' ';
        int size = 16;
        int x = source.length() % size;
        int padLength = size - x;
        for (int i = 0; i < padLength; i++) {
            source += paddingChar;
        }
        return source;
    }
    private String bytesToHex(byte[] data) {
        if (data == null) {
            return null;
        }
        int len = data.length;
        String str = "";
        for (int i = 0; i < len; i++) {
            if ((data[i] & 0xFF) < 16)
                str = str + "0" + Integer.toHexString(data[i] & 0xFF);
            else
                str = str + Integer.toHexString(data[i] & 0xFF);
        }
        return str;
    }
    private byte[] hexToBytes(String str) {
        if (str == null) {
            return null;
        } else if (str.length() < 2) {
            return null;
        } else {
            int len = str.length() / 2;
            byte[] buffer = new byte[len];
            for (int i = 0; i < len; i++) {
                buffer[i] = (byte) Integer.parseInt(str.substring(i * 2, i * 2 + 2), 16);
            }
            return buffer;
        }
    }
    private byte[] fromBase64(String base64) {
        return Base64.decode(base64, Base64.NO_WRAP);
    }
    private  String toBase64(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }
    private class CropResult{
        private byte[] source = null;
        private byte[] cropped = null;
        private byte[] remaining = null;
        CropResult(final byte[] source, final byte[] cropped, final byte[] remain){
            this.source = source;
            this.cropped = cropped;
            this.remaining = remain;
        }
    }
    private int randomInt(final int min, final int max){
        final int random = new Random().nextInt((max - min) + 1) + min;
        return random;
    }
}

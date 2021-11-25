package com.prophet.common;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * 加密算法基础工具类
 *
 */
public abstract class Encryptor {
	public static final String KEY_SHA = "SHA";  
    public static final String KEY_MD5 = "MD5";  
  
    /** 
     * MAC算法可选以下多种算法 
     *  
     * HmacMD5  
     * HmacSHA1  
     * HmacSHA256  
     * HmacSHA384  
     * HmacSHA512 
     */  
    public static final String KEY_MAC = "HmacMD5";  
  
    /** 
     * MD5加密 
     *  
     * @param data 
     * @return 
     * @throws Exception 
     */  
    public static String encryptMD5(String data){  
        MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance(KEY_MD5);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}  
        md5.update(data.getBytes());  
        BigInteger bi = new BigInteger(md5.digest());
        return bi.toString(16);  
    }  
  
    /** 
     * SHA加密 
     *  
     * @param data 
     * @return 
     * @throws Exception 
     */  
    public static String encryptSHA(String data){
        MessageDigest sha = null;
		try {
			sha = MessageDigest.getInstance(KEY_SHA);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}  
        sha.update(data.getBytes());  
        BigInteger bi = new BigInteger(sha.digest());
        return bi.toString(32);
    } 
}

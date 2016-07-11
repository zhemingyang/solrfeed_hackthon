package bfd.sys.solr.util;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class HashUtil {
	
	private static final int BASE_NUM = 3*4*5;
	
	private static final int MULTIPLE = 2*2;
	
	private static final int HASH_MAX = BASE_NUM * MULTIPLE;
	
	private static final HashFunction HF = Hashing.md5();
	
	public static int hash(String key){
		return Math.abs(HF.hashString(key).asInt())%HASH_MAX;
	}
	

}
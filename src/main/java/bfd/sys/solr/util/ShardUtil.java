package bfd.sys.solr.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServer;

import bfd.sys.solr.Shard;

public class ShardUtil {
	
	public static Map<String,Shard[]> shards = new HashMap<String, Shard[]>();
	
	public static Map<String,Range[]> ranges = new HashMap<String, Range[]>();
	
	public static Map<Shard,Set<SolrServer>> solrs = new HashMap<Shard, Set<SolrServer>>();
	
	public static Shard locateShard(String cid,int hashCode){
		Range[] r = ranges.get(cid);
		return shards.get(cid)[Arrays.binarySearch(r, new Range(hashCode,hashCode))];
	}
	
	public static Set<SolrServer> locateSolrs(Shard shard){
		return solrs.get(shard);
	}
	
	public static Set<SolrServer> locateSolrs(String cid,int hashCode){
		return solrs.get(locateShard(cid, hashCode));
	}

}


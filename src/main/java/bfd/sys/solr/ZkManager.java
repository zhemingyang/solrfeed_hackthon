package bfd.sys.solr;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

import bfd.sys.solr.util.Configuration;
import bfd.sys.solr.util.Range;
import bfd.sys.solr.util.ShardUtil;

public class ZkManager {
	
	public static ZooKeeper zookeeper;
	private static String zkAddress;
	private static String zk;
	private static Map<String,SolrInfo> map = new HashMap<String, SolrInfo>(); 
	
	private static Logger logger = Logger.getLogger(ZkManager.class);
	
	public static void init(Configuration config) {
		zkAddress = config.getString("zkAddress");
		zk = config.getString("zk");
		// 建立连接
		try {
			zookeeper = new ZooKeeper(zkAddress, 1000, new Watcher() {
				
				public void process(WatchedEvent arg0) {
					System.out.println("Event:"+arg0.getType());
				}
				
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static boolean getConfig() throws JSONException{
		try {
			List<String> cids = zookeeper.getChildren(zk, false);
			return refreshConfig(cids);
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 预置zk上所有的meta信息到集合对象Map<String,SolrInfo>中（ ZkManager.map）
	 * @param cids
	 * @return
	 * @throws JSONException
	 */
	public static boolean refreshConfig(Collection<String> cids) throws JSONException{
		try {
			for (String cid : cids) {
				JSONObject temp;
				String data = new String(zookeeper.getData(zk + "/" + cid, false, null));
				if(data!=null||(data = data.trim()).length()>0){
					JSONObject json = JSONObject.fromObject(data);
					int state = json.getInt("state");
					SolrInfo clientInfo = new SolrInfo();
					clientInfo.setState(state);
					if(json.has("shards")){
						JSONArray shards = json.getJSONArray("shards");
						for(int i=0;i<shards.size();i++){
							Shard s = new Shard();
							String shard = shards.getString(i);
							s.setName(shard);
							data = new String(zookeeper.getData(zk + "/" + cid + "/" +shard, false, null));
							JSONObject info = JSONObject.fromObject(data);
							s.setState(info.getInt("state"));
							s.setBhash(info.getInt("bhash"));
							s.setEhash(info.getInt("ehash"));
							s.setNo(info.getInt("no"));
							SolrNode master = new SolrNode();
							SolrNode slave = new SolrNode();
							try {
								data = new String(zookeeper.getData(zk + "/" + cid + "/" +shard + "/master" , false, null));
								temp = JSONObject.fromObject(data);
								master.setState(temp.getInt("state"));
								master.setData(temp.getString("nodes"));
							} catch (Exception e) {
								e.printStackTrace();
							}
								
							try {
								data = new String(zookeeper.getData(zk + "/" + cid + "/" +shard + "/slave" , false, null));
								temp = JSONObject.fromObject(data);
								slave.setState(temp.getInt("state"));
								slave.setData(temp.getString("nodes"));
							} catch (Exception e) {
								e.printStackTrace();
							}
							s.setMaster(master);
							s.setSlave(slave);
							clientInfo.getShards().put(shard,s);
						}
					}
					if(json.has("fields")){
						JSONArray fields = json.getJSONArray("fields");
						for(int i=0;i<fields.size();i++){
							SolrField f = new SolrField(fields.getJSONObject(i));
							clientInfo.getFields().put(f.getName(),f);
						}
					}
					if(json.has("attrs")){
						JSONArray fields = json.getJSONArray("attrs");
						for(int i=0;i<fields.size();i++){
							SolrField f = new SolrField(fields.getJSONObject(i));
							clientInfo.getAttrs().put(f.getName(),f);
						}
					}
					map.put(cid, clientInfo);
					initShardHash(cid);
				}
			}
			return true;
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 预置分片策略：hash区间（Range）、分片meta信息（Shard）、每个
	 * shard的Set<SolrServer>三者映射关系建立
	 * @param cid
	 * @return
	 */
	static boolean initShardHash(String cid) {
		if(map.get(cid)!=null){
			SolrInfo info = map.get(cid);
			Map<Integer, Shard> sortedShards = new TreeMap<Integer, Shard>();
			for(String shard:info.getShards().keySet()){
				Shard s = info.getShards().get(shard);
				sortedShards.put(s.getBhash(), s);
			}
			int size = info.getShards().size(),i=0;
			Range[] ranges = new Range[size]; 
			Shard[] sArray = new Shard[size];
			for(Shard shard:sortedShards.values()){
				ranges[i] = new Range(shard.getBhash(), shard.getEhash());
				sArray[i++] = shard;
				updateShardSolrs(shard);
			}
			ShardUtil.ranges.put(cid, ranges);
			ShardUtil.shards.put(cid, sArray);
			return true;
		}
		return false;
	}
	
	public static boolean updateField(String cid,String name,String value){
		if(map.containsKey(cid)){
			try {
				String data = new String(zookeeper.getData(zk+"/"+cid, false, null));
				JSONObject json = JSONObject.fromObject(data);
				if(data!=null){
					if(json.has("fields")){
						JSONArray fields = json.getJSONArray("fields");
						int index = fields.size();
						for(int i=0;i<fields.size();i++){
							if(fields.getJSONObject(i).getString("name").equals(name)){
								index = i;
								break ;
							}
						}
						fields.set(index, value);
					}else{
						JSONArray fields = new JSONArray();
						fields.add(value);
						json.put("fields", fields);
					}
					map.get(cid).getFields().put(name, new SolrField(JSONObject.fromObject(value)));
				}
				zookeeper.setData(zk+"/"+cid, json.toString().getBytes(), -1);
				return true;
			} catch (KeeperException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public static boolean updateFields(String cid,String value){
		if(map.containsKey(cid)){
			try {
				String data = new String(zookeeper.getData(zk+"/"+cid, false, null));
				JSONObject json = JSONObject.fromObject(data);
				if(data!=null){
					JSONArray fields = JSONArray.fromObject(value);
					JSONArray cf = new JSONArray();
					for(int i=0;i<fields.size();i++){
						JSONObject field = fields.getJSONObject(i);
						cf.add(field.getJSONObject("value"));
						map.get(cid).getFields().put(field.getString("name"), new SolrField(field.getJSONObject("value")));
					}
					json.put("fields", cf);
				}
				zookeeper.setData(zk+"/"+cid, json.toString().getBytes(), -1);
				return true;
			} catch (KeeperException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public static boolean updateShardInfo(String cid,Shard shard){
		if(map.containsKey(cid)&&map.get(cid).getShards().containsKey(shard.getName())&&shard!=null){
			try {
				zookeeper.setData(zk+"/"+cid+"/"+shard.getName(),shard.getNodeString().getBytes(), -1);
				Shard current = map.get(cid).getShards().get(shard.getName());
				current.setBhash(shard.getBhash());
				current.setEhash(shard.getEhash());
				current.setState(shard.getState());
				current.setNo(shard.getNo());
				return true;
			} catch (KeeperException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public static boolean addShardInfo(String cid,Shard shard){
		if(map.containsKey(cid)&&!map.get(cid).getShards().containsKey(shard.getName())&&shard!=null){
			try {
				zookeeper.create(zk+"/"+cid+"/"+shard.getName(), shard.getNodeString().getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				String data = new String(zookeeper.getData(zk+"/"+cid, false, null));
				JSONObject temp = JSONObject.fromObject(data);
				JSONArray shards = temp.getJSONArray("shards");
				if(!shards.contains(shard.getName())){
					shards.add(shard.getName());
				}
				zookeeper.setData(zk+"/"+cid, temp.toString().getBytes(), -1);
				map.get(cid).getShards().put(shard.getName(), shard);
				return true;
			} catch (KeeperException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public static boolean addShardMaster(String cid,Shard shard){
		if(addShardNode(cid, shard.getName(), shard.getMaster(), true)){
			updateShardSolrs(shard);
			return true;
		}
		return false;
	}

	/**
	 * 实例化每个master下servers的HttpSolrServer
	 * 路径：/SolrNew/ns3/Cxicihutong/shard1/master/(nodes/server)
	 * @param shard
	 */
	private static void updateShardSolrs(Shard shard) {
		SolrNode node = shard.getMaster();
		if(node !=null && node.getData()!=null){
			JSONArray solrs = JSONArray.fromObject(node.getData());
			Set<SolrServer> servers = new HashSet<SolrServer>();
			for (int i = 0; i < solrs.size(); i++) {
				servers.add(new HttpSolrServer(solrs.getJSONObject(i).getString("url")));
			}
			ShardUtil.solrs.put(shard, servers);
		}
	}
	
	public static boolean addShardSlave(String cid,Shard shard){
		return addShardNode(cid, shard.getName(), shard.getSlave(), false);
	}
	
	private static boolean addShardNode(String cid,String shard,SolrNode node,boolean isMaster){
		if(map.containsKey(cid)&&map.get(cid).getShards().containsKey(shard)&&node!=null){
			try {
				zookeeper.create(zk+"/"+cid+"/"+shard+(isMaster?"/master":"/slave"), node.toString().getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				if(isMaster){
					map.get(cid).getShards().get(shard).setMaster(node);
				}else{
					map.get(cid).getShards().get(shard).setSlave(node);
				}
				return true;
			} catch (KeeperException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public static boolean updateShardMaster(String cid,Shard shard){
		if(updateShardNode(cid, shard.getName(), shard.getMaster(), true)){
			updateShardSolrs(shard);
			return true;
		}
		return false;
	}
	
	public static boolean updateShardSlave(String cid,Shard shard){
		return updateShardNode(cid, shard.getName(), shard.getSlave(), false);
	}
	
	private static boolean updateShardNode(String cid,String shard,SolrNode node,boolean isMaster){
		if(map.containsKey(cid)&&map.get(cid).getShards().containsKey(shard)&&node!=null){
			try {
				zookeeper.setData(zk+"/"+cid+"/"+shard+(isMaster?"/master":"/slave"), node.toString().getBytes(), -1);
				if(isMaster){
					map.get(cid).getShards().get(shard).setMaster(node);
				}else{
					map.get(cid).getShards().get(shard).setSlave(node);
				}
				return true;
			} catch (KeeperException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public static SolrInfo getInfo(String cid){
		return map.get(cid);
	}
	
	public static void dumpConfig(){
		System.out.println(JSONArray.fromObject(map).toString());
	}
	
}
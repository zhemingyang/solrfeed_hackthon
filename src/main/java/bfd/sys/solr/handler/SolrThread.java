package bfd.sys.solr.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

import bfd.sys.solr.Shard;
import bfd.sys.solr.util.Constants;
import bfd.sys.solr.util.HashUtil;
import bfd.sys.solr.util.ShardUtil;
/**
 * 消费者：将数据写到索引库.
 * 读取ConsumeThread生产的数据【在队列Constants.data.get(cid)中，
 * 类型为：ConcurrentLinkedQueue<SolrInputDocument>】.
 */
public class SolrThread extends Thread {
	
	private static Logger logger = Logger.getLogger(SolrThread.class);
	
	private String cid;
	
	SolrThread(String cid){
		this.cid = cid;
	}

	@Override
	public void run() {
		logger.info(cid + " solr feed thread start_over.");
		Map<String, SolrInputDocument> updateDocs = new HashMap<String, SolrInputDocument>();
		try {
			ConcurrentLinkedQueue<SolrInputDocument> docs = Constants.data.get(cid);
			SolrInputDocument doc = null;
			Map<String, Long> leastDocs = new HashMap<String, Long>();
			int max = 200000,i=0;
			while ((doc = docs.poll()) != null&&i++<max) {
				String cid_iid = doc.getFieldValue("cid_iid").toString();
//				Long time;
//				try {
//					time = Long.getLong(doc.getFieldValue("timestamp").toString());
//					if (leastDocs.containsKey(cid_iid)) {//hj:能走到这？
//						if (leastDocs.get(cid_iid) > time) {
//							logger.info(cid + " queue skip_doc :" + cid_iid);
//							continue;
//						}
//						leastDocs.put(cid_iid, time);
//					}
//				} catch (Exception e) {
//					logger.debug("TIMESTAMP_EXCEPTION:"+doc.toString());
//					e.printStackTrace();
//				}
				updateDocs.put(cid_iid, doc);
				logger.info(cid + " queue remove_doc :" + cid_iid);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		logger.info(cid + " queue remove_doc_size:"+updateDocs.size());
		if(updateDocs.size() > 0){
			Map<Shard,List<SolrInputDocument>> map = new HashMap<Shard, List<SolrInputDocument>>();
			for(String cid_iid:updateDocs.keySet()){
				Shard shard = ShardUtil.locateShard(cid,HashUtil.hash(cid_iid));
				if(!map.containsKey(shard)){
					map.put(shard, new ArrayList<SolrInputDocument>());
				}
				map.get(shard).add(updateDocs.get(cid_iid));
			}
			for(Shard shard:map.keySet()){
				for(SolrServer server:ShardUtil.locateSolrs(shard)){
					try {
						server.add(map.get(shard));
						logger.info("insert_to solr "+cid);
						server.commit(true, true, true);//hj:可改为批量提交
					} catch (SolrServerException e) {
						logger.error(e.getMessage());
						e.printStackTrace();
					} catch (IOException e) {
						logger.error(e.getMessage());
						e.printStackTrace();
					}
				}
				
			}
		}
		logger.info(Thread.currentThread().getName()+"solr commit_over.");
	}

	
}

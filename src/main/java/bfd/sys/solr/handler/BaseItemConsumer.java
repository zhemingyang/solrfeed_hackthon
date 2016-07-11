package bfd.sys.solr.handler;

import bfd.sys.solr.SolrField;
import bfd.sys.solr.util.Constants;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;

import java.util.*;
import java.util.concurrent.Exchanger;

public abstract class BaseItemConsumer extends AbstractTopicConsumer {

	protected static Logger logger = Logger.getLogger(BaseItemConsumer.class);

	public BaseItemConsumer(String topic,String group,Exchanger<List<String>> exchanger) {
		this.topic = topic;
		this.group = group;
		this.exchanger = exchanger;
		this.isDebug = false;
	}
	
	public BaseItemConsumer(String topic,String group,Exchanger<List<String>> exchanger,boolean isDebug) {
		this.topic = topic;
		this.group = group;
		this.exchanger = exchanger;
		this.isDebug = isDebug;
	}


	public void process(ArrayList<SolrInputDocument> docs) {
		for (int i = 0; i < docs.size(); i++) {
			try {
//				String cid = docs.get(i).getFieldValue("cid").toString().replace("_test", "");
				if(docs.get(i).containsKey("type") && "newslist".equals(docs.get(i).getFieldValue("type").toString())){
					Constants.data.get("hackthon_list").offer(docs.get(i));
					logger.info("hackthon_list" + " queue insert_doc :" + docs.get(i).getFieldValue("cid_iid").toString());
				}
				if(docs.get(i).containsKey("type") && "newscontent".equals(docs.get(i).getFieldValue("type").toString())){
					Constants.data.get("hackthon_importnew").offer(docs.get(i));
					logger.info("hackthon_detail" + " queue insert_doc :" + docs.get(i).getFieldValue("cid_iid").toString());
				}
			} catch (Exception e) {
				logger.error(docs.get(i));
				logger.error(e.getStackTrace());
			}
		}
	}
	
	public ArrayList<SolrInputDocument> updateItem(String msg){
		try {
			logger.info(("updateItem:msg->"+msg));
			ArrayList<SolrInputDocument> documents = new ArrayList<>();
			JSONObject json = JSONObject.fromObject(msg);
			String cid = json.getString("cid");
			String iid = json.getString("iid");
			String cid_iid = new StringBuffer(cid).append("_").append(iid).toString();

			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("cid", cid);
			doc.addField("iid", iid);
			doc.addField("cid_iid", cid_iid);

			if(json.has("contents")){
				doc.addField("contents",json.get("contents").toString());
			}
			if(json.has("html")){
				doc.addField("html",json.get("html").toString());
			}
			if(json.has("title")){
				doc.addField("title",json.get("title").toString());
			}

			doc.addField("timestamps",((Double)json.getDouble("creation_time")).longValue());

			if(json.has("post_time")){
				doc.addField("ptime",(long)json.get("post_time")/1000);
			}else {
				doc.addField("ptime",((Double)json.getDouble("creation_time")).longValue());
			}

			if(json.has("topic")){
				doc.addField("topic",json.get("topic").toString());
			}
			if(json.has("url")){
				doc.addField("url",json.get("url").toString());
			}
			if(json.has("keyword")){
				doc.addField("keywords",json.get("keyword").toString());
			}
			if(json.has("abs")){
				doc.addField("abs",json.get("abs").toString());
			}
			if(json.has("loc")){
				doc.addField("loc",json.get("loc").toString());
			}
			if(json.has("pic")){
				doc.addField("pic",json.get("pic").toString());
			}
			if(json.has("cat")){
				doc.addField("cat",json.get("cat").toString());
			}
			if(json.has("tag")){
				doc.addField("tag",json.get("tag").toString());
			}
			if(json.has("type")){
				doc.addField("type",json.get("type").toString());
			}
			documents.add(doc);
			return documents;
		} catch (Throwable e) {
			logger.error("BaseItemConsumer.updateItem_error:"+e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public ArrayList<SolrInputDocument> updateListItem(String msg){
		try {
			logger.info(("updateListItem:msg->"+msg));
			ArrayList<SolrInputDocument> docs = new ArrayList<>();
			SolrInputDocument doc;
			JSONObject json = JSONObject.fromObject(msg);
			String cid = json.getString("cid");
			String iid,cid_iid;
			JSONArray items = json.getJSONArray("items");// TODO
			for(Object object : items){
				doc = new SolrInputDocument();
				JSONObject jsonObject = JSONObject.fromObject(object);
				iid = jsonObject.get("iid").toString();
				cid_iid = new StringBuffer(cid).append("_").append(iid).toString();
				doc.addField("cid", cid);
				doc.addField("iid", iid);
				doc.addField("cid_iid", cid_iid);
				if(jsonObject.has("title")){
					doc.addField("title",jsonObject.get("title").toString());
				}
				if(jsonObject.has("smallPic")){
					doc.addField("smallPic",jsonObject.get("smallPic").toString());
				}
				if(jsonObject.has("abs")){
					doc.addField("abs",jsonObject.get("abs").toString());
				}
				if(jsonObject.has("posttime")){
					doc.addField("posttime",jsonObject.get("posttime").toString());
				}
				if(jsonObject.has("infoUrl")){
					doc.addField("infoUrl",jsonObject.get("infoUrl").toString());
				}
				if(json.has("type")){
					doc.addField("type",json.get("type").toString());
				}
				docs.add(doc);
			}
			logger.info(("updateListItem finished! msg->"+msg));
			return docs;
		} catch (Throwable e) {
			logger.error("BaseItemConsumer.updateItem_error:"+e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	private void setAttr(SolrInputDocument doc, Map<String, SolrField> attrs,
			JSONObject attrJson, String attr, boolean flag) {
		
		if(flag && (attrJson.get(attr) instanceof JSONArray)){
			JSONArray v = attrJson.getJSONArray(attr);
			doc.addField(attr, v);
		}else{
			switch (attrs.get(attr).getType()) {
			case BOOLEAN:
				doc.addField(attr, attrJson.getBoolean(attr));
				break;
			case INT:
				doc.addField(attr, attrJson.getInt(attr));
				break;
			case LONG:
				doc.addField(attr, attrJson.getLong(attr));
				break;
			case FLOAT:
				doc.addField(attr, Float.valueOf(attrJson.getString(attr)));
				break;
			case DOUBLE:
				doc.addField(attr, attrJson.getDouble(attr));
				break;
			default:
				doc.addField(attr, attrJson.getString(attr));
				break;
			}
		}
	}


	public ArrayList<SolrInputDocument> rmItem(String msg) {
		if(isDebug){
			info(msg);
		}
		ArrayList<SolrInputDocument> documents = new ArrayList<>();
		JSONObject json = JSONObject.fromObject(msg);
		SolrInputDocument doc = new SolrInputDocument();
		/*String cacheBaseKey = json.getString("cid") + ":NewsBase:" + json.getString("iid");
		info("remove_doc_"+cacheBaseKey.replace(":NewsBase:", ""));
		NewsBase base = null;
		try {
			byte[] value = memc.get(cacheBaseKey);
			if (null != value) {
				base = NewsBase.parseFrom(value);
			}
		} catch (InvalidProtocolBufferException e) {
			logger.error(e.getMessage());
		}
		
		if (base != null) {
			return null;
		}*/
		doc.addField("cid_iid", json.getString("cid")+"_"+json.getString("iid"));
		doc.addField("cid", json.getString("cid"));
		doc.addField("iid", json.getString("iid"));
		Map<String,Boolean>  expired = new HashMap<String, Boolean>();
		expired.put("set", true);
		doc.addField("expired", expired);
		if(json.has("timestamp")){
			doc.addField("timestamp", ((Double)json.getDouble("timestamp")).longValue());
		}
		info("rm doc:"+doc.getField("cid_iid").toString());
		documents.add(doc);
		return documents;
	}

	private void addField(SolrInputDocument doc, String field, Collection goods) {
		if (goods != null && !goods.isEmpty()) {
			doc.addField(field, goods);
		}
	}
	

	private void addField(SolrInputDocument doc, String field,
			List<String> goods, float boost) {
		if (goods != null && !goods.isEmpty()) {
			doc.addField(field, goods, boost);
		}
	}

}

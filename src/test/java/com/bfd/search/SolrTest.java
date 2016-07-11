/**
 * 
 */
package com.bfd.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

/**
 * @class SolrTest
 * @package com.bfd.search
 * @description TODO
 * @author super(weiguo.gao@baifendian.com)
 * @date 2014-9-16
 *
 */
public class SolrTest {
	
	public static void main(String[] args) throws Exception, IOException {
		List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		SolrServer solrServer = new HttpSolrServer("http://192.168.61.73:9030/solr/Cxicihutong");
		solrServer.deleteById("Cxicihutong_d208301127");
		solrServer.commit();
	}

}

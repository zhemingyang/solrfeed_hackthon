/**
 * 
 */
package bfd.sys.solr.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.nlpcn.commons.lang.finger.pojo.MyFingerprint;
import org.nlpcn.commons.lang.tire.GetWord;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.tire.library.Library;
import org.nlpcn.commons.lang.util.MD5;
import org.nlpcn.commons.lang.util.StringUtil;

/**
 * @class FingerUtil
 * @package bfd.sys.solr.util
 * @description TODO
 * @author super(weiguo.gao@baifendian.com)
 * @date 2014-9-23
 *
 */
public class FingerUtil {
	
	private static Forest forest = new Forest();
	
	static{
		try {
			forest = Library.makeForest(FingerUtil.class.getResourceAsStream("/finger.dic"));
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String fingerprint(String content)
    {
        content = StringUtil.rmHtmlTag(content);
        GetWord word = new GetWord(forest, content);
        String temp = null;
        int middleLength = content.length() / 2;
        HashMap<String,MyFingerprint> hm = new HashMap<String,MyFingerprint>();
        MyFingerprint myFingerprint = null;
        do
        {
            if((temp = word.getFrontWords()) == null)
                break;
            if(temp == null || temp.length() != 0)
            {
                temp = temp.toLowerCase();
                double factory = 1.0D - (double)Math.abs(middleLength - word.offe) / (double)middleLength;
                if((myFingerprint = (MyFingerprint)hm.get(temp)) != null)
                    myFingerprint.updateScore(factory);
                else
                    hm.put(temp, new MyFingerprint(temp, Double.parseDouble(word.getParam(1)), factory));
            }
        } while(true);
        Set<MyFingerprint> set = new TreeSet<MyFingerprint>();
        set.addAll(hm.values());
        int size = Math.min(set.size() / 10, 4) + 1;
        Iterator<MyFingerprint> iterator = set.iterator();
        int j = 0;
        HashSet<String> hs = new HashSet<String>();
        for(; j < size && iterator.hasNext(); j++)
        {
            myFingerprint = (MyFingerprint)iterator.next();
            hs.add((new StringBuilder()).append(myFingerprint.getName()).append(" ").toString());
        }

        String finger = MD5.code(hs.toString());
        return finger;
    }
}

package bfd.sys.solr.util;

/**
 * 存储每个shard所处的hash段：0-239中的一段，如（0,79）
 */
public class Range implements Comparable<Range>{
	
	private int min;
	private int max;
	

	public Range(int min, int max) {
		super();
		this.min = min;
		this.max = max;
	}


	public int getMin() {
		return min;
	}


	public void setMin(int min) {
		this.min = min;
	}


	public int getMax() {
		return max;
	}


	public void setMax(int max) {
		this.max = max;
	}


	@Override
	public int compareTo(Range o) {
		if(this.max<o.min){
			return -1;
		}
		if(this.min>o.max){
			return 1;
		}
		return 0;
	}

}

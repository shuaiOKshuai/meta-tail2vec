package prep;

import utils.Pair;


public class Attribute extends Pair<String, String> {
	public Attribute(String key, String value) {
		super(key, value);
	}
	
	@Override
	public boolean equals(Object x) {
		if (! (x instanceof Attribute))
			return false;
		
		Attribute a = (Attribute)x;
		return (a.key.equals(this.key)) && (a.value.equals(this.value));
	}
	
	@Override
	public int hashCode() {
		return key.hashCode() + value.hashCode() * 13;			
	}
}

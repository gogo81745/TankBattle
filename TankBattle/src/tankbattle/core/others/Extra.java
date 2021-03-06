package tankbattle.core.others;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 本类是{@link Extrable}的默认实现方式<br>
 * 
 * @see Extrable
 * @author Gogo
 *
 */
public class Extra implements Extrable {

	private Map<String, Object> extras = Collections.synchronizedMap(new HashMap<String, Object>());

	@Override
	@SuppressWarnings("unchecked")
	public <E> E put(String key, E obj) {
		return (E) extras.put(key, obj);
	}

	@Override
	public Object getObj(String key) {
		return extras.get(key);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getObj(String key, Class<T> T) {
		return (T) extras.get(key);
	}

	@Override
	public boolean contains(String key) {
		return extras.containsKey(key);
	}

	@Override
	public int getInt(String key) {
		return ((Integer) extras.get(key)).intValue();
	}

	@Override
	public double getDouble(String key) {
		return ((Double) extras.get(key)).doubleValue();
	}

	@Override
	public boolean getBool(String key) {
		return ((Boolean) extras.get(key)).booleanValue();
	}

	@Override
	public String getString(String key) {
		return (String) extras.get(key);
	}

	@Override
	public Object remove(String key) {
		return extras.remove(key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T remove(String key, Class<T> T) {
		return (T) extras.remove(key);
	}

	@Override
	public Extrable extra() {
		return this;
	}

}

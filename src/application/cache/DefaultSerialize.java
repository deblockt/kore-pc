package application.cache;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public class DefaultSerialize implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3917825219747926732L;

	private Map<String, Object> properties = new HashMap<>();

	private String className;

	public DefaultSerialize(Object movie) {
		this.className = movie.getClass().getName();
		for (Field field : movie.getClass().getFields()) {
			if (!Modifier.isStatic(field.getModifiers())) {
				try {
					properties.put(field.getName(), field.get(movie));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private Object readResolve() throws ObjectStreamException {
		try {
			Class<?> clazz = Class.forName(this.className);
			Object retour = clazz.getConstructor(JsonNode.class).newInstance(new Object[]{JsonNodeFactory.instance.objectNode()});

			for (Entry<String, Object> entry : this.properties.entrySet()) {
				try {
					Field field = clazz.getField(entry.getKey());
					field.setAccessible(true);
					field.set(retour, entry.getValue());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			return retour;
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return null;
	}
}

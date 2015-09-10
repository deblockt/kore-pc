package application.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ParameterService {
    /**
     * singleton
     */
	private static final ParameterService INSTANCE = new ParameterService();

	/**
	 * properties information
	 */
    private final Properties properties = new Properties();

    private final String PROPERTIES_FILE = "./config/config.properties";

    private ParameterService() {
    	try {
			properties.load(new FileInputStream(new File(PROPERTIES_FILE)));
		} catch (IOException e) {
			System.err.println("Impossible de charger les paramètres");
			e.printStackTrace();
		}
    }

    public static ParameterService getInstance() {
        return INSTANCE;
    }

    /**
     * return a string param. If param don't exists null is returned
     * @param paramName the parameterName
     * @return
     */
    public String getString(final String paramName) {
    	return getString(paramName, null);
    }

    /**
     * return a string value
     *
     * @param paramName
     * @param defaultValue
     * @return
     */
    public String getString(final String paramName, final String defaultValue) {
    	String prop = this.properties.getProperty(paramName);
    	return prop == null ? defaultValue : prop;
    }

    /**
     * return integer properties
     * @param paramName
     * @param defaultValue
     * @return
     */
	public Integer getInt(String paramName, int defaultValue) {
		String value = this.getString(paramName);
		return value == null ? defaultValue: Integer.parseInt(value);
	}

	/**
	 *
	 * @param paramName
	 * @param defaultValue
	 * @return
	 */
	public boolean getBoolean(String paramName, boolean defaultValue) {
		String value = this.getString(paramName);
		return value == null ? defaultValue : "1".equals(value) || "true".equals(value);
	}

    /**
     * set the parameter value
     * @param paramName propertiesPath
     * @param value the new value
     */
	public void setParameter(final String paramName, final String value) {
		this.properties.setProperty(paramName, value);
	}

	/**
	 * set the parameter value
	 * @param paramName
	 * @param value
	 */
	public void setParameter(String paramName, int value) {
		this.setParameter(paramName, String.valueOf(value));
	}

	/**
	 * set the parameter value
	 * @param paramName
	 * @param value
	 */
	public void setParameter(String paramName, boolean value) {
		this.setParameter(paramName, ""+value);
	}

	/**
	 * save properties
	 */
	public void save() {
		try {
			this.properties.store(new FileOutputStream(new File(PROPERTIES_FILE)), "saved by kore pc");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}




}

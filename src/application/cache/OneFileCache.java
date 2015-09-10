package application.cache;

import application.cache.CacheFactory.FilenameGenerator;

/**
 * file cache is only a file
 * @author thomas
 *
 * @param <T>
 */
public class OneFileCache implements FilenameGenerator<Object> {

	private final String filename;

	public OneFileCache(String filename) {
		this.filename = filename;
	}

	@Override
	public String generateFilename(Object data) {
		return this.filename;
	}

}
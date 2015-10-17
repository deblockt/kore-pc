package application.cache;

import java.io.File;

public class CacheFactory<T> {
	/**
	 * interface used for generate filename for the cache
	 *
	 * @author thomas
	 *
	 * @param <T>
	 */
	public interface FilenameGenerator<T> {
		/**
		 * method must return the filename cache
		 *
		 * @param data
		 * @return
		 */
		public String generateFilename(T data);
	}


	public final static String DEFAULT_CACHE_DIRECTORY = System.getProperty("java.io.tmpdir");

	private final FilenameGenerator<? super T> filenameGenerator;

	private final String cacheDirectory;

	public CacheFactory(FilenameGenerator<? super T> filenameGenerator, String cacheDirectory) {
		super();
		this.filenameGenerator = filenameGenerator;
		this.cacheDirectory = cacheDirectory;
		File directory = new File(this.cacheDirectory);
		if (!directory.exists()) {
			directory.mkdir();
		}
	}

	public CacheFactory(FilenameGenerator<? super T> filenameGenerator) {
		this(filenameGenerator, DEFAULT_CACHE_DIRECTORY);
	}

	/**
	 * get a cache for a filename
	 *
	 * @param filename the data used for filename (can be null if filenameGenerator don't need)
	 * @return
	 */
	public <Y> Cache<Y> getCache(T data) {
		return new Cache<Y>(new File(cacheDirectory, filenameGenerator.generateFilename(data)));
	}

	/**
	 * get a cache for a filename.
	 * this method need to be used only with OneFileCache
	 *
	 * @return
	 */
	public <Y> Cache<Y> getCache() {
		return this.getCache(null);
	}
}

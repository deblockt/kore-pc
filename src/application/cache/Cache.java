package application.cache;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Cache<Y> {


	private final File file;

	/**
	 * create a cache manager
	 *
	 * @param filenameGenerator
	 * @param the cache directory
	 */
	public Cache(File file) {
		this.file = file;
	}

	/**
	 * save the object one the filecache
	 * @param object
	 */
	public void save(Y object) {
		ObjectOutputStream outputstream;
		try {
			outputstream = new MediInfoOutputStream(new FileOutputStream(this.file));
			outputstream.writeObject(object);
			outputstream.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * return the path to fileCache
	 * @return
	 */
	public String getCacheFilePath() {
		return this.file.getAbsolutePath();
	}

	/**
	 * check if the cache file already exists
	 * @param videoCacheFile
	 * @return
	 */
	public boolean issetCache() {
		return file.exists();
	}

	/**
	 * remove the cache file
	 * @param videoCacheFile
	 */
	public void clearCache() {
		this.file.delete();
	}

	/**
	 * get the cache data information
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Y getCacheData() throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream inputStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(this.file)));
		Y movies = (Y) inputStream.readObject();
		inputStream.close();
		return movies;
	}
}

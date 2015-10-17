package application.component;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.commons.codec.digest.DigestUtils;

import application.cache.Cache;
import application.cache.CacheFactory;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class CacheImageFactory {

	private static CacheFactory<String> IMAGE_CACHE_FACTORY = new CacheFactory<String>((file) -> {
		return DigestUtils.md5Hex(file) + ".png";
	}, CacheFactory.DEFAULT_CACHE_DIRECTORY + File.separatorChar + "kodyImage");

	public static Image getImage(String url, double requestedWidth, double requestedHeight, boolean preserveRatio, boolean smooth, boolean backgroundLoading) {
		Cache<String> cache = IMAGE_CACHE_FACTORY.getCache(url + requestedHeight + requestedWidth);
		String imageUrl = url;
		if (cache.issetCache()) {
			imageUrl = "file:"+cache.getCacheFilePath();
		}

		Image image = new Image(imageUrl, requestedWidth, requestedHeight, preserveRatio, smooth, backgroundLoading);
		if (!cache.issetCache()) {
			image.progressProperty().addListener((event, oldValue, newValue) -> {
				if (!image.isError() && newValue.intValue() == 1) {
					BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
					try {
						ImageIO.write(bImage, "png", new File(cache.getCacheFilePath()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}

		return image;
	}

	public static Image getImage(String imageUrl, boolean async) {
		return getImage(imageUrl, 0, 0, false, false, async);
	}

}

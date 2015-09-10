package application.cache;

import org.xbmc.kore.jsonrpc.ApiCallback;
import org.xbmc.kore.jsonrpc.ApiMethod;
import org.xbmc.kore.jsonrpc.Handler;
import org.xbmc.kore.jsonrpc.HostConnection;

public class CachedApiMethod<T> {
	/**
	 * The api method
	 */
	private ApiMethod<T> apiMethod;
	/**
	 * the cache file
	 */
	private Cache<T> cache;

	/**
	 * reate an apiMethod cached
	 *
	 * @param apiMethod
	 * @param cache
	 */
	public CachedApiMethod(ApiMethod<T> apiMethod, Cache<T> cache) {
		this.apiMethod = apiMethod;
		this.cache = cache;
	}

	/**
	 * execute the api method
	 *
	 * @param hostConnection
	 * @param callback
	 * @param handler
	 */
	public void execute(HostConnection hostConnection, ApiCallback<T> callback, Handler handler) {
		if (this.cache.issetCache()) {
			try {
				T result = cache.getCacheData();
				callback.onSuccess(result);
				return;
			} catch (Exception e) {
				// error, no issue (call normal api method)
				e.printStackTrace();
			}
		}

		// no cache file, or cache error
		apiMethod.execute(hostConnection, new ApiCallback<T>() {

			@Override
			public void onSuccess(T result) {
				cache.save(result);
				callback.onSuccess(result);
			}

			@Override
			public void onError(int errorCode, String description) {
				callback.onError(errorCode, description);
			}
		}, handler);
	}


}

package com.replicanet;

import java.io.InputStream;

/**
 * Allows callbacks into the server
 */
public interface ACEServerCallback
{
	/**
	 * If an InputStream is returned then dynamic runtime data can be used instead of file data
	 * @param uri
	 * @return
	 */
	InputStream beforeGet(String uri, String query);

	void afterGet(String uri, String query);

	void afterPut(String uri, String query);
}

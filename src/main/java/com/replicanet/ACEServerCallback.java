package com.replicanet;

/**
 * Allows callbacks into the server
 */
public interface ACEServerCallback
{
	void afterGet(String uri);

	void afterPut(String uri);
}

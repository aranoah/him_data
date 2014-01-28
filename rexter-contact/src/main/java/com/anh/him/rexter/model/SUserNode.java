package com.anh.him.rexter.model;

import java.util.HashMap;
import java.util.Iterator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tinkerpop.blueprints.Vertex;

public class SUserNode extends HashMap {
	@JsonIgnore
	Vertex vertex;
	public static final int ACTIVE = 0;
	public static final int BLOCKED = 1;
	public static final int SUSPEND = 2;

	public SUserNode() {
	}

	public SUserNode(Vertex vertex) {
		this.vertex = vertex;
		Iterator<String> iter = vertex.getPropertyKeys().iterator();
		String key = null;
		while (iter.hasNext()) {
			key = iter.next();
			this.put(key, vertex.getProperty(key));
		}
	}

	public void jsonInit() {
		Iterator<String> iter = vertex.getPropertyKeys().iterator();
		String key = null;
		while (iter.hasNext()) {
			key = iter.next();
			this.put(key, vertex.getProperty(key));
		}
	}

	public String getPhotoUrl() {
		return vertex.getProperty("photoUrl");
	}

	public void setPhotoUrl(String photoUrl) {
		vertex.setProperty("photoUrl", photoUrl);
	}

	public String getDomain() {
		return vertex.getProperty("domain");
	}

	public void setDomain(String domain) {
		vertex.setProperty("domain", domain);
	}

	public String getEmailAddress() {
		return vertex.getProperty("emailId");
	}

	public void setEmailAddress(String emailAddress) {
		vertex.setProperty("emailId", emailAddress);
	}

	public Integer getStatus() {
		return vertex.getProperty("status");
	}

	public void setStatus(Integer status) {
		vertex.setProperty("status", status);
	}

	public Vertex getVertex() {
		return vertex;
	}

	public void setVertex(Vertex vertex) {
		this.vertex = vertex;
	}

	public void setName(String name) {
		vertex.setProperty("name", name);
	}

	public String getName() {
		return vertex.getProperty("name");
	}

	public String getId() {
		return vertex.getProperty("__id");
	}

	public void setId(String id) {
		vertex.setProperty("__id", id);
	}

	public void setVer(Long ver) {
		vertex.setProperty("ver", ver);
	}

	public Long getVer() {
		return vertex.getProperty("ver");
	}

	public void setProfile(String profile) {
		vertex.setProperty("profile", profile);
	}

	public String getProfile() {
		return vertex.getProperty("profile");
	}

	public void setLikes(Long t) {
		vertex.setProperty("likes", t);
	}

	public Long getLikes() {
		return vertex.getProperty("likes");
	}

	public void setRating(Float t) {
		vertex.setProperty("rating", t);
	}

	public Float getRating() {
		return vertex.getProperty("rating");
	}

	/**
	 * @return the followers
	 */
	public Long getFollowers() {
		return vertex.getProperty("followers");
	}

	/**
	 * @param followers
	 *            the followers to set
	 */
	public void setFollowers(Long followers) {
		vertex.setProperty("followers", followers);
	}

}

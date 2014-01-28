package com.anh.him.rexter.model;

import java.util.HashMap;
import java.util.Iterator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thinkaurelius.titan.core.attribute.Geoshape.Point;
import com.tinkerpop.blueprints.Vertex;

public class SCircleNode extends HashMap {
	@JsonIgnore
	Vertex vertex;
	public static final int ACTIVE = 0;

	public SCircleNode() {
	}

	public SCircleNode(Vertex vertex) {
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

	/**
	 * cid
	 * 
	 * @return
	 */
	public String getId() {
		return vertex.getProperty("__cid");
	}

	public void setId(String id) {
		vertex.setProperty("__cid", id);
	}

	public String getType() {
		return vertex.getProperty("type");
	}

	public void setType(String type) {
		vertex.setProperty("type", type);
	}

	public String getName() {
		return vertex.getProperty("cname");
	}

	public void setName(String cname) {
		vertex.setProperty("cname", cname);
	}

	public String getNodeType() {
		return vertex.getProperty("type");
	}

	public void setNodeType(String nt) {
		vertex.setProperty("type", nt);
	}

	public void setPincode(String pincode) {
		vertex.setProperty("cpincode", pincode);
	}

	public String getPincode(String pincode) {
		return vertex.getProperty("cpincode");
	}

	public void setCity(String city) {
		vertex.setProperty("ccity", city);
	}

	public String getCity() {
		return vertex.getProperty("ccity");
	}

	public void setArea(String area) {
		vertex.setProperty("carea", area);
	}

	public void setState(String state) {
		vertex.setProperty("cstate", state);
	}

	public String getState() {
		return vertex.getProperty("cstate");
	}

	public void setLoc(Point point) {
		vertex.setProperty("cloc", point);
	}

	public Point getLoc() {
		return vertex.getProperty("cloc");
	}

}

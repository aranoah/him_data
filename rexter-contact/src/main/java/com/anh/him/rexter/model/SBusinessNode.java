package com.anh.him.rexter.model;

import java.util.HashMap;
import java.util.Iterator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thinkaurelius.titan.core.attribute.Geoshape.Point;
import com.tinkerpop.blueprints.Vertex;

public class SBusinessNode extends HashMap {
	@JsonIgnore
	Vertex vertex;
	public static final int ACTIVE=0;
	public static final int BLOCKED=1;
	public static final int SUSPEND=2;


	public SBusinessNode() {
	}
	public SBusinessNode(Vertex vertex) {
		this.vertex = vertex;
		Iterator<String> iter = vertex.getPropertyKeys().iterator();
		String key = null;
		while (iter.hasNext()) {
			key = iter.next();
			this.put(key, vertex.getProperty(key));
		}
	}
	
	public void jsonInit(){
		Iterator<String> iter = vertex.getPropertyKeys().iterator();
		String key = null;
		while (iter.hasNext()) {
			key = iter.next();
			this.put(key, vertex.getProperty(key));
		}
		
	}

	/**
	 * cid
	 * @return
	 */
	public String getId() {
		return vertex.getProperty("__bid");
	}

	public void setId(String id) {
		vertex.setProperty("__bid", id);
	}
	public String getType() {
		return vertex.getProperty("btype");
	}

	public void setType(String type) {
		vertex.setProperty("btype", type);
	}	
	public String getName() {
		return vertex.getProperty("bname");
	}
	public void setName(String cname) {
		vertex.setProperty("bname", cname);
	}
	public String getNodeType() {
		return vertex.getProperty("nodeType");
	}
	public void setNodeType(String nt) {
		vertex.setProperty("nodeType", nt);
	}
	
	public String getState(){
		return vertex.getProperty("bstate");
	}
	public void setGeo(Point point){
		vertex.setProperty("bgeo", point);
	}
	public Point getGeo(){
		return vertex.getProperty("bgeo");
	}                     
	
	public void setStatus(Integer active){
		vertex.setProperty("bactive", active);
	}
	public Integer getStatus(){
		return vertex.getProperty("bactive");
	}
	public void setTags(String tags){
		vertex.setProperty("btags", tags);
	}
	public String getTags(){
		return vertex.getProperty("btags");
	}
	public void addValues(String key, Object v){
		vertex.setProperty(key, v);
	}
}

package com.anh.him.rexter.model;

import java.util.Date;

import com.anh.him.rexter.util.HIMGraphConstant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import com.tinkerpop.blueprints.Edge;

public class SGeoLocationEdge {
	private Object id;

	@JsonIgnore
	private Edge edge;
	private String label;
	private Integer status = 0;
	private Date doj;
	private Float rating;
	private Long likes;
	private String etags;
	private String profile;
	private Long followers;
	private Geoshape loc;
	private String name;
	private String area;
	private String city;
	private String state;
	private String country;
	private Integer pincode;
	private String cid;
	private String house;
	private String btype;
	private String category;
	private String serviceId;

	/**
	 * 
	 * @return the btype
	 */
	public String getBtype() {
		return btype;
	}

	/**
	 * @param btype
	 *            the btype to set
	 */
	public void setBtype(String btype) {
		this.btype = btype;
	}

	/**
	 * @return the area
	 */
	public String getArea() {
		return area;
	}

	/**
	 * @param area
	 *            the area to set
	 */
	public void setArea(String area) {
		this.area = area;
	}

	/**
	 * @return the house
	 */
	public String getHouse() {
		return house;
	}

	/**
	 * @param house
	 *            the house to set
	 */
	public void setHouse(String house) {
		this.house = house;
	}

	public SGeoLocationEdge() {

	}

	public SGeoLocationEdge(Edge e) {
		this.edge = e;
		id = e.getId();
		label = e.getLabel();
		this.doj = e.getProperty("doj");
		this.rating = e.getProperty("rating");
		this.likes = e.getProperty("likes");
		this.etags = e.getProperty("etags");
		this.profile = e.getProperty("profile");
		this.name = e.getProperty("name");
		this.followers = e.getProperty("followers");
		this.loc = e.getProperty("loc");
		this.area = e.getProperty("area");
		this.house = e.getProperty("house");
		this.status = e.getProperty("status");
		this.city = e.getProperty("city");
		this.state = e.getProperty("state");
		this.country = e.getProperty("country");
		this.pincode = e.getProperty("pincode");
		this.cid = e.getProperty("cid");
		this.btype = e.getProperty("btype");
	}

	public void prepare() {
		if (rating != null)
			this.edge.setProperty("rating", rating);
		if (likes != null)
			this.edge.setProperty("likes", likes);
		if (etags != null)
			this.edge.setProperty("etags", etags);
		if (profile != null)
			this.edge.setProperty("profile", profile);
		if (followers != null)
			this.edge.setProperty("followers", followers);
		if (loc != null)
			this.edge.setProperty("loc", loc);
		if (area != null)
			this.edge.setProperty("area", area);
		if (house != null)
			this.edge.setProperty("house", house);
		if (status != null)
			this.edge.setProperty("status", status);
		if (cid != null)
			this.edge.setProperty("cid", cid);
		if (name != null)
			this.edge.setProperty("name", name);
		if (doj != null)
			this.edge.setProperty("doj", doj);
		if (city != null)
			this.edge.setProperty("city", city);
		if (state != null)
			this.edge.setProperty("state", state);
		if (country != null)
			this.edge.setProperty("country", country);
		if (pincode != null)
			this.edge.setProperty("pincode", pincode);
		if (btype != null)
			this.edge.setProperty("btype", btype);
	}

	/**
	 * @return the id
	 */
	public Object getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Object id) {
		this.id = id;
	}

	/**
	 * @return the edge
	 */
	public Edge getEdge() {
		return edge;
	}

	/**
	 * @param edge
	 *            the edge to set
	 */
	public void setEdge(Edge edge) {
		this.edge = edge;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the doj
	 */
	public Date getDoj() {
		return doj;
	}

	/**
	 * @param doj
	 *            the doj to set
	 */
	public void setDoj(Date doj) {
		this.doj = doj;
	}

	/**
	 * @return the rating
	 */
	public Float getRating() {
		return rating;
	}

	/**
	 * @param rating
	 *            the rating to set
	 */
	public void setRating(Float rating) {
		this.rating = rating;
	}

	/**
	 * @return the likes
	 */
	public Long getLikes() {
		return likes;
	}

	/**
	 * @param likes
	 *            the likes to set
	 */
	public void setLikes(Long likes) {
		this.likes = likes;
	}

	/**
	 * @return the etags
	 */
	public String getEtags() {
		return etags;
	}

	/**
	 * @param etags
	 *            the etags to set
	 */
	public void setEtags(String etags) {
		this.etags = etags;
	}

	/**
	 * @return the profile
	 */
	public String getProfile() {
		return profile;
	}

	/**
	 * @param profile
	 *            the profile to set
	 */
	public void setProfile(String profile) {
		this.profile = profile;
	}

	/**
	 * @return the followers
	 */
	public Long getFollowers() {
		return followers;
	}

	/**
	 * @param followers
	 *            the followers to set
	 */
	public void setFollowers(Long followers) {
		this.followers = followers;
	}

	/**
	 * @return the loc
	 */
	public Geoshape getLoc() {
		return loc;
	}

	/**
	 * @param loc
	 *            the loc to set
	 */
	public void setLoc(Geoshape loc) {
		this.loc = loc;
	}

	/**
	 * @return the status
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @param city
	 *            the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * @param state
	 *            the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @param country
	 *            the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * @return the pincode
	 */
	public Integer getPincode() {
		return pincode;
	}

	/**
	 * @param pincode
	 *            the pincode to set
	 */
	public void setPincode(Integer pincode) {
		this.pincode = pincode;
	}

	public void setCid(String cid) {
		this.cid = cid;

	}

	/**
	 * @return the cid
	 */
	public String getCid() {
		return cid;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

}

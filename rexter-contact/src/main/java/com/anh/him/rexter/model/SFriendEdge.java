package com.anh.him.rexter.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tinkerpop.blueprints.Edge;

public class SFriendEdge {
	private Object id;
	public static final String LABEL = "friendOf";
	@JsonIgnore
	private Edge edge;
	private String label;
	private String status;
	private List<String> groupName;

	private Date fsd;
	private Integer sub, ask, recv;
	private Integer fsub, fask, frecv;
	private String initiator;
	private String note;
	private Date lastNoteDate;

	public SFriendEdge(Edge e) {
		this.edge = e;
		id = e.getId();
		label = e.getLabel();
		status = edge.getProperty("estatus");
		ask = edge.getProperty("ask");
		recv = edge.getProperty("recv");
		sub = edge.getProperty("sub");
		fask = edge.getProperty("fask");
		frecv = edge.getProperty("frecv");
		fsub = edge.getProperty("fsub");
		fsd = edge.getProperty("fsd");
	}

	public static enum EnumFriendshipStatus {
		STATIC("static"), PENDING_UR_APPROVAL("waiting for approval"), INVITED(
				"invited"), BLOCKED("blocked"), FRIEND("friend"), REJECT(
				"reject"), ACCEPT("accept");
		private String status;

		private EnumFriendshipStatus(String status) {
			this.status = status;
		}

		public String getStatus() {
			return status;
		}

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
	public Edge prepare() {
		edge.setProperty("estatus", status);
		edge.setProperty("ask", ask);
		edge.setProperty("recv", recv);
		edge.setProperty("sub", sub);
		edge.setProperty("fask", fask);
		edge.setProperty("frecv", frecv);
		edge.setProperty("fsub", fsub);
		edge.setProperty("fsd", new Date());
		if (initiator != null)
			edge.setProperty("initiator", initiator);
		if (note != null)
			edge.setProperty("note", note);
		if (lastNoteDate != null)
			edge.setProperty("lastNoteDate", lastNoteDate);
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
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the groupName
	 */
	public List<String> getGroupName() {
		return groupName;
	}

	/**
	 * @param groupName
	 *            the groupName to set
	 */
	public void setGroupName(List<String> groupName) {
		this.groupName = groupName;
	}

	/**
	 * @return the fsd
	 */
	public Date getFsd() {
		return fsd;
	}

	/**
	 * @param fsd
	 *            the fsd to set
	 */
	public void setFsd(Date fsd) {
		this.fsd = fsd;
	}

	/**
	 * @return the sub
	 */
	public int getSub() {
		return sub;
	}

	/**
	 * @param sub
	 *            the sub to set
	 */
	public void setSub(int sub) {
		this.sub = sub;
	}

	/**
	 * @return the ask
	 */
	public int getAsk() {
		return ask;
	}

	/**
	 * @param ask
	 *            the ask to set
	 */
	public void setAsk(int ask) {
		this.ask = ask;
	}

	/**
	 * @return the recv
	 */
	public int getRecv() {
		return recv;
	}

	/**
	 * @param recv
	 *            the recv to set
	 */
	public void setRecv(int recv) {
		this.recv = recv;
	}

	/**
	 * @return the fsub
	 */
	public int getFsub() {
		return fsub;
	}

	/**
	 * @param fsub
	 *            the fsub to set
	 */
	public void setFsub(int fsub) {
		this.fsub = fsub;
	}

	/**
	 * @return the fask
	 */
	public int getFask() {
		return fask;
	}

	/**
	 * @param fask
	 *            the fask to set
	 */
	public void setFask(int fask) {
		this.fask = fask;
	}

	/**
	 * @return the frecv
	 */
	public int getFrecv() {
		return frecv;
	}

	/**
	 * @param frecv
	 *            the frecv to set
	 */
	public void setFrecv(int frecv) {
		this.frecv = frecv;
	}

	/**
	 * @return the initiator
	 */
	public String getInitiator() {
		return initiator;
	}

	/**
	 * @param initiator
	 *            the initiator to set
	 */
	public void setInitiator(String initiator) {
		this.initiator = initiator;
	}

	/**
	 * @return the note
	 */
	public String getNote() {
		return note;
	}

	/**
	 * @param note
	 *            the note to set
	 */
	public void setNote(String note) {
		this.note = note;
	}

	/**
	 * @return the lastNoteDate
	 */
	public Date getLastNoteDate() {
		return lastNoteDate;
	}

	/**
	 * @param lastNoteDate
	 *            the lastNoteDate to set
	 */
	public void setLastNoteDate(Date lastNoteDate) {
		this.lastNoteDate = lastNoteDate;
	}

	public void setOrigStatus(int sub, int ask, int recv) {
		this.sub = sub;
		this.ask = ask;
		this.recv = recv;
	}

	public void setFndStatus(int sub, int ask, int recv) {
		this.fsub = sub;
		this.fask = ask;
		this.frecv = recv;
	}

}

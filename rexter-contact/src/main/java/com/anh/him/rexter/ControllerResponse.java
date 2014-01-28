package com.anh.him.rexter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thinkaurelius.titan.core.TitanIndexQuery.Result;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONMode;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONUtility;
import com.tinkerpop.pipes.util.structures.Row;
import com.tinkerpop.rexster.extension.ExtensionResponse;

public class ControllerResponse {
	int status = 0;
	String message = "";
	@JsonIgnore
	Object dbResult;
	private static Logger logger = Logger.getLogger(ControllerResponse.class);
	boolean hasMore = false;
	int start = 0;
	int pageSize = 15;
	int total = 15;

	public ControllerResponse() {
	}

	public ControllerResponse(int status) {
		this.status = status;

	}

	public ControllerResponse(int status, String msg) {
		this.status = status;
		this.message = msg;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the dbResult
	 */
	public Object getDbResult() {
		return dbResult;
	}

	/**
	 * @param dbResult
	 *            the dbResult to set
	 */
	public void setDbResult(Object dbResult) {
		this.dbResult = dbResult;
	}

	public ExtensionResponse prepare() {
		try {
			JSONObject response = new JSONObject();
			Object result = null;
			if (dbResult == null) {
			} else if (dbResult instanceof Iterator) {
				final JSONObject json = new JSONObject();
				final JSONArray elementArray = new JSONArray();
				List<String> columns;
				Iterator rs = (Iterator) dbResult;
				while (rs.hasNext()) {
					Object ele = rs.next();
					if (ele instanceof Row) {
						Row r = (Row) ele;
						columns = r.getColumnNames();
						Iterator iter = r.iterator();
						HashMap<String, Object> valMap = new HashMap<String, Object>();
						int pos = 0;
						while (iter.hasNext()) {
							Object o = iter.next();
							if (o instanceof Element) {
								o = GraphSONUtility.jsonFromElement(
										(Element) o, null, GraphSONMode.NORMAL);
							}
							valMap.put(columns.get(pos), o);
							pos++;
						}
						elementArray.put(valMap);
					} else if (ele instanceof Element) {
						elementArray.put(GraphSONUtility.jsonFromElement(
								(Element) ele, null, GraphSONMode.NORMAL));
					} else if (ele instanceof Result) {
						elementArray.put(GraphSONUtility.jsonFromElement(
								((Result) ele).getElement(), null,
								GraphSONMode.NORMAL));
					} else {
						elementArray.put(ele);
					}
				}
				hasMore = elementArray.length() >= pageSize ? true : false;
				result = elementArray;

			} else if (dbResult instanceof Element) {
				result = GraphSONUtility.jsonFromElement((Element) dbResult,
						null, GraphSONMode.NORMAL);
			} else {
				result = dbResult;
			}
			response.put("result", result);
			response.put("status", status);
			response.put("message", message);
			response.put("hasMore", hasMore);
			response.put("start", start);
			response.put("pageSize", pageSize);
			response.put("total", total);
			return ExtensionResponse.ok(response);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
		return null;
	}

	public void appendMsg(String string) {

		message += " \n " + string;
	}
}

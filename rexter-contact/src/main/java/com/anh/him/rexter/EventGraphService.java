package com.anh.him.rexter;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.SecurityContext;

import org.apache.log4j.Logger;

import com.anh.him.rexter.model.SGeoLocationEdge;
import com.anh.him.rexter.model.SchemaFactory;
import com.anh.him.rexter.util.HIMGraphConstant;
import com.anh.him.rexter.util.HIMGraphMessageConstant;
import com.thinkaurelius.titan.core.TitanEdge;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.rexster.extension.AbstractRexsterExtension;
import com.tinkerpop.rexster.extension.ExtensionDefinition;
import com.tinkerpop.rexster.extension.ExtensionDescriptor;
import com.tinkerpop.rexster.extension.ExtensionNaming;
import com.tinkerpop.rexster.extension.ExtensionPoint;
import com.tinkerpop.rexster.extension.ExtensionRequestParameter;
import com.tinkerpop.rexster.extension.ExtensionResponse;
import com.tinkerpop.rexster.extension.HttpMethod;
import com.tinkerpop.rexster.extension.RexsterContext;

@ExtensionNaming(namespace = AbstractExtension.EXTENSION_NAMESPACE, name = EventGraphService.EXTENSION_NAME)
public class EventGraphService extends AbstractRexsterExtension implements
		AbstractExtension {
	public static final String EXTENSION_NAME = "event";
	private static Logger logger = Logger.getLogger(EventGraphService.class);

	@ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH, method = HttpMethod.POST, path = "createOrUpdate", produces = "application/json")
	@ExtensionDescriptor(description = "register or update an event")
	public ExtensionResponse createOrUpdateEvent(
			@RexsterContext SecurityContext securityContext,
			@RexsterContext Graph g,
			@RexsterContext HttpServletRequest request,
			@ExtensionRequestParameter(name = "eventCode", description = "event Code") String eventCode,
			@ExtensionRequestParameter(name = "himId", description = "HIM registration Id") String himId,
			@ExtensionRequestParameter(name = "himType", description = "store or user") String himType,

			@ExtensionRequestParameter(name = "eventName", description = "event name") String eventName,
			@ExtensionRequestParameter(name = "eventType", description = "event type") String eventType,
			@ExtensionRequestParameter(name = "tags", description = "event tags") String tags,
			@ExtensionRequestParameter(name = "eventCategory", description = "event category") String eventCategory,
			@ExtensionRequestParameter(name = "startDate", description = "event start date") String startDate,
			@ExtensionRequestParameter(name = "endDate", description = "event end date") String endDate,

			@ExtensionRequestParameter(name = "street", description = "street") String street,
			@ExtensionRequestParameter(name = "city", description = "city") String city,
			@ExtensionRequestParameter(name = "pincode", description = "pincode") Integer pincode,
			@ExtensionRequestParameter(name = "country", description = "country") String country,
			@ExtensionRequestParameter(name = "lon", description = "longitude") Float lon,
			@ExtensionRequestParameter(name = "lat", description = "latitude") Float lat) {
		ControllerResponse response = new ControllerResponse();
		TitanGraph graph = (TitanGraph) g;
		/*
		 * response = HimAuthenticationFilter.isAdminUser(null,
		 * securityContext); if (response != null) { return response.prepare();
		 * } else { response = new ControllerResponse(); }
		 */
		if (null == eventCode) {
			response.setMessage(HIMGraphMessageConstant.MISSING_SERVICE);
			response.setStatus(BAD_INPUT);
			logger.error(HIMGraphMessageConstant.MISSING_SERVICE);
			return response.prepare();
		}
		Iterator<Vertex> event = graph.getVertices(HIMGraphConstant.ID,
				eventCode).iterator();
		Vertex eventVertex = null;
		if (!event.hasNext()) {
			eventVertex = graph.addVertex(null);
		} else {
			eventVertex = event.next();
		}
		eventVertex.setProperty(HIMGraphConstant.EVENT_CODE, eventCode);
		eventVertex.setProperty(HIMGraphConstant.ID, eventCode);
		eventVertex.setProperty(HIMGraphConstant.HIM_ID, himId);
		eventVertex.setProperty(HIMGraphConstant.BTYPE, himType);

		if (null != eventName)
			eventVertex.setProperty(HIMGraphConstant.NAME, eventName);
		if (null != tags)
			eventVertex.setProperty(HIMGraphConstant.TAGS, tags);
		if (null != eventType)
			eventVertex.setProperty(HIMGraphConstant.EVENT_TYPE, eventType);
		if (null != eventCategory)
			eventVertex.setProperty(HIMGraphConstant.EVENT_CATEGORY,
					eventCategory);

		eventVertex.setProperty(HIMGraphConstant.DATE_OF_JOINING, new Date());
		eventVertex.setProperty(HIMGraphConstant.VERSION,
				System.currentTimeMillis());

		SGeoLocationEdge edge = null;
		try {
			List vinfo = new ArrayList();
			edge = new SGeoLocationEdge();
			Iterator<Vertex> es = graph.query().has(HIMGraphConstant.ID, himId)
					.limit(1).vertices().iterator();
			Vertex loc = null;
			if (es.hasNext()) {
				loc = es.next();
				vinfo.add(loc);
			}
			if (loc == null) {
				// throw error
				response.setMessage(HIMGraphMessageConstant.MISSING_STORE);
				response.setStatus(BAD_INPUT);
				logger.error(HIMGraphMessageConstant.MISSING_STORE);
				return response.prepare();
			}
			TitanEdge e = null;
			try {
				GremlinPipeline pipe = new GremlinPipeline();
				Iterator iter = pipe.start(eventVertex)
						.bothE(SchemaFactory.E_EVENTS).as("e").bothV()
						.retain(vinfo).back("e").iterator();
				if (iter.hasNext()) {
					e = (TitanEdge) iter.next();
				} else {
					e = (TitanEdge) graph.addEdge(null, eventVertex, loc,
							SchemaFactory.E_EVENTS);
				}
			} catch (Exception exp) {
				logger.error(HIMGraphMessageConstant.EDGE_ALREADY_EXISTS);
			}

			edge.setEdge(e);
			if (null != city)
				edge.setCity(city);
			if (null != loc.getProperty(HIMGraphConstant.COUNTRY))
				edge.setCountry(loc.getProperty(HIMGraphConstant.COUNTRY)
						.toString());
			edge.setDoj(new java.util.Date());
			if (null != tags)
				edge.setEtags(tags);
			if (null != eventName)
				edge.setName(eventName);
			if (null != pincode)
				edge.setPincode(pincode);
			edge.setLabel(SchemaFactory.E_EVENTS);
			if (null != eventType)
				edge.setBtype(eventType);
			if (null != lat && null != lon) {
				if (Geoshape.isValidCoordinate(lat, lon))
					edge.setLoc(Geoshape.point(lat, lon));
			}
			edge.prepare();
			response.setDbResult(e);
		} catch (Exception e) {
			logger.error(HIMGraphMessageConstant.UNKNOWN_ERROR + " : "
					+ e.getMessage());
			response.setMessage(HIMGraphMessageConstant.UNKNOWN_ERROR);
			response.setStatus(BAD_INPUT);
			return response.prepare();
		}
		return response.prepare();
	}

	@ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH, method = HttpMethod.POST, path = "update", produces = "application/json")
	@ExtensionDescriptor(description = "register or update business")
	public ExtensionResponse updateBusiness(
			@RexsterContext SecurityContext securityContext,
			@RexsterContext Graph g,
			@RexsterContext HttpServletRequest request,
			@ExtensionRequestParameter(name = "eventCode", description = "event Code") String eventCode,
			@ExtensionRequestParameter(name = "himId", description = "him Id") String himId,
			@ExtensionRequestParameter(name = "eventName", description = "event name") String eventName,
			@ExtensionRequestParameter(name = "eventType", description = "event type") String eventType,
			@ExtensionRequestParameter(name = "eventCategory", description = "event category") String eventCategory,
			@ExtensionRequestParameter(name = "status", description = "status") Integer status,
			@ExtensionRequestParameter(name = "rating", description = "rating") Float rating,
			@ExtensionRequestParameter(name = "followers", description = "followers") Long followers,
			@ExtensionRequestParameter(name = "tags", description = "tags") String tags,
			@ExtensionRequestParameter(name = "likes", description = "likes") Long likes) {
		ControllerResponse response = new ControllerResponse();
		TitanGraph graph = (TitanGraph) g;
		/*
		 * response = HimAuthenticationFilter.isAdminUser(null,
		 * securityContext); if (response != null) { return response.prepare();
		 * } else { response = new ControllerResponse(); }
		 */
		if (null == eventCode) {
			response.setMessage(HIMGraphMessageConstant.MISSING_SERVICE);
			response.setStatus(BAD_INPUT);
			logger.error(HIMGraphMessageConstant.MISSING_SERVICE);
			return response.prepare();
		}
		Iterator<Vertex> meIter = graph.getVertices(HIMGraphConstant.ID,
				eventCode).iterator();
		Vertex meV = meIter.next();
		if (meV == null) {
			response.setMessage(HIMGraphMessageConstant.CIRCLE_NOT_EXIST);
			response.setStatus(BAD_INPUT);
			logger.error(HIMGraphMessageConstant.CIRCLE_NOT_EXIST);
			return response.prepare();
		}

		if (null != eventName)
			meV.setProperty(HIMGraphConstant.NAME, eventName);
		if (null != tags)
			meV.setProperty(HIMGraphConstant.TAGS, tags);
		if (null != eventType)
			meV.setProperty(HIMGraphConstant.EVENT_TYPE, eventType);
		if (null != eventCategory)
			meV.setProperty(HIMGraphConstant.EVENT_CATEGORY, eventCategory);
		if (null != status)
			meV.setProperty(HIMGraphConstant.STATUS, status);
		meV.setProperty(HIMGraphConstant.DATE_OF_JOINING, new Date());
		meV.setProperty(HIMGraphConstant.VERSION, System.currentTimeMillis());
		if (null != rating)
			meV.setProperty(HIMGraphConstant.RATING, rating);
		if (null != followers)
			meV.setProperty(HIMGraphConstant.FOLLOWERS, followers);

		SGeoLocationEdge edge = null;
		try {
			List vinfo = new ArrayList();
			edge = new SGeoLocationEdge();
			Iterator<Vertex> es = graph.query().has(HIMGraphConstant.ID, himId)
					.limit(1).vertices().iterator();
			Vertex loc = null;
			if (es.hasNext()) {
				loc = es.next();
				vinfo.add(loc);
			}
			if (loc == null) {
				// throw error
				response.setMessage(HIMGraphMessageConstant.CIRCLE_NOT_EXIST);
				response.setStatus(BAD_INPUT);
				logger.error(HIMGraphMessageConstant.CIRCLE_NOT_EXIST);
				return response.prepare();
			}
			TitanEdge e = null;
			try {
				GremlinPipeline pipe = new GremlinPipeline();
				Iterator iter = pipe.start(meV).bothE(SchemaFactory.E_EVENTS)
						.as("e").bothV().retain(vinfo).back("e").iterator();
				if (iter.hasNext()) {
					e = (TitanEdge) iter.next();
				}
				if (e == null) {
					// throw error
					response.setMessage(HIMGraphMessageConstant.RELATION_NOT_EXIST);
					response.setStatus(BAD_INPUT);
					logger.error(HIMGraphMessageConstant.RELATION_NOT_EXIST);
					return response.prepare();
				}

			} catch (Exception exp) {
				logger.error(HIMGraphMessageConstant.EDGE_ALREADY_EXISTS);
			}

			edge.setEdge(e);
			if (null != tags)
				edge.setEtags(tags);
			if (null != eventName)
				edge.setName(eventName);
			if (null != followers)
				edge.setFollowers(followers);
			if (null != rating)
				edge.setRating(rating);
			if (null != status)
				edge.setStatus(status);
			if (null != likes)
				edge.setLikes(likes);
			if (null != eventType)
				edge.setBtype(eventType);
			edge.prepare();
			response.setDbResult(e);
		} catch (Exception e) {
			logger.error(HIMGraphMessageConstant.UNKNOWN_ERROR + " : "
					+ e.getMessage());
			response.setMessage(HIMGraphMessageConstant.UNKNOWN_ERROR);
			response.setStatus(BAD_INPUT);
			return response.prepare();
		}
		return response.prepare();
	}

}
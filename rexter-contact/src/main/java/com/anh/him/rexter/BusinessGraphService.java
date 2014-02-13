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

@ExtensionNaming(namespace = AbstractExtension.EXTENSION_NAMESPACE, name = BusinessGraphService.EXTENSION_NAME)
public class BusinessGraphService extends AbstractRexsterExtension implements
		AbstractExtension {
	public static final String EXTENSION_NAME = "circle";
	private static Logger logger = Logger.getLogger(BusinessGraphService.class);

	@ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH, method = HttpMethod.POST, path = "registerOrUpdate", produces = "application/json")
	@ExtensionDescriptor(description = "register or update business")
	public ExtensionResponse registerOrUpdateBusiness(
			@RexsterContext SecurityContext securityContext,
			@RexsterContext Graph g,
			@RexsterContext HttpServletRequest request,
			@ExtensionRequestParameter(name = "serviceId", description = "service id") String serviceId,
			@ExtensionRequestParameter(name = "serviceName", description = "service name") String serviceName,
			@ExtensionRequestParameter(name = "btype", description = "business type") String bType,
			@ExtensionRequestParameter(name = "bcategory", description = "business category") String bCategory,
			@ExtensionRequestParameter(name = "pincode", description = "pincode") Integer pincode,
			@ExtensionRequestParameter(name = "state", description = "state") String state,
			@ExtensionRequestParameter(name = "city", description = "city") String city,
			@ExtensionRequestParameter(name = "area", description = "area") String area,
			@ExtensionRequestParameter(name = "doj", description = "doj") String doj,
			@ExtensionRequestParameter(name = "status", description = "status") Integer status,
			@ExtensionRequestParameter(name = "rating", description = "rating") Float rating,
			@ExtensionRequestParameter(name = "followers", description = "followers") Long followers,
			@ExtensionRequestParameter(name = "ver", description = "ver") String ver,
			@ExtensionRequestParameter(name = "tags", description = "tags") String tags,
			@ExtensionRequestParameter(name = "address", description = "address") String address,
			@ExtensionRequestParameter(name = "likes", description = "likes") Long likes,
			@ExtensionRequestParameter(name = "lon", description = "longitude") Float lon,
			@ExtensionRequestParameter(name = "lat", description = "latitude") Float lat) {
		ControllerResponse response = new ControllerResponse();
		TitanGraph graph = (TitanGraph) g;
		/*
		 * response = HimAuthenticationFilter.isAdminUser(null,
		 * securityContext); if (response != null) { return response.prepare();
		 * } else { response = new ControllerResponse(); }
		 */
		if (null == serviceId) {
			response.setMessage(HIMGraphMessageConstant.MISSING_SERVICE);
			response.setStatus(BAD_INPUT);
			logger.error(HIMGraphMessageConstant.MISSING_SERVICE);
			return response.prepare();
		}
		Iterator<Vertex> meIter = graph.getVertices(HIMGraphConstant.ID,
				serviceId).iterator();
		Vertex meV = null;
		if (!meIter.hasNext()) {
			meV = graph.addVertex(null);
		} else {
			meV = meIter.next();
		}
		meV.setProperty(HIMGraphConstant.ID, serviceId);

		if (null != serviceName)
			meV.setProperty(HIMGraphConstant.NAME, serviceName);
		if (null != tags)
			meV.setProperty(HIMGraphConstant.TAGS, tags);
		if (null != bType)
			meV.setProperty(HIMGraphConstant.BTYPE, bType);
		if (null != bCategory)
			meV.setProperty(HIMGraphConstant.BCATEGORY, bCategory);
		if (null != serviceId)
			meV.setProperty(HIMGraphConstant.SERVICE_ID, serviceId);
		if (null != serviceName)
			meV.setProperty(HIMGraphConstant.SERVICE_NAME, serviceName);
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
			Iterator<Vertex> es = graph.query().has("czone", pincode / 1000)
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
				Iterator iter = pipe.start(meV)
						.bothE(SchemaFactory.E_BUSINESS_LOC_EDGE).as("e")
						.bothV().retain(vinfo).back("e").iterator();
				if (iter.hasNext()) {
					e = (TitanEdge) iter.next();
				} else {
					e = (TitanEdge) graph.addEdge(null, meV, loc,
							SchemaFactory.E_BUSINESS_LOC_EDGE);
				}
			} catch (Exception exp) {
				logger.error(HIMGraphMessageConstant.EDGE_ALREADY_EXISTS);
			}

			edge.setEdge(e);
			edge.setId(serviceId);
			if (area != null)
				edge.setArea(area);
			if (null != loc.getProperty(HIMGraphConstant.CID))
				edge.setCid(loc.getProperty(HIMGraphConstant.CID).toString());
			if (null != city)
				edge.setCity(city);
			if (null != loc.getProperty(HIMGraphConstant.COUNTRY))
				edge.setCountry(loc.getProperty(HIMGraphConstant.COUNTRY)
						.toString());
			edge.setDoj(new java.util.Date());
			if (null != tags)
				edge.setEtags(tags);
			if (null != serviceName)
				edge.setName(serviceName);
			if (null != pincode)
				edge.setPincode(pincode);

			if (null != state)
				edge.setState(state);
			if (null != followers)
				edge.setFollowers(followers);
			if (null != rating)
				edge.setRating(rating);
			edge.setLabel(SchemaFactory.E_BUSINESS_LOC_EDGE);
			if (null != status)
				edge.setStatus(status);
			if (null != likes)
				edge.setLikes(likes);
			if (null != address)
				edge.setHouse(address);
			if (null != bType)
				edge.setBtype(bType);
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
			@ExtensionRequestParameter(name = "serviceId", description = "service id") String serviceId,
			@ExtensionRequestParameter(name = "serviceName", description = "service name") String serviceName,
			@ExtensionRequestParameter(name = "btype", description = "business type") String bType,
			@ExtensionRequestParameter(name = "bcategory", description = "business category") String bCategory,
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
		if (null == serviceId) {
			response.setMessage(HIMGraphMessageConstant.MISSING_SERVICE);
			response.setStatus(BAD_INPUT);
			logger.error(HIMGraphMessageConstant.MISSING_SERVICE);
			return response.prepare();
		}
		Iterator<Vertex> meIter = graph.getVertices(HIMGraphConstant.ID,
				serviceId).iterator();
		Vertex meV = meIter.next();
		if (meV == null) {
			response.setMessage(HIMGraphMessageConstant.CIRCLE_NOT_EXIST);
			response.setStatus(BAD_INPUT);
			logger.error(HIMGraphMessageConstant.CIRCLE_NOT_EXIST);
			return response.prepare();
		}

		if (null != serviceName)
			meV.setProperty(HIMGraphConstant.NAME, serviceName);
		if (null != tags)
			meV.setProperty(HIMGraphConstant.TAGS, tags);
		if (null != bType)
			meV.setProperty(HIMGraphConstant.BTYPE, bType);
		if (null != bCategory)
			meV.setProperty(HIMGraphConstant.BCATEGORY, bCategory);
		if (null != serviceId)
			meV.setProperty(HIMGraphConstant.SERVICE_ID, serviceId);
		if (null != serviceName)
			meV.setProperty(HIMGraphConstant.SERVICE_NAME, serviceName);
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
			Iterator<Vertex> es = graph.query()
					.has(HIMGraphConstant.ID, serviceId).limit(1).vertices()
					.iterator();
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
				Iterator iter = pipe.start(meV)
						.bothE(SchemaFactory.E_BUSINESS_LOC_EDGE).as("e")
						.bothV().retain(vinfo).back("e").iterator();
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
			if (null != bCategory)
				edge.setCategory(bCategory);
			if (null != serviceId)
				edge.setCategory(serviceId);
			if (null != tags)
				edge.setEtags(tags);
			if (null != serviceName)
				edge.setName(serviceName);
			if (null != followers)
				edge.setFollowers(followers);
			if (null != rating)
				edge.setRating(rating);
			if (null != status)
				edge.setStatus(status);
			if (null != likes)
				edge.setLikes(likes);
			if (null != bType)
				edge.setBtype(bType);
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

	@ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH, method = HttpMethod.POST, path = "associateBusiness", produces = "application/json")
	@ExtensionDescriptor(description = "register or update business")
	public ExtensionResponse associateBusiness(
			@RexsterContext SecurityContext securityContext,
			@RexsterContext Graph g,
			@RexsterContext HttpServletRequest request,
			@ExtensionRequestParameter(name = "fromId", description = "from Business Id") String fromId,
			@ExtensionRequestParameter(name = "toId", description = "toBusiness Id") Long toId,
			@ExtensionRequestParameter(name = "relation", description = "relation") String relation,
			@ExtensionRequestParameter(name = "tags", description = "tags") String tags) {
		ControllerResponse response = new ControllerResponse();
		TitanGraph graph = (TitanGraph) g;
		/*
		 * response = HimAuthenticationFilter.isAdminUser(null,
		 * securityContext); if (response != null) { return response.prepare();
		 * } else { response = new ControllerResponse(); }
		 */
		if (null == toId || null == fromId) {
			response.setMessage(HIMGraphMessageConstant.MISSING_SERVICE);
			response.setStatus(BAD_INPUT);
			logger.error(HIMGraphMessageConstant.MISSING_SERVICE);
			return response.prepare();
		}
		Iterator<Vertex> meIter = graph
				.getVertices(HIMGraphConstant.ID, fromId).iterator();
		Vertex meV = null;
		if (!meIter.hasNext()) {
			response.setMessage(HIMGraphMessageConstant.CIRCLE_NOT_EXIST);
			response.setStatus(BAD_INPUT);
			logger.error(HIMGraphMessageConstant.CIRCLE_NOT_EXIST);
			return response.prepare();
		}
		meV = meIter.next();

		meIter = graph.getVertices(HIMGraphConstant.ID, toId).iterator();
		Vertex loc = null;
		if (!meIter.hasNext()) {
			response.setMessage(HIMGraphMessageConstant.CIRCLE_NOT_EXIST);
			response.setStatus(BAD_INPUT);
			logger.error(HIMGraphMessageConstant.CIRCLE_NOT_EXIST);
			return response.prepare();
		}
		loc = meIter.next();

		SGeoLocationEdge edge = null;
		try {
			edge = new SGeoLocationEdge();

			TitanEdge e = null;
			try {
				e = (TitanEdge) graph.addEdge(null, meV, loc,
						SchemaFactory.E_BUSINESS_ASC_EDGE);
			} catch (Exception exp) {
				logger.error(HIMGraphMessageConstant.EDGE_ALREADY_EXISTS);
				response.setMessage(HIMGraphMessageConstant.EDGE_ALREADY_EXISTS);
				response.setStatus(BAD_INPUT);
				return response.prepare();
			}
			edge.setEdge(e);
			edge.setDoj(new java.util.Date());
			if (null != tags)
				edge.setEtags(tags);
			if (null != relation)
				edge.setName(relation);
			edge.setLabel(SchemaFactory.E_BUSINESS_ASC_EDGE);
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
package com.anh.him.rexter;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.SecurityContext;

import org.apache.log4j.Logger;

import com.anh.him.rexter.model.SGeoLocationEdge;
import com.anh.him.rexter.util.HIMGraphConstant;
import com.anh.him.rexter.util.HIMGraphMessageConstant;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanGraphQuery;
import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Contains;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.rexster.extension.AbstractRexsterExtension;
import com.tinkerpop.rexster.extension.ExtensionDefinition;
import com.tinkerpop.rexster.extension.ExtensionDescriptor;
import com.tinkerpop.rexster.extension.ExtensionNaming;
import com.tinkerpop.rexster.extension.ExtensionPoint;
import com.tinkerpop.rexster.extension.ExtensionRequestParameter;
import com.tinkerpop.rexster.extension.ExtensionResponse;
import com.tinkerpop.rexster.extension.HttpMethod;
import com.tinkerpop.rexster.extension.RexsterContext;

@ExtensionNaming(namespace = AbstractExtension.EXTENSION_NAMESPACE, name = SearchGraphService.EXTENSION_NAME)
public class SearchGraphService extends AbstractRexsterExtension implements
		AbstractExtension {
	public static final String EXTENSION_NAME = "search";
	private static Logger logger = Logger.getLogger(SearchGraphService.class);

	@ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH, method = HttpMethod.POST, path = "business", produces = "application/json")
	@ExtensionDescriptor(description = "register or update business")
	public ExtensionResponse registerOrUpdateBusiness(
			@RexsterContext SecurityContext securityContext,
			@RexsterContext Graph g,
			@RexsterContext HttpServletRequest request,
			@ExtensionRequestParameter(name = "btype", description = "business type") String bType,
			@ExtensionRequestParameter(name = "state", description = "state") String state,
			@ExtensionRequestParameter(name = "city", description = "city") String city,
			@ExtensionRequestParameter(name = "pincode", description = "pincode") String pincode,
			@ExtensionRequestParameter(name = "q", description = "query String") String q) {
		ControllerResponse response = new ControllerResponse();
		TitanGraph graph = (TitanGraph) g;
		/*
		 * response = HimAuthenticationFilter.isAdminUser(null,
		 * securityContext); if (response != null) { return response.prepare();
		 * } else { response = new ControllerResponse(); }
		 */
		if (null == bType) {
			logger.error(HIMGraphMessageConstant.MISSING_SERVICE);
			response.setMessage(HIMGraphMessageConstant.MISSING_SERVICE);
			response.setStatus(BAD_INPUT);
			return response.prepare();
		}
		List<SGeoLocationEdge> edges = new ArrayList<SGeoLocationEdge>();
		SGeoLocationEdge edge = null;
		try {
			TitanGraphQuery query = graph.query().has(HIMGraphConstant.BTYPE,
					bType);
			if (null != q)
				query.has(HIMGraphConstant.ETAGS, Text.CONTAINS, q);
			if (null != pincode)
				query.has(HIMGraphConstant.PINCODE, pincode);
			if (null != state)
				query.has(HIMGraphConstant.STATE, state);
			if (null != city)
				query.has(HIMGraphConstant.CITY, city);

			Iterator<Edge> es = query.edges().iterator();
			// while (es.hasNext()) {
			// Edge ej = es.next();
			// edge = new SGeoLocationEdge();
			// edge.setEdge(ej);
			// if (null != ej.getProperty(HIMGraphConstant.DATE_OF_JOINING))
			// edge.setDoj((Date) ej
			// .getProperty(HIMGraphConstant.DATE_OF_JOINING));
			// if (null != ej.getProperty(HIMGraphConstant.STATUS))
			// edge.setStatus(Integer.getInteger(ej.getProperty(
			// HIMGraphConstant.STATUS).toString()));
			// if (null != ej.getProperty(HIMGraphConstant.CITY))
			// edge.setCity((ej.getProperty(HIMGraphConstant.CITY)
			// .toString()));
			// if (null != ej.getProperty(HIMGraphConstant.AREA))
			// edge.setArea((ej.getProperty(HIMGraphConstant.AREA)
			// .toString()));
			// if (null != ej.getProperty(HIMGraphConstant.FOLLOWERS))
			// edge.setFollowers((Long.getLong(ej.getProperty(
			// HIMGraphConstant.FOLLOWERS).toString())));
			// if (null != ej.getProperty(HIMGraphConstant.LIKES))
			// edge.setLikes((Long.getLong(ej.getProperty(
			// HIMGraphConstant.LIKES).toString())));
			// if (null != ej.getProperty(HIMGraphConstant.NAME))
			// edge.setName((ej.getProperty(HIMGraphConstant.NAME)
			// .toString()));
			// if (null != ej.getProperty(HIMGraphConstant.STATE))
			// edge.setState((ej.getProperty(HIMGraphConstant.STATE)
			// .toString()));
			// if (null != ej.getProperty(HIMGraphConstant.PINCODE))
			// edge.setPincode((Integer.getInteger(ej.getProperty(
			// HIMGraphConstant.PINCODE).toString())));
			// if (null != ej.getProperty(HIMGraphConstant.RATING))
			// edge.setRating((Float.valueOf(ej.getProperty(
			// HIMGraphConstant.RATING).toString())));
			//
			// edges.add(edge);
			// }
			response.setDbResult(es);
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
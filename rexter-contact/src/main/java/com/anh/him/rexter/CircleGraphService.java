package com.anh.him.rexter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.SecurityContext;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import com.anh.him.rexter.auth.HimAuthenticationFilter;
import com.anh.him.rexter.auth.HimAuthenticationFilter.AuthenticationPrincipal;
import com.anh.him.rexter.auth.HimAuthenticationFilter.User;
import com.anh.him.rexter.model.SFriendEdge;
import com.anh.him.rexter.model.SFriendEdge.EnumFriendshipStatus;
import com.anh.him.rexter.model.SUserNode;
import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.util.structures.Pair;
import com.tinkerpop.pipes.util.structures.Row;
import com.tinkerpop.rexster.extension.AbstractRexsterExtension;
import com.tinkerpop.rexster.extension.ExtensionDefinition;
import com.tinkerpop.rexster.extension.ExtensionDescriptor;
import com.tinkerpop.rexster.extension.ExtensionNaming;
import com.tinkerpop.rexster.extension.ExtensionPoint;
import com.tinkerpop.rexster.extension.ExtensionRequestParameter;
import com.tinkerpop.rexster.extension.ExtensionResponse;
import com.tinkerpop.rexster.extension.HttpMethod;
import com.tinkerpop.rexster.extension.RexsterContext;

@ExtensionNaming(namespace = AbstractExtension.EXTENSION_NAMESPACE, name = CircleGraphService.EXTENSION_NAME)
public class CircleGraphService extends AbstractRexsterExtension implements
		AbstractExtension {
	public static final String EXTENSION_NAME = "circle";
	private static Logger logger = Logger.getLogger(CircleGraphService.class);

	
	@ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH, path = "add")
	@ExtensionDescriptor(description = "invite friend")
	public ExtensionResponse inviteContact(
			@RexsterContext SecurityContext securityContext,
			@RexsterContext Graph g,
			@RexsterContext HttpServletRequest request,
			@ExtensionRequestParameter(name = "me", description = "my id") String me,
			@ExtensionRequestParameter(name = "fid", description = "friend") String fid) {
		ControllerResponse response = new ControllerResponse();
		TitanGraph graph = (TitanGraph) g;
		HimAuthenticationFilter.isAuthorizedUser(me, securityContext);
		if (me == null || me.isEmpty() || fid == null || fid.isEmpty()) {
			response.setStatus(400);
			response.setMessage("me or fid is missing in request");
			return response.prepare();
		}

		Iterator<Vertex> meIter = graph.getVertices("__id", me).iterator();
		Iterator<Vertex> fIter = graph.getVertices("__id", fid).iterator();
		if (!meIter.hasNext() || !fIter.hasNext()) {
			response.setStatus(400);
			response.setMessage("friend doesn't exist in the system.");
			return response.prepare();
		}
		// GremlinGroovyScriptEngine engine = new GremlinGroovyScriptEngine();
		// javax.script.Bindings binding = engine.createBindings();
		Vertex meV = meIter.next();
		Vertex friendV = fIter.next();
		// binding.put("g", graph);
		// binding.put("v1", meV);
		// binding.put("fid", fid);
		// Boolean out = new Boo;
		List flist = new ArrayList();
		flist.add(friendV);
		Edge frnd = null;
		try {
			GremlinPipeline pipe = new GremlinPipeline();
			Iterator iter = pipe.start(meV).bothE("friend").as("e").bothV()
					.retain(flist).back("e").iterator();
			frnd = (Edge) iter.next();

		} catch (Exception e) {
			e.printStackTrace();
		}
		if (frnd != null) {
			response.setStatus(400);
			response.setMessage("already in friendship status "
					+ frnd.getProperty("status"));
			return response.prepare();
		}
		Edge e = meV.addEdge("friend", friendV);
		SFriendEdge friendship = new SFriendEdge(e);
		friendship.setFndStatus(1, 3, -1);
		friendship.setOrigStatus(1, 3, -1);
		friendship.setStatus(SFriendEdge.EnumFriendshipStatus.INVITED
				.getStatus());
		friendship.setInitiator(me);
		e = friendship.prepare();
		response.setDbResult(e);

		return response.prepare();
	}



}
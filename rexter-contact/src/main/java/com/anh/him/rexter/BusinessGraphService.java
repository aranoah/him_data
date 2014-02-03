package com.anh.him.rexter;

import org.apache.log4j.Logger;

import com.tinkerpop.rexster.extension.AbstractRexsterExtension;
import com.tinkerpop.rexster.extension.ExtensionNaming;

@ExtensionNaming(namespace = AbstractExtension.EXTENSION_NAMESPACE, name = BusinessGraphService.EXTENSION_NAME)
public class BusinessGraphService extends AbstractRexsterExtension implements
		AbstractExtension {
	public static final String EXTENSION_NAME = "circle";
	private static Logger logger = Logger.getLogger(BusinessGraphService.class);

//	
//	@ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH, path = "add")
//	@ExtensionDescriptor(description = "invite friend")
//	public ExtensionResponse inviteContact(
//			@RexsterContext SecurityContext securityContext,
//			@RexsterContext Graph g,
//			@RexsterContext HttpServletRequest request,
//			@ExtensionRequestParameter(name = "serviceId", description = "service id") String serviceId,
//			@ExtensionRequestParameter(name = "templateId", description = "templateId") String templateId,
//			@ExtensionRequestParameter(name = "pincode", description = "pincode") Integer pincode,
//			@ExtensionRequestParameter(name = "state", description = "state") String state,
//			@ExtensionRequestParameter(name = "city", description = "city") String city,
//			@ExtensionRequestParameter(name = "area", description = "area") String area,
//			@ExtensionRequestParameter(name = "tags", description = "tags") String[] tags,
//			@ExtensionRequestParameter(name = "lon", description = "longitude") Float lon,
//			@ExtensionRequestParameter(name = "lat", description = "latitude") Float lat
//			
//			) {
//		ControllerResponse response = new ControllerResponse();
//		TitanGraph graph = (TitanGraph) g;
//		HimAuthenticationFilter.isAuthorizedUser(me, securityContext);
//		if (me == null || me.isEmpty() || fid == null || fid.isEmpty()) {
//			response.setStatus(400);
//			response.setMessage("me or fid is missing in request");
//			return response.prepare();
//		}
//
//		Iterator<Vertex> meIter = graph.getVertices("__id", me).iterator();
//		Iterator<Vertex> fIter = graph.getVertices("__id", fid).iterator();
//		if (!meIter.hasNext() || !fIter.hasNext()) {
//			response.setStatus(400);
//			response.setMessage("friend doesn't exist in the system.");
//			return response.prepare();
//		}
//		// GremlinGroovyScriptEngine engine = new GremlinGroovyScriptEngine();
//		// javax.script.Bindings binding = engine.createBindings();
//		Vertex meV = meIter.next();
//		Vertex friendV = fIter.next();
//		// binding.put("g", graph);
//		// binding.put("v1", meV);
//		// binding.put("fid", fid);
//		// Boolean out = new Boo;
//		List flist = new ArrayList();
//		flist.add(friendV);
//		Edge frnd = null;
//		try {
//			GremlinPipeline pipe = new GremlinPipeline();
//			Iterator iter = pipe.start(meV).bothE("friend").as("e").bothV()
//					.retain(flist).back("e").iterator();
//			frnd = (Edge) iter.next();
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		if (frnd != null) {
//			response.setStatus(400);
//			response.setMessage("already in friendship status "
//					+ frnd.getProperty("status"));
//			return response.prepare();
//		}
//		Edge e = meV.addEdge("friend", friendV);
//		SFriendEdge friendship = new SFriendEdge(e);
//		friendship.setFndStatus(1, 3, -1);
//		friendship.setOrigStatus(1, 3, -1);
//		friendship.setStatus(SFriendEdge.EnumFriendshipStatus.INVITED
//				.getStatus());
//		friendship.setInitiator(me);
//		e = friendship.prepare();
//		response.setDbResult(e);
//
//		return response.prepare();
//	}



}
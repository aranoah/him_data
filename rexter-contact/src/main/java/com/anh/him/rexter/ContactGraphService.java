package com.anh.him.rexter;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.util.Version;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.anh.him.rexter.auth.HimAuthenticationFilter;
import com.anh.him.rexter.auth.HimAuthenticationFilter.AuthenticationPrincipal;
import com.anh.him.rexter.auth.HimAuthenticationFilter.User;
import com.anh.him.rexter.model.SFriendEdge;
import com.anh.him.rexter.model.SFriendEdge.EnumFriendshipStatus;
import com.anh.him.rexter.model.SGeoLocationEdge;
import com.anh.him.rexter.model.SUserNode;
import com.anh.him.rexter.model.SchemaFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanIndexQuery.Result;
import com.thinkaurelius.titan.core.TitanKey;
import com.thinkaurelius.titan.core.TitanLabel;
import com.thinkaurelius.titan.core.TitanVertex;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import com.thinkaurelius.titan.diskstorage.StorageException;
import com.thinkaurelius.titan.diskstorage.indexing.IndexTransaction;
import com.thinkaurelius.titan.graphdb.database.StandardTitanGraph;
import com.thinkaurelius.titan.graphdb.transaction.StandardTitanTx;
import com.thinkaurelius.titan.util.encoding.LongEncoding;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
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

@ExtensionNaming(namespace = AbstractExtension.EXTENSION_NAMESPACE, name = ContactGraphService.EXTENSION_NAME)
public class ContactGraphService extends AbstractRexsterExtension implements
		AbstractExtension {
	public static final String EXTENSION_NAME = "socio";
	private static Logger logger = Logger.getLogger(ContactGraphService.class);

	@ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH, method = HttpMethod.POST, path = "index", produces = "application/json")
	@ExtensionDescriptor(description = "Create schema & indexing")
	public ExtensionResponse index(
			@RexsterContext SecurityContext securityContext,
			@ExtensionRequestParameter(name = "schema", defaultValue = "true") Boolean schema,
			@ExtensionRequestParameter(name = "reindex", defaultValue = "false") Boolean reindex,
			@ExtensionRequestParameter(name = "delete", defaultValue = "false") Boolean delete,
			@RexsterContext Graph g, @RexsterContext HttpServletRequest request) {

		ControllerResponse response = new ControllerResponse();
		TitanGraph graph = (TitanGraph) g;

		if (delete) {
			Iterator iter = graph.getVertices().iterator();
			int i = 0;
			while (iter.hasNext()) {
				graph.removeVertex((Vertex) iter.next());
				if (i++ > 100) {
					i = 0;
					graph.commit();
				}
			}
			iter = graph.getEdges().iterator();
			i = 0;
			while (iter.hasNext()) {
				graph.removeEdge((Edge) iter.next());
				if (i++ > 100) {
					i = 0;
					graph.commit();
				}
			}
		} else if (reindex) {
			Iterator<Vertex> iter = graph.getVertices().iterator();
			IndexTransaction v = ((StandardTitanTx) ((StandardTitanGraph) graph)
					.getCurrentThreadTx()).getTxHandle()
					.getIndexTransactionHandle("search");
			int i = 00;
			String encode = LongEncoding.encode((Long) graph
					.getType("cpincode").getId());
			while (iter.hasNext()) {
				Vertex x = iter.next();
				String type = x.getProperty("type");
				if (type != null && type.equals("circle")) {

					v.add("vertex", LongEncoding.encode((Long) x.getId()),
							encode, (Integer) x.getProperty("cpincode"), true);
				}
				try {
					if (i++ > 200) {
						v.flush();
						i = 0;
					}
				} catch (StorageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (schema)
			SchemaFactory.createIndex(graph, 1);
		return response.prepare();
	}

	@ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH, method = HttpMethod.POST, path = "indexes", produces = "application/json")
	@ExtensionDescriptor(description = "getIndex definition")
	public ExtensionResponse getIndexes(
			@ExtensionRequestParameter(name = "version") Integer version,
			@RexsterContext SecurityContext securityContext,
			@RexsterContext Graph g, @RexsterContext HttpServletRequest request)
			throws Exception {

		ControllerResponse response = new ControllerResponse();
		TitanGraph graph = (TitanGraph) g;

		Iterator<TitanLabel> labels = graph.getTypes(TitanLabel.class)
				.iterator();
		JSONArray larrays = new JSONArray();
		TitanLabel ll = null;
		while (labels.hasNext()) {
			;
			ll = labels.next();
			larrays.put(ll.getName());
		}
		JSONObject result = new JSONObject();
		result.put("labels", larrays);
		response.setDbResult(result);
		Iterator<TitanKey> iter = graph.getTypes(TitanKey.class).iterator();
		TitanKey tk = null;
		JSONObject allKeys = new JSONObject();
		String key;
		while (iter.hasNext()) {
			tk = iter.next();
			try {
				key = LongEncoding.encode((Long) tk.getId());
			} catch (Exception e) {
				key = tk.getName();
			}
			allKeys.put(tk.getName(), key);
		}
		result.put("allkeys", allKeys);
		return response.prepare();
	}

	@ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH, method = HttpMethod.POST, path = "remove-index", produces = "application/json")
	@ExtensionDescriptor(description = "Create schema & indexing")
	public ExtensionResponse remove(
			@RexsterContext SecurityContext securityContext,
			@RexsterContext Graph g, @RexsterContext HttpServletRequest request) {

		ControllerResponse response = new ControllerResponse();
		TitanGraph graph = (TitanGraph) g;

		Iterator iter = graph.getVertices().iterator();
		int i = 0;
		while (iter.hasNext()) {
			graph.removeVertex((Vertex) iter.next());
			if (i++ > 100) {
				i = 0;
				graph.commit();
			}
		}
		iter = graph.getEdges().iterator();
		i = 0;
		while (iter.hasNext()) {
			graph.removeEdge((Edge) iter.next());
			if (i++ > 100) {
				i = 0;
				graph.commit();
			}
		}

		return response.prepare();
	}

	@ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH, method = HttpMethod.POST, path = "re-index", produces = "application/json")
	@ExtensionDescriptor(description = "Create schema & indexing")
	public ExtensionResponse reIndex(
			@RexsterContext SecurityContext securityContext,
			@RexsterContext Graph g, @RexsterContext HttpServletRequest request) {

		ControllerResponse response = new ControllerResponse();
		TitanGraph graph = (TitanGraph) g;

		Iterator<Vertex> iter = graph.getVertices().iterator();
		IndexTransaction v = ((StandardTitanTx) ((StandardTitanGraph) graph)
				.getCurrentThreadTx()).getTxHandle().getIndexTransactionHandle(
				"search");
		int i = 00;
		String encode = LongEncoding.encode((Long) graph.getType("cpincode")
				.getId());
		while (iter.hasNext()) {
			Vertex x = iter.next();
			String type = x.getProperty("type");
			if (type != null && type.equals("circle")) {

				v.add("vertex", LongEncoding.encode((Long) x.getId()), encode,
						(Integer) x.getProperty("cpincode"), true);
			}
			try {
				if (i++ > 200) {
					v.flush();
					i = 0;
				}
			} catch (StorageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return response.prepare();
	}

	@ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH, method = HttpMethod.POST, path = "logout", produces = "application/json")
	@ExtensionDescriptor(description = "authentication service")
	public ExtensionResponse logout(
			@RexsterContext SecurityContext securityContext,
			@RexsterContext Graph g,
			@RexsterContext HttpServletRequest request,
			@ExtensionRequestParameter(name = "refresh", description = "user name", defaultValue = "false") Boolean refresh) {
		HttpSession session = request.getSession();
		ControllerResponse response = new ControllerResponse();
		session.invalidate();
		return response.prepare();
	}

	@ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH, method = HttpMethod.POST, path = "login", produces = "application/json")
	@ExtensionDescriptor(description = "authentication service")
	public ExtensionResponse authenticate(
			@RexsterContext SecurityContext securityContext,
			@RexsterContext Graph g,
			@RexsterContext HttpServletRequest request,
			@ExtensionRequestParameter(name = "refresh") String refresh) {
		ControllerResponse response = new ControllerResponse();
		AuthenticationPrincipal auth = (AuthenticationPrincipal) securityContext
				.getUserPrincipal();
		User user = auth.getUser();
		HttpSession session = request.getSession();
		if (refresh != null && refresh.equals("true") && user.username == null) {

			User lastUserSession = (User) session.getAttribute("HIM_TOKEN");
			if (lastUserSession.checkToken(user.token))
				user = new User(lastUserSession.username, lastUserSession.role);
			else {
				session.invalidate();
				response.setStatus(AUTH_ERROR);
				response.setMessage("Please login");
				return response.prepare();
			}
		} else if (user.username == null) {
			response.setStatus(AUTH_ERROR);
			response.setMessage("Please login");
			return response.prepare();
		}
		request.getSession().setAttribute("HIM_TOKEN", user);
		JSONObject tokens = new JSONObject();
		try {
			tokens.put("token", user.role + user.sysToken);
			tokens.put("expireOn", System.currentTimeMillis() + EXPIRATION_TIME);
			Vertex v = null;
			Iterator<Vertex> iterator = g.getVertices("__id", user.username)
					.iterator();
			if (iterator.hasNext()) {
				v = iterator.next();
				session.setAttribute("VID", v.getId());

			}
		} catch (Exception e) {

		}
		response.setDbResult(tokens);

		return response.prepare();
	}

	@ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH, method = HttpMethod.POST, path = "delete", produces = "application/json")
	@ExtensionDescriptor(description = "delete me to socio graph")
	public ExtensionResponse deleteFromNetwork(
			@RexsterContext SecurityContext securityContext,
			@RexsterContext Graph g,
			@RexsterContext HttpServletRequest request,
			@ExtensionRequestParameter(name = "id", description = "hereiam id") String id) {
		ControllerResponse response = null;
		TitanGraph graph = (TitanGraph) g;
		response = HimAuthenticationFilter
				.isAuthorizedUser(id, securityContext);
		if (response != null) {
			return response.prepare();
		}
		response = new ControllerResponse();
		if (id == null || id.isEmpty()) {
			response.setStatus(400);
			response.appendMsg("id should be valid value");
		}

		if (response.getStatus() != OK) {
			return response.prepare();
		}
		Vertex v = null;
		Iterator<Vertex> iterator = graph.getVertices("__id", id).iterator();
		if (iterator.hasNext()) {
			v = iterator.next();
		}
		if (v != null) {
			v.setProperty("status", SUserNode.SUSPEND);
		}

		return response.prepare();
	}

	@ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH, method = HttpMethod.POST, path = "addme", produces = "application/json")
	@ExtensionDescriptor(description = "add me to socio graph")
	public ExtensionResponse joinToNetwork(
			@RexsterContext SecurityContext securityContext,
			@RexsterContext Graph g,
			@RexsterContext HttpServletRequest request,
			@ExtensionRequestParameter(name = "id", description = "hereiam id") String id,
			@ExtensionRequestParameter(name = "name", description = "user name") String name,
			@ExtensionRequestParameter(name = "emailId", description = "email address") String emailId,
			@ExtensionRequestParameter(name = "domain", description = "domain") String domain,
			@ExtensionRequestParameter(name = "profile", description = "profile") String profile,
			@ExtensionRequestParameter(name = "photoUrl", description = "Photo Url") String photoUrl

	) {
		ControllerResponse response = null;
		TitanGraph graph = (TitanGraph) g;
		response = HimAuthenticationFilter
				.isAuthorizedUser(id, securityContext);
		if (response != null) {
			return response.prepare();
		}
		response = new ControllerResponse();
		if (id == null || id.isEmpty()) {
			response.setStatus(400);
			response.appendMsg("id should be valid value");
		}
		if (name == null || name.isEmpty()) {
			response.setStatus(400);
			response.appendMsg("name shouldnot be null");
		}

		if (response.getStatus() != OK) {
			return response.prepare();
		}
		Vertex v = null;
		Iterator<Vertex> iterator = graph.getVertices("__id", id).iterator();
		if (iterator.hasNext()) {
			v = iterator.next();
		} else {
			v = graph.addVertex(null);
		}
		SUserNode user = new SUserNode(v);
		user.setEmailAddress(emailId);
		user.setDomain(domain);
		user.setName(name);
		user.setId(id);
		user.setStatus(0);
		user.setPhotoUrl(photoUrl);
		user.setVer(System.currentTimeMillis());
		user.setLikes(0l);
		user.setFollowers(0l);
		user.setProfile(profile);
		response.setDbResult(user);
		return response.prepare();
	}

	@ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH, path = "add-loc")
	@ExtensionDescriptor(description = "add or change address")
	public ExtensionResponse addToMyLocation(
			@RexsterContext SecurityContext securityContext,
			@RexsterContext Graph g,
			@RexsterContext HttpServletRequest request,
			@ExtensionRequestParameter(name = "mid", description = "my id") String mid,
			@ExtensionRequestParameter(name = "cid", description = "circle id") String cid,
			@ExtensionRequestParameter(name = "lo", description = "longitude id") Float lo,
			@ExtensionRequestParameter(name = "la", description = "circle id") Float lat,
			@ExtensionRequestParameter(name = "area", description = "area") String area,
			@ExtensionRequestParameter(name = "house", description = "house or office block") String house,
			@ExtensionRequestParameter(name = "city", description = "city") String city,
			@ExtensionRequestParameter(name = "state", description = "state") String state,
			@ExtensionRequestParameter(name = "country", description = "country") String country,
			@ExtensionRequestParameter(name = "pincode", description = "pincode") Integer pincode,
			@ExtensionRequestParameter(name = "tags", description = "searchable keyindexes") String searchTags

	) {
		ControllerResponse response = null;
		final TitanGraph graph = (TitanGraph) g;
		response = HimAuthenticationFilter.isAuthorizedUser(mid,
				securityContext);
		if (response != null) {
			return response.prepare();
		}

		GremlinPipeline pipe = new GremlinPipeline();
		Iterator iter = pipe.start(graph.getVertices("__id", mid)).as("v")
				.outE("location").as("e").optional("v")
				.select(new PipeFunction<Element, Element>() {
					public Element compute(Element arg0) {
						if (arg0 instanceof Edge)
							graph.removeEdge((Edge) arg0);
						return arg0;
					}
				}).range(0, 1).iterator();
		if (!iter.hasNext()) {
			response.setStatus(Status.BAD_REQUEST.getStatusCode());
			response.setMessage("user does not exist in social network database");
			return response.prepare();
		}
		Vertex myV = (Vertex) ((Row) iter.next()).getColumn("v");

		response = new ControllerResponse();
		if (cid == null && city == null && state == null && pincode == null) {
			response.setStatus(Status.BAD_REQUEST.getStatusCode());
			response.setMessage("circle id/city   ,pincode,state,country are required");
			return response.prepare();
		}
		response = new ControllerResponse();
		SGeoLocationEdge edge = new SGeoLocationEdge();
		edge.setLabel(SchemaFactory.E_LOCATON_EDGE);
		edge.setCid(cid);
		if (lo != null && lat != null) {
			edge.setLoc(Geoshape.point(lat, lo));
		}
		edge.setArea(area);
		edge.setHouse(house);
		edge.setCity(city);
		edge.setState(state);
		edge.setCountry(country);
		edge.setPincode(pincode);
		edge.setEtags(searchTags);
		response = addLocation(myV, edge, graph);

		return response.prepare();
	}

	private ControllerResponse addLocation(Vertex myV, SGeoLocationEdge edge,
			final TitanGraph graph) {
		ControllerResponse response = new ControllerResponse();
		try {

			if (edge.getCid() != null || edge.getPincode() != null) {
				Iterator<Result<Vertex>> es = graph
						.indexQuery(AbstractExtension.INDEX_NAME,
								"czone:(" + edge.getPincode() / 10000 + ")")
						.limit(1).vertices().iterator();
				if (es.hasNext()) {
					Vertex loc = es.next().getElement();
					Edge e = graph.addEdge(null, myV, loc, edge.getLabel());
					edge.setEdge(e);
					edge.setDoj(new Date());
					edge.prepare();
					response.setDbResult(e);
				} else {
					response.setMessage("circle doesnot exists");
					response.setStatus(BAD_INPUT);
				}
			} else {
				Iterator<Vertex> es = graph.getVertices("__cid", "0")
						.iterator();
				if (es.hasNext()) {
					Vertex loc = es.next();
					Edge e = graph.addEdge(null, myV, loc, edge.getLabel());
					edge.setEdge(e);
					edge.setDoj(new Date());
					edge.prepare();

				}
			}

			graph.commit();

		} catch (Exception e) {
			response.setStatus(Status.INTERNAL_SERVER_ERROR.getStatusCode());
			logger.error(e);
			response.setMessage(e.getMessage());
			graph.rollback();
		}
		return response;
	}

	@ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH, path = "invite")
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
		Vertex meV = meIter.next();
		Vertex friendV = fIter.next();
		List flist = new ArrayList();
		flist.add(friendV);
		Edge frnd = null;
		try {
			GremlinPipeline pipe = new GremlinPipeline();
			Iterator iter = pipe.start(meV).bothE(SFriendEdge.LABEL).as("e")
					.bothV().retain(flist).back("e").iterator();
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
		Edge e = meV.addEdge(SFriendEdge.LABEL, friendV);
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

	@ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH, path = "search")
	@ExtensionDescriptor(description = "search contact list")
	public ExtensionResponse findPeople(
			@RexsterContext SecurityContext securityContext,
			@RexsterContext Graph g,
			@RexsterContext HttpServletRequest request,
			@ExtensionRequestParameter(name = "myId", description = "my id") String myId,
			@ExtensionRequestParameter(name = "name", description = "name search ") String name,
			@ExtensionRequestParameter(name = "cid", description = "community id") String cid,
			@ExtensionRequestParameter(name = "pincode", description = "pincode") String pincode,
			@ExtensionRequestParameter(name = "city", description = "city") String city,
			@ExtensionRequestParameter(name = "tag", description = "searchable tag") String tag,
			@ExtensionRequestParameter(name = "pageSize", defaultValue = "15") Integer pageSize) {

		ControllerResponse response;

		TitanGraph graph = (TitanGraph) g;

		response = new ControllerResponse();
		if (name == null || name.isEmpty()) {
			response.setStatus(400);
			response.setMessage("me or fid is missing in request");
			return response.prepare();
		}
		HttpSession session = request.getSession();
		Object oid = session.getAttribute("VID");

		Vertex me = graph.getVertex(oid);
		if (name == null) {
			name = "";
		}

		if (me == null) {
			response.setStatus(403);
			response.setMessage("You are not part of social network,please contact Admin");
			return response.prepare();
		}

		StringBuffer query = new StringBuffer("e.name:(" + name + "*)");
		GremlinPipeline pipe = new GremlinPipeline();
		Iterator resultIter = null;

		if (city != null) {
			resultIter = graph
					.indexQuery("search",
							"e.city:(" + city + ") AND e.name:(" + name + "*)")
					.limit(pageSize).edges().iterator();
		} else if (pincode != null) {
			resultIter = graph
					.indexQuery(
							"search",
							"e.pincode:(" + pincode + ") AND e.etag:(" + name
									+ "*)").limit(pageSize).vertices()
					.iterator();
		} else if (tag != null) {
			QueryParser parser = new QueryParser(Version.LUCENE_44, "e.city",
					new StandardAnalyzer(Version.LUCENE_44));
			String termq = null;
			try {
				termq = parser.parse(tag).toString();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			resultIter = graph.indexQuery("search", termq).limit(pageSize)
					.edges().iterator();
		} else {
			resultIter = graph
					.indexQuery("search", "e.profile:(" + name + "*)")
					.limit(pageSize).edges().iterator();
		}

		response.setDbResult(resultIter);

		return response.prepare();

	}

	@ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH, path = "friends")
	@ExtensionDescriptor(description = "my contact list")
	public ExtensionResponse friends(
			@RexsterContext SecurityContext securityContext,
			@RexsterContext Graph g,
			@RexsterContext HttpServletRequest request,
			@ExtensionRequestParameter(name = "my", description = "a value to reply with") String my,
			@ExtensionRequestParameter(name = "start", defaultValue = "0") Integer start,
			@ExtensionRequestParameter(name = "pageSize", defaultValue = "15") Integer pageSize) {

		ControllerResponse response;
		TitanGraph graph = (TitanGraph) g;
		if ((response = HimAuthenticationFilter.isAuthorizedUser(my,
				securityContext)) != null) {
			return response.prepare();
		}
		response = new ControllerResponse();
		if (my == null || my.isEmpty()) {
			response.setStatus(400);
			response.setMessage("me or fid is missing in request");
			return response.prepare();
		}

		Iterator<Vertex> vtxs = graph.query().has("__id", my).limit(1)
				.vertices().iterator();
		Vertex me = null;
		if (vtxs.hasNext())
			me = vtxs.next();

		if (me == null) {
			response.setStatus(403);
			response.setMessage("You are not part of social network,please contact Admin");
			return response.prepare();
		}
		Iterator friends = null;
		List<Vertex> excepts = new ArrayList<Vertex>();
		excepts.add(me);
		List<String> steps = new ArrayList<String>();
		steps.add("e");
		steps.add("v");
		try {
			GremlinPipeline pipe = new GremlinPipeline();
			friends = pipe
					.start(me)
					.bothE(SFriendEdge.LABEL)
					.as("e")
					.bothV()
					.except(excepts)
					.has("status", 0)
					.order(new PipeFunction<Pair<Vertex, Vertex>, Integer>() {
						public Integer compute(Pair<Vertex, Vertex> arg0) {
							return ((String) arg0.getA().getProperty("name"))
									.compareTo((String) arg0.getB()
											.getProperty("name"));
						}
					}).as("v").range(start, (pageSize - 1)).select(steps)
					.iterator();

		} catch (Exception e) {
			e.printStackTrace();
		}

		response.setDbResult(friends);

		return response.prepare();

	}

	@ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH, path = "acceptOrReject")
	@ExtensionDescriptor(description = "my contact list")
	public ExtensionResponse acceptOrReject(
			@RexsterContext SecurityContext securityContext,
			@RexsterContext Graph g,
			@RexsterContext HttpServletRequest request,
			@ExtensionRequestParameter(name = "my") String my,
			@ExtensionRequestParameter(name = "fid") String fid,
			@ExtensionRequestParameter(name = "action") String action,
			@ExtensionRequestParameter(name = "relId") String relId) {
		ControllerResponse response;
		final TitanGraph graph = (TitanGraph) g;
		if ((response = HimAuthenticationFilter.isAuthorizedUser(my,
				securityContext)) != null) {
			return response.prepare();
		}
		response = new ControllerResponse();
		if (action == null || action.trim().length() == 0) {
			response.setStatus(400);
			response.setMessage("invalid action");
			return response.prepare();
		}

		if (action.equalsIgnoreCase(EnumFriendshipStatus.REJECT.getStatus())) {
			List flist = new ArrayList();
			Edge frnd = null;
			try {
				GremlinPipeline pipe = new GremlinPipeline();
				Iterator iter = pipe.start(graph.getVertices("__id", my))
						.bothE("friend").as("e").bothV().has("__id", fid)
						.select(new PipeFunction<Edge, Edge>() {
							public Edge compute(Edge arg0) {
								graph.removeEdge(arg0);
								return arg0;
							}
						}).range(0, 1).iterator();
				if (iter.hasNext()) {
					frnd = (Edge) ((Row) iter.next()).getColumn("e");
				}
				response.setDbResult(frnd);

			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			GremlinPipeline pipe = new GremlinPipeline();
			Iterator iter = pipe.start(graph.getVertices("__id", my))
					.inE("friend").as("e").outV().has("__id", fid)
					.select(new PipeFunction<Edge, Edge>() {
						public Edge compute(Edge arg0) {
							arg0.setProperty("estatus",
									SFriendEdge.EnumFriendshipStatus.FRIEND
											.getStatus());
							arg0.setProperty("fsd", new java.util.Date());
							return arg0;
						}
					}).range(0, 1).iterator();
			if (iter.hasNext()) {

				response.setDbResult(((Row) iter.next()).getColumn("e"));
			} else {
				response.setStatus(BAD_INPUT);
				response.setMessage("Not allowed");
			}

		}

		return response.prepare();
	}

	@ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH, method = HttpMethod.POST, path = "init-address")
	@ExtensionDescriptor(description = "index address")
	public ExtensionResponse indexState(
			@RexsterContext SecurityContext securityContext,
			@RexsterContext Graph g,
			@ExtensionRequestParameter(name = "myId") String myId,
			@ExtensionRequestParameter(name = "json") String jsonData,
			@RexsterContext HttpServletRequest request) {

		ControllerResponse response;
		TitanGraph graph = (TitanGraph) g;
		if (false && (response = HimAuthenticationFilter.isAdminUser(myId,
				securityContext)) != null) {
			return response.prepare();
		}
		response = new ControllerResponse();
		String state = null;
		String country = null;
		JSONArray array = null;

		try {
			JSONArray json = new JSONArray(jsonData);
			for (int i = 0; i < json.length(); i++) {
				JSONObject o = json.getJSONObject(i);
				state = o.getString("cstate");
				array = o.getJSONArray("czone");
				country = o.getString("ccountry");

				TitanVertex v = (TitanVertex) graph.addVertex(null);
				v.setProperty("cstate", state);
				v.setProperty("ccountry", country);
				v.setProperty("type", "circle");
				v.setProperty("__cid", state);
				for (int j = 0; j < array.length(); j++) {
					v.addProperty("czone", array.getInt(j));
				}

			}
			graph.commit();
		} catch (Exception e) {
			logger.error(e);
			response.setMessage(e.getMessage());
			response.setStatus(SERVER_ERROR);
		}

		return response.prepare();

	}

}
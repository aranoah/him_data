package com.anh.him.rexter.model;

import java.util.Date;

import com.anh.him.rexter.AbstractExtension;
import com.thinkaurelius.titan.core.Mapping;
import com.thinkaurelius.titan.core.Parameter;
import com.thinkaurelius.titan.core.Titan;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanKey;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import com.thinkaurelius.titan.graphdb.types.StandardKeyMaker;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;

public class SchemaFactory {
	private static final String E_FOLLOWS = "follows";
	private static final String E_FRIEND_OF = "friendOf";
	public static final int CURR_VERSION = 1;
	public static final String E_LOCATON_EDGE = "locatedIn";
	public static final String E_BUSINESS_LOC_EDGE = "linkAt";

	// public static final String E_BUSINESS_TYPE_EDGE="linkAt";

	public static void createIndex(TitanGraph graph, int version) {
		switch (version) {
		case 1: {

			// USER NODE
			graph.makeKey("__id").dataType(String.class)
					.indexed(Vertex.class)
					.unique().make();
			graph.makeKey("type").dataType(String.class).make();
			graph.makeKey("photoUrl").dataType(String.class).make();
			graph.makeKey("emailId").dataType(String.class).make();
			graph.makeKey("name").dataType(String.class).make();
			graph.makeKey("desc").dataType(String.class).make();
			graph.makeKey("status").dataType(Integer.class).make();
			graph.makeKey("doj").dataType(Date.class).make();
			graph.makeKey("city").dataType(String.class).make();
			graph.makeKey("state").dataType(String.class).make();
			graph.makeKey("address").dataType(String.class).make();
			graph.makeKey("country").dataType(String.class).make();
			graph.makeKey("domain").dataType(String.class).make();
			graph.makeKey("ver").dataType(Long.class).make();
			TitanKey fndship = graph.makeKey("estatus").dataType(String.class)
					.make();
			// participant keys
			TitanKey likes = graph.makeKey("likes").dataType(Long.class).make();

			// indexing
			TitanKey followers = graph.makeKey("followers")
					.dataType(Long.class).make();
			TitanKey rating = graph.makeKey("rating").dataType(Float.class)
					.make();
			TitanKey profile = graph.makeKey("profile").dataType(String.class)
					.indexed(AbstractExtension.INDEX_NAME, Edge.class).make();
			TitanKey pincode = graph.makeKey("pincode").dataType(Integer.class)
					.indexed(AbstractExtension.INDEX_NAME, Element.class)
					.make();
			TitanKey loc = graph.makeKey("loc").dataType(Geoshape.class)
					.indexed(AbstractExtension.INDEX_NAME, Edge.class).make();
			TitanKey btype = graph.makeKey("btype").dataType(Integer.class)
					.indexed(AbstractExtension.INDEX_NAME, Edge.class).make();
			TitanKey cid = graph.makeKey("cid").dataType(Integer.class)
					.indexed(AbstractExtension.INDEX_NAME, Edge.class).make();

			TitanKey etags = graph
					.makeKey("etags")
					.dataType(String.class)
					.indexed(
							AbstractExtension.INDEX_NAME,
							Edge.class,
							new Parameter[] { Parameter.of(
									Mapping.MAPPING_PREFIX, Mapping.TEXT) })
					.make();

			// label declaration
			graph.makeLabel(E_BUSINESS_LOC_EDGE).signature(btype, pincode)
					.sortKey(rating, likes, followers).oneToMany().make();

			graph.makeLabel(E_LOCATON_EDGE).signature(profile, pincode)
					.sortKey(rating, likes, followers).oneToMany().make();

			graph.makeLabel(E_FRIEND_OF).signature(fndship).manyToMany().make();
			graph.makeLabel(E_FOLLOWS).manyToMany().make();
			// Location or visiting place
			graph.makeKey("caddress").dataType(String.class).make();
			graph.makeKey("carea").dataType(String.class).make();
			TitanKey ccity = graph.makeKey("ccity").dataType(String.class)
					.indexed(AbstractExtension.INDEX_NAME, Edge.class).make();

			// SCircle Node
			graph.makeKey("cstate").dataType(String.class).make();
			graph.makeKey("ccountry").dataType(String.class).make();
			graph.makeKey("cname").dataType(String.class).make();
			TitanKey cpincode = graph.makeKey("czone").dataType(Integer.class)
					.list().indexed(Titan.Token.STANDARD_INDEX, Vertex.class)
					.make();
			TitanKey __cid = graph.makeKey("__cid").dataType(String.class)
					.indexed(AbstractExtension.INDEX_NAME, Vertex.class).make();
			StandardKeyMaker l;
			graph.commit();
		}
			break;
		default:
			break;
		}

	}
}

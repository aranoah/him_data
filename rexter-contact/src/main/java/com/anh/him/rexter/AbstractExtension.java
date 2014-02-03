package com.anh.him.rexter;

import java.util.HashMap;

import com.anh.him.rexter.model.SFriendEdge;
import com.anh.him.rexter.model.SUserNode;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraphConfiguration;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.modules.TypeResolver;
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule;

public interface AbstractExtension {
	public static final String EXTENSION_NAMESPACE = "rexter-him";
	public static final int BAD_INPUT = 400;
	public static final int SESSION_EXPIRE = 403;
	public static final int SERVER_ERROR = 500;
	public static final int OK = 0;
	public static final int AUTH_ERROR = 401;
	public static final int EXPIRATION_TIME = 24 * 3600 * 1000;
	public static String INDEX_NAME = "search";
	public static final HashMap<String, EnumNodeType> nodes = new HashMap<String, AbstractExtension.EnumNodeType>();
	public static final int DEPLOYMENT_VERSION=1;

	enum EnumNodeType {
		USER("user"), PLACE("place"), GROUP("group"), BUSINESS("business"),EVENT("event"),;
		String type;

		private EnumNodeType(String type) {
			this.type = type;
			nodes.put(type, this);
		}

		@Override
		public String toString() {
			return this.type;
		}

		public EnumNodeType getType(String type) {
			return nodes.get(type);
		}
	}

	public GremlinGroovyModule module = new GremlinGroovyModule() {
		boolean initialized = false;

		@Override
		public void doConfigure(FramedGraphConfiguration config) {
			if (!initialized) {
				initialized = true;
			}
			config.addTypeResolver(new TypeResolver() {
				public Class<?>[] resolveTypes(Edge e, Class<?> defaultType) {

					return new Class[] { defaultType };
				}

				public Class<?>[] resolveTypes(Vertex v, Class<?> defaultType) {

					return new Class[] { SUserNode.class, SFriendEdge.class };
				}
			});
			super.doConfigure(config);
		}
	};
	public FramedGraphFactory factory = new FramedGraphFactory(module);
}

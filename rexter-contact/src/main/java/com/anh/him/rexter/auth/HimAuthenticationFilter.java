package com.anh.him.rexter.auth;

import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.UUID;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

import com.anh.him.rexter.AbstractExtension;
import com.anh.him.rexter.ControllerResponse;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.tinkerpop.rexster.Tokens;
import com.tinkerpop.rexster.filter.AbstractSecurityFilter;
import com.tinkerpop.rexster.protocol.msg.ErrorResponseMessage;
import com.tinkerpop.rexster.protocol.msg.RexProMessage;
import com.tinkerpop.rexster.protocol.msg.SessionRequestMessage;
import com.tinkerpop.rexster.protocol.server.RexProRequest;

/**
 * Provides authentication for Rexster for all services: RexPro, REST, and Dog
 * House.
 * <p/>
 * Utilizes a simple list of usernames and passwords in rexster.xml. Example:
 * <p/>
 * <security> <authentication> <type>default</type> <configuration> <users>
 * <user> <username>rexster</username> <password>rexster</password> </user>
 * </users> </configuration> </authentication> </security>
 * 
 * @author aniyus
 */
public class HimAuthenticationFilter extends AbstractSecurityFilter implements
		ContainerRequestFilter {
	private static final int VERIFY_TIME_GAP = 2*60*1000;
	private static final Logger logger = Logger
			.getLogger(AbstractSecurityFilter.class);
	private static TimeZone COMMTIMEZONE=TimeZone.getTimeZone("IST");

	@Context
	protected UriInfo uriInfo;

	@Context
	protected ServletConfig servletConfig;

	@Context
	protected HttpServletRequest httpServletRequest;

	@Context
	protected HttpServletResponse httpServletResponse;

	private boolean isConfigured = false;

	private static String allowDomain;

	private static String authUrl;

	private static long expirationTime = VERIFY_TIME_GAP;
	private static SortedSet<String> ignorePaths = new TreeSet<String>();

	public HimAuthenticationFilter() {
	}

	public HimAuthenticationFilter(final XMLConfiguration configuration) {
		configure(configuration);
		isConfigured = true;
	}

	private static Map<String, String> users = null;

	/**
	 * basic = userid:sessionid:XXXsecurityToken returned token =
	 * sessionid+server generated token
	 * 
	 * security token = Md5(username + ":" + password + ":"
	 * +currentMilis.longValue()+":"+ myPassword)
	 */

	/**
	 * Checks the users map to determine if the username and password supplied
	 * is one of the ones available..
	 */
	public boolean authenticate(final String username, final String password,
			String svcToken) {
		// first three char userId
		String svrUid = svcToken.substring(0, 3);
		String svrTime = svcToken.substring(3, 23);
		String secureToken = svcToken.substring(23);
		String myPassword = this.users.get(svrUid);

		if (myPassword == null) {
			return false;
		}
		Long currentMilis = new Long(svrTime);
		long currSrvTime = System.currentTimeMillis();
		long timeDiff = currSrvTime-currentMilis;

		if (timeDiff>expirationTime) {
			return false;
		}
		try {
			StringBuffer hexString = new StringBuffer();
			byte[] hash = MessageDigest.getInstance("MD5").digest(
					(username + ":" + password + ":" + currentMilis.longValue()
							+ ":" + myPassword).getBytes());

			for (int i = 0; i < hash.length; i++) {
				if ((0xff & hash[i]) < 0x10) {
					hexString.append("0"
							+ Integer.toHexString((0xFF & hash[i])));
				} else {
					hexString.append(Integer.toHexString(0xFF & hash[i]));
				}
			}

			return hexString.toString().equals(secureToken);
		} catch (NoSuchAlgorithmException e) {
			return false;
		}
	}
	
	public static String md5(byte[] data){
		
		try {
			StringBuffer hexString = new StringBuffer();
			byte[] hash = MessageDigest.getInstance("MD5").digest(data);

			for (int i = 0; i < hash.length; i++) {
				if ((0xff & hash[i]) < 0x10) {
					hexString.append("0"
							+ Integer.toHexString((0xFF & hash[i])));
				} else {
					hexString.append(Integer.toHexString(0xFF & hash[i]));
				}
			}

			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}

	/**
	 * Checks the users map to determine if the username and password supplied
	 * is one of the ones available..
	 */
	public boolean webauthenticate(final String username, final String password) {
		String myPassword = this.users.get(username);
		if (myPassword == null) {
			return false;
		}
		return password.equals(myPassword);
	}

	/**
	 * Reads the configuration from rexster.xml and converts it to a map of
	 * usernames and passwords. session->expireTime,authUrl no-auths->url[..]
	 * 
	 */
	public void configure(final XMLConfiguration configuration) {
		if (this.users == null) {
			this.users = new HashMap<String, String>();

			try {
				final HierarchicalConfiguration authenticationConfiguration = configuration
						.configurationAt("security.authentication.configuration.users");
				final List<HierarchicalConfiguration> userListFromConfiguration = authenticationConfiguration
						.configurationsAt("user");
				final HierarchicalConfiguration expiration = configuration
						.configurationAt("security.authentication.configuration.session");
				expirationTime = expiration.getLong("expireTime");
				authUrl = expiration.getString("authUrl");
				final HierarchicalConfiguration domain = configuration
						.configurationAt("security.authentication.configuration.domain");
				allowDomain = domain.getString("allow");

				final HierarchicalConfiguration skips = configuration
						.configurationAt("security.authentication.configuration.no-auths");
				final List<HierarchicalConfiguration> skipsList = skips
						.configurationsAt("url");

				for (HierarchicalConfiguration userFromConfiguration : userListFromConfiguration) {
					this.users.put(userFromConfiguration.getString("username"),
							userFromConfiguration.getString("password"));
				}
				for (HierarchicalConfiguration skip : skipsList) {
					this.ignorePaths
							.add((String) skip.getRootNode().getValue());
					System.out.println((String) skip.getRootNode().getValue());
				}

			} catch (Exception e) {
				this.users = null;
				throw new RuntimeException(
						"Invalid configuration of users in configuration file.",
						e);
			}
		}
	}

	public String getName() {
		return "HimDefaultSecurity";
	}

	/**
	 * RexPro authentication
	 */
	public NextAction handleRead(final FilterChainContext ctx)
			throws IOException {
		final RexProRequest request = ctx.getMessage();
		request.process();

		final RexProMessage message = request.getRequestMessage();
		if (message instanceof SessionRequestMessage && !message.hasSession()) {
			final SessionRequestMessage specificMessage = (SessionRequestMessage) message;

			if (!specificMessage.metaGetKillSession()) {
				final String username = specificMessage.Username;
				final String password = specificMessage.Password;
				if (!authenticate(username, password, null)) {
					// there is no session to this message...that's a problem
					final ErrorResponseMessage errorMessage = new ErrorResponseMessage();
					errorMessage.setSessionAsUUID(RexProMessage.EMPTY_SESSION);
					errorMessage.Request = specificMessage.Request;
					errorMessage.ErrorMessage = "Invalid username or password.";
					errorMessage
							.metaSetFlag(ErrorResponseMessage.AUTH_FAILURE_ERROR);

					request.writeResponseMessage(errorMessage);
					ctx.write(request);

					return ctx.getStopAction();
				}
			}
		}

		return ctx.getInvokeAction();
	}

	/**
	 * REST/Dog House based authentication.
	 */
	public ContainerRequest filter(final ContainerRequest request) {
		httpServletResponse.setHeader("Access-Control-Allow-Origin",
				allowDomain);

		httpServletResponse.setHeader("Access-Control-Allow-Methods",
				"GET,PUT,POST,OPTIONS,DELETE");
		httpServletResponse.setHeader("Access-Control-Allow-Credentials",
				"true");

		String currUrl = httpServletRequest.getRequestURI();
		boolean ignorable = ignorePaths.contains(currUrl);
		final User user = authenticateServletRequest(request, ignorable);
		request.setSecurityContext(new Authorizer(user));

		return request;
	}

	private void initFromServletConfiguration() {
		// have to do this here because the @Context is not initialized in the
		// constructor
		if (isConfigured) {
			final String rexsterXmlFile = servletConfig
					.getInitParameter("com.tinkerpop.rexster.config");
			final XMLConfiguration properties = new XMLConfiguration();

			try {
				properties.load(new FileReader(rexsterXmlFile));
			} catch (Exception e) {
				throw new RuntimeException("Could not locate " + rexsterXmlFile
						+ " properties file.", e);
			}
			configure(properties);
			isConfigured = true;
		}
	}

	boolean initialized = false;

	private User authenticateServletRequest(ContainerRequest request,
			boolean ignorable) {

		// not sure that this will ever get called, but it's worth a check
		this.initFromServletConfiguration();
		// if (!initialized) {
		// httpServletResponse.setHeader("Access-Control-Allow-Origin","http://hereiamconnect.com:8080");
		initialized = true;
		// }
		final boolean isLoginCall = httpServletRequest.getRequestURI().indexOf(
				"/socio/login") != -1;
		final boolean isExtension = httpServletRequest.getRequestURI().indexOf(
				"/rexter-him/") != -1;

		// get the authorization header value
		String authentication = request.getHeaderValue("HIM_AUTHERISATION");
		if (authentication == null)
			authentication = request
					.getHeaderValue(ContainerRequest.AUTHORIZATION);
		User user =null;
		try {
			user = verifyToken(request, isLoginCall, isExtension,
					authentication);
		} catch (RuntimeException e) {
            if(!ignorable){
            	throw e;
            }
		}
		if ((user != null && !isLoginCall) || (!isLoginCall && ignorable)) {
			return user;
		}

		if (!authentication.startsWith("Basic ")) {
			logger.info("Authentication failed: request for unsupported authentication type ["
					+ authentication + "]");
			throw new WebApplicationException(
					generateErrorResponse("Invalid authentication credentials."));
		}

		final String authenticationBase64Segment = authentication
				.substring("Basic ".length());
		final String[] values = new String(
				Base64.base64Decode(authenticationBase64Segment)).split(":");
		if (values.length == 2) {
			if (webauthenticate(values[0], values[1])) {
				return new User(values[0], values[1]);
			} else {
				throw new WebApplicationException(
						generateWebResponse("Invalid authentication credentials."));
			}
		}
		if (values.length < 3) {
			logger.info("Authentication failed: invalid authentication string format ["
					+ authenticationBase64Segment + "]");
			throw new WebApplicationException(
					generateErrorResponse("Invalid authentication credentials."));
		}

		final String username = values[0];
		final String password = values[1];
		final String svrtoken = values[2];
		if ((username == null) || (password == null) || svrtoken == null) {
			logger.info("Authentication failed: missing valid token["
					+ authentication + "]");
			throw new WebApplicationException(
					generateErrorResponse("Invalid authentication credentials."));
		}

		if (authenticate(username, password, svrtoken)) {
			user = new User(username, password);
			if (isLoginCall) {
				javax.servlet.http.Cookie cookie = new javax.servlet.http.Cookie(
						"HIM_TOKEN", user.getToken());
				cookie.setDomain("hereiamconnect.com");
				// cookie.setPath("/");
				cookie.setMaxAge(24 * 3600);
				httpServletResponse.addCookie(cookie);
			}
			logger.debug("Authentication succeeded for [" + username + "]");
		} else {
			logger.info("Authentication failed: invalid username or password ["
					+ authenticationBase64Segment + "]");
			throw new WebApplicationException(
					generateErrorResponse("Invalid username or password."));
		}

		return user;
	}

	private User verifyToken(ContainerRequest request,
			final boolean isLoginCall, final boolean isExtension,
			String authentication) {
		String token = request.getHeaderValue("HIM_TOKEN");
		final String s2s = request.getHeaderValue("S2S_TOKEN");
		Map<String, javax.ws.rs.core.Cookie> cookiesMap = request.getCookies();

		if (!isLoginCall && token == null) {
			Cookie cookie = cookiesMap.get("HIM_TOKEN");
			token = (cookie != null ? cookiesMap.get("HIM_TOKEN").getValue()
					: null);
		}

		if (authentication == null && token == null && s2s == null) {
			if (isExtension) {
				throw new WebApplicationException(
						generateErrorResponse("Authentication credentials are required."));
			} else
				throw new WebApplicationException(
						generateWebResponse("Authentication credentials are required."));
		}
		if (!isLoginCall && token != null) {
			User user = new User(null, null, token);
			isValidUserAndInitialized(httpServletRequest, user, isExtension);
			return user;
		}
		if (s2s != null) {
			String data[] = new String(Base64.base64Decode(s2s)).split(":");
			if (data == null || data.length != 3) {
				throw new WebApplicationException(
						generateErrorResponse("Authentication credentials are required."));
			}
			long currentTime =  System.currentTimeMillis();
			long tokenTime = new Long(data[0]).longValue();
			long timeDiff = (currentTime-tokenTime);
			if(timeDiff>VERIFY_TIME_GAP || timeDiff<0 ){
				throw new WebApplicationException(
						generateErrorResponse("Authentication credentials are required."));
			}
			long encoding = Long.parseLong(data[0].substring(data[0].length()-5));
			String square = ""+(encoding*encoding);
			String myPassword = this.users.get(data[1]);
			String secToken = HimAuthenticationFilter.md5((square.substring(0,5)+myPassword+square.substring(5)).getBytes());
			if(!secToken.equals(data[2])){
				throw new WebApplicationException(
						generateErrorResponse("Authentication credentials are required."));
			}
			return new User(true, data[1]);
		}
		return null;
	}

	private static Response generateErrorResponse(final String message) {
		final Map<String, String> errorEntity = new HashMap<String, String>() {
			{
				put("message", message);
				put(Tokens.VERSION, Tokens.REXSTER_VERSION);
			}
		};
		ControllerResponse response = new ControllerResponse(401, message);

		return response.prepare().getJerseyResponse();
	}

	private static Response generateWebResponse(final String message) {
		final Map<String, String> errorEntity = new HashMap<String, String>() {
			{
				put("message", message);
				put(Tokens.VERSION, Tokens.REXSTER_VERSION);
			}
		};

		return Response.status(Response.Status.UNAUTHORIZED)
				.header("WWW-Authenticate", "Basic realm=\"rex\"")
				.type("application/json").entity(new JSONObject(errorEntity))
				.build();
	}

	public static class User {
		public String username;
		public String role;
		public String token;
		public String sysToken;
		public long time;
		public String serverTalkToken;
		public boolean isServerTalk = false;

		public User(boolean serverTalk, String serverTalkToken) {
			this(null);
			this.isServerTalk = serverTalk;
			this.serverTalkToken = serverTalkToken;

		}

		public User(String token) {
			this.username = null;
			this.role = null;
			this.token = token;
			this.time = System.currentTimeMillis();
			this.sysToken = null;
			this.serverTalkToken = null;
		}

		public User(String username, String role, String token) {
			this.username = username;
			this.role = role;
			this.token = token;
			this.time = System.currentTimeMillis();
			this.sysToken = null;
		}

		public User(String username, String role) {
			this.username = username;
			this.role = role;
			this.token = null;
			this.time = System.currentTimeMillis();
			this.sysToken = UUID.randomUUID().toString();
		}

		public String refreshToken() {
			this.time = System.currentTimeMillis();
			this.sysToken = UUID.randomUUID().toString();
			return getToken();
		}

		public boolean checkToken(String userToken) {

			return (getToken()).equals(userToken);
		}

		public String getToken() {
			return sysToken;
		}

	}

	public boolean isValidUserAndInitialized(HttpServletRequest request,
			User user, boolean isExtension) {
		String msg = "Invalid session.Please relogin";
		if (user.isServerTalk) {
			User tokenUser = (User) request.getSession().getAttribute(
					"HIM_TOKEN");
			if (tokenUser == null) {
				return true;
			}
			if (tokenUser.serverTalkToken.equals(user.serverTalkToken)) {
				// return false;
				msg = "Invalid server token";
			} else {
				request.getSession().setAttribute("HIM_TOKEN", user);
				return true;
			}
		} else if (user.username == null && user.token != null) {
			User tokenUser = (User) request.getSession().getAttribute(
					"HIM_TOKEN");
			if (tokenUser != null && tokenUser.checkToken(user.token)) {
				user.username = tokenUser.username;
				if ((tokenUser.time + AbstractExtension.EXPIRATION_TIME) > user.time) {
					return true;
				} else {
					user.refreshToken();
					javax.servlet.http.Cookie cookie = new javax.servlet.http.Cookie(
							"HIM_TOKEN", user.refreshToken());
					cookie.setDomain("hereiamconnect.com");
					// cookie.setPath("/");
					cookie.setMaxAge(24 * 3600);
					httpServletResponse.addCookie(cookie);
					return true;
				}
			}
		}

		if (isExtension) {
			throw new WebApplicationException(generateErrorResponse(msg));
		} else
			throw new WebApplicationException(generateWebResponse(msg));
	}

	public static String getUserId(SecurityContext securityContext) {
		AuthenticationPrincipal auth = (AuthenticationPrincipal) securityContext
				.getUserPrincipal();

		return (auth != null && auth.getUser() != null) ? auth.getUser().username
				: null;
	}

	public static ControllerResponse isAdminUser(String userId,
			SecurityContext securityContext) {
		AuthenticationPrincipal auth = (AuthenticationPrincipal) securityContext
				.getUserPrincipal();
		User user = auth.getUser();
		if (user.isServerTalk) {
			return null;
		}
		ControllerResponse response = new ControllerResponse(
				AbstractExtension.AUTH_ERROR, "Permission denied");
		return response;

	}
	public static ControllerResponse isAuthorizedUser(String userId,
			SecurityContext securityContext) {
		AuthenticationPrincipal auth = (AuthenticationPrincipal) securityContext
				.getUserPrincipal();
		User user = auth.getUser();
		if (user.isServerTalk) {
			return null;
		}
		if (user != null && user.username != null) {
			if (user.username.equals(userId)) {
				return null;
			}
		}
		ControllerResponse response = new ControllerResponse(
				AbstractExtension.SESSION_EXPIRE, "Session expired");
		return response;

	}

	public static class AuthenticationPrincipal implements Principal {

		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

		private User user;
		private String token;

		/**
		 * @return the user
		 */
		public User getUser() {
			return user;
		}

		/**
		 * @param user
		 *            the user to set
		 */
		public void setUser(User user) {
			this.user = user;
		}

	}

	public static class Authorizer implements SecurityContext {
		private final AuthenticationPrincipal principal;

		public Authorizer(final User user) {

			this.principal = new AuthenticationPrincipal();
			principal.setUser(user);
		}

		public User getUser() {
			return principal.getUser();
		}

		/**
		 * @return the principal
		 */
		public Principal getPrincipal() {
			return principal;
		}

		public Principal getUserPrincipal() {
			return this.principal;
		}

		public boolean isUserInRole(String role) {
			return (role.equals(principal.user.role));
		}

		public boolean isSecure() {
			return true;
		}

		public String getAuthenticationScheme() {
			return SecurityContext.FORM_AUTH;
		}
	}

	@Override
	public boolean authenticate(String user, String password) {

		return false;
	}

}

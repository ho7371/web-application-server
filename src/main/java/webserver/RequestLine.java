package webserver;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;

public class RequestLine {

	private static final Logger log = LoggerFactory.getLogger(RequestLine.class);

	public String path;

	private HttpMethod method;

	private Map<String, String> headerMap = new HashMap<>();

	private Map<String, String> parameterMap = new HashMap<>();

	public RequestLine(String requestLine) {
		log.debug("request line : {}", requestLine);
		String[] tokens = requestLine.split(" ");
		
		if (tokens.length != 3) {
			throw new IllegalArgumentException(requestLine + "이 형식에 맞지 않습니다.");
		}
		
		method = HttpMethod.valueOf(tokens[0]);
		if (method.isPost()) {
			path = tokens[1];
			return;
		}
		
		int index = tokens[1].indexOf("?");
		if (index == -1) {
			path = tokens[1];
		} else {
			path = tokens[1].substring(0, index);
			parameterMap = HttpRequestUtils.parseQueryString(tokens[1].substring(index+1));
		}
	}
	
	public HttpMethod getMethod() {
		return method;
	}
	public String getPath() {
		return path;
	}
	public Map<String, String> getParameterMap() {
		return parameterMap;
	}
}

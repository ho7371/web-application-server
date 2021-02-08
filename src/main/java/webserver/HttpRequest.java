package webserver;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;
import util.IOUtils;

public class HttpRequest {

	private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

	public String path;

	private Map<String, String> headerMap = new HashMap<>();

	private Map<String, String> parameterMap = new HashMap<>();
	
	private RequestLine requestLine;

	public String getPath() {
		return requestLine.getPath();
	}

	public HttpMethod getMethod() {
		return requestLine.getMethod();
	}

	public String getHeader(String name) {
		return headerMap.get(name);
	}

	public String getParameter(String name) {
		return parameterMap.get(name);
	}

	public HttpRequest(InputStream in) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String line = br.readLine();

			if (line == null) {
				return;
			}

			requestLine = new RequestLine(line);

			line = br.readLine();
			while (!line.isBlank()) {
				log.debug("header:{}", line);
				String[] tokens = line.split(":");

				headerMap.put(tokens[0].trim(), tokens[1].trim());

				line = br.readLine();
			}
			
			if (requestLine.getMethod().isPost()) {
				String body = IOUtils.readData(br, Integer.parseInt(headerMap.get("Content-Length")));
				parameterMap = HttpRequestUtils.parseQueryString(body);
			} else {
				parameterMap = requestLine.getParameterMap();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public boolean isLogin() {
		Map<String, String> cookies = HttpRequestUtils.parseCookies(headerMap.get("Cookie"));
		String value = cookies.get("logined");
		if (value == null) {
			return false;
		}
		return Boolean.parseBoolean(value);
	}
}

package webserver;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import util.HttpRequestUtils;
import util.IOUtils;

public class HttpRequest {

	private InputStream inputStream;

	private String url;
	private String method;
	private String httpVersion;

	private Map<String, String> headerMap = new HashMap<>();
	private Map<String, String> parameterMap = new HashMap<>();


	public HttpRequest(InputStream inputStream) {
		this.inputStream = inputStream;
		initialize();
	}

	private void initialize () {
		try {
			InputStreamReader	isr	= new InputStreamReader(inputStream);
			BufferedReader		br	= new BufferedReader(isr);

			String requestLine = br.readLine();

			parseRequestLine(requestLine);

			parseHeaderMap(br);

			if ("GET".equals(method)) {
				
				parseGETParameterMap(url);
				
			} else if ("POST".equals(method)) {
				
				parsePOSTParameterMap(br);
				
			}
		} catch (Exception e) {
			
		}
	}

	private void parseRequestLine(String requestLine) {
		if (requestLine == null || "".equals(requestLine.trim())) {
			return;
		}

		String[] tokens	= requestLine.split(" ");
		method = tokens[0];
		url = tokens[1];
		httpVersion = tokens[2];
	}
	
	private void parseHeaderMap(BufferedReader br) throws Exception {
		String line = null;

		while ((line = br.readLine()) != null && !line.isEmpty()) {
			String[] arr = line.split(": ");
			headerMap.put(arr[0], arr[1]);
		}
	}

	private void parseGETParameterMap(String url) {
		int indexOfQuestion = url.indexOf("?");									// 1. 쿼리스트링을 추출한다.

		String queryString = url.substring(indexOfQuestion + 1, url.length());	// (index + 1을 하지 않으면 ?가 포함된다.)

		this.parameterMap = HttpRequestUtils.parseQueryString(queryString);		// 2. 쿼리스트링에서 파라미터를 추출한다.
	}

	private void parsePOSTParameterMap(BufferedReader br) throws Exception {
		String contentLength = getHeader("Content-Length");		// 1. 헤더에서 Content-Length 값을 추출한다.

		String messageBody = IOUtils.readData(br, Integer.parseInt(contentLength));		// 2. request에서 빈 라인 \r\n 을 기준으로 MessageBody를 Content-Length만큼 추출한다.

		this.parameterMap = HttpRequestUtils.parseQueryString(messageBody);		// 3. MessageBody에서 파라미터를 추출한다.
	}

	public String getUrl() {
		return url;
	}

	public String getMethod() {
		return method;
	}

	public String getHttpVersion() {
		return httpVersion;
	}

	public String getHeader(String key) {
		return headerMap.get(key);
	}

	public String getParameter(String key) {
		return parameterMap.get(key);
	}
}

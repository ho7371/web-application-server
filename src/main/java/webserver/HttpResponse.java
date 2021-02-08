package webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;

public class HttpResponse {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

	private DataOutputStream dos = null;

	private Map<String, String> headerMap = new HashMap<>();

	public HttpResponse(OutputStream out) {
		dos = new DataOutputStream(out);
	}

	public void addHeader(String key, String value) {
		headerMap.put(key, value);
	}

	private void processHeaders() {
		try {
			Set<String> keys = headerMap.keySet();
			for (String key : keys) {
				dos.writeBytes(key + ": " + headerMap.get(key) + " \r\n");
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void response200Header(int contentLength) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			processHeaders();
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void responseBody(byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.writeBytes("\r\n");
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	public void forward(String url) {
		try {
			byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());

			if (url.endsWith(".css")) {
				headerMap.put("Content-Type", "text/css");
			} else if (url.endsWith(".js")) {
				headerMap.put("Content-Type", "application/javascript");
			} else if (url.endsWith(".html")) {
				headerMap.put("Content-Type", "text/html;charset=urf-8");
			}

			headerMap.put("Content-Length", String.valueOf(body.length));
			response200Header(body.length);
			responseBody(body);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	public void forwardBody(String body) {
		byte[] contents = body.getBytes();
		headerMap.put("Content-Type", "text/html;charset=urf-8");
		headerMap.put("Content-Length", String.valueOf(contents.length));
		response200Header(contents.length);
		responseBody(contents);
	}

	public void sendRedirect(String redirectUrl) {
		try {
			dos.writeBytes("HTTP/1.1 302 Found \r\n");
			processHeaders();
			dos.writeBytes("Location: " + redirectUrl + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	
	


/*
	public void setCookie(String cookieName, String value) {
		String cookies = headerMap.get("Set-Cookie");
		String retValue = "";

		if (cookies == null) {
			retValue = cookieName + "=" + value;
		} else {
			Map<String, String> cookieMap = HttpRequestUtils.parseCookies(cookies);
			cookieMap.put(cookieName, value);
			
			Iterator<String> iter = cookieMap.keySet().iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				String val = cookieMap.get(key);
				
				retValue += key + "=" + val + "&";
			}
			
			retValue = new String(retValue.substring(0, retValue.lastIndexOf("&")));
		}
		
		headerMap.put("Set-Cookie", retValue);
	}
*/


}

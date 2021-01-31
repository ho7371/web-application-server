package webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;

public class HttpResponse {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
	private DataOutputStream dos;

	private String httpVersion = "HTTP/1.1";
	
	private int statusCode;
	
	private String statusMessage;
	
	private Map<String, String> headerMap = new HashMap<>();
	
	private int DEFAULT_STATUS_CODE = 200;
	
	private byte[] body = "Hello World".getBytes();
	
	
	public HttpResponse(OutputStream out) {
		dos = new DataOutputStream(out);
		initialize();
	}

	private void initialize() {
		setStatusCode(DEFAULT_STATUS_CODE);
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
		
		if (200 == statusCode) {
			this.statusMessage = "OK";
		} else if (302 == statusCode) {
			this.statusMessage = "Found";
		}
	}

	public void addHeader(String key, String value) {
		headerMap.put(key, value);
	}

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

	public void setBody(byte[] body) {
		this.body = body;
		addHeader("Content-Length", String.valueOf(body.length));
	}

	public void forward(String url) {
		try {
			String filePath = "./webapp" + url;
			body = Files.readAllBytes(new File(filePath).toPath());
			response();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendRedirect(String location) {
		setStatusCode(302);
		headerMap.put("Location", location);
		response();
	}

	public void response() {
		try {
			createResponseLine();

			createResponseHeader();

			createResponseBody();

			dos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createResponseLine() {
		try {
			String responseLine = 
					httpVersion + " "
					+ statusCode + " "
					+ statusMessage + " \r\n";

			dos.writeBytes(responseLine);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createResponseHeader() {
		try {
			Iterator<String> iterator = headerMap.keySet().iterator();

			while (iterator.hasNext()) {
				String key = iterator.next();
				String value = headerMap.get(key);
				dos.writeBytes(key + ": " + value + " \r\n");
			}

			dos.writeBytes("\r\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createResponseBody() {
		try {
			dos.write(body, 0, body.length);
			dos.writeBytes("\r\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

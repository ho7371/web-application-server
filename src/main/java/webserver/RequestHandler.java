package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import db.DataBase;
import model.User;
import util.IOUtils;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
	
	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run2() {
		// request 에서 읽어들인다.
			// 1) header를 읽는다.
			// 2) body를 읽는다.

		// url에 따라 분기한다.

		// 각 분기에 맞는 처리를 한다.
			// 1) 파일 출력
			// 2) 처리

		// response를 작성하여 보낸다.
			// 1) header를 작성한다.
			// 2) body를 작성한다.
	}
	
	public void run() {
		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
			DataOutputStream dos = new DataOutputStream(out);
			InputStreamReader reader = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(reader);
			
			String		requestLine	= br.readLine();
			String[]	tokens		= requestLine.split(" ");
			String		method		= tokens[0];
			String		url			= tokens[1];
			String		httpVersion	= tokens[2];
			Map<String, String> requestHeaderMap = null;
			
			StringBuilder responseHeader = new StringBuilder();
			byte[] responseBody = null;
			String responseStatusCode = "200";
			String responseStatusMessage = "OK";
			Map<String, String> responseHeaderMap = null;
			
			User user = null;
			
			if (url.equals("/index.html") || url.equals("/user/form.html") || url.equals("/user/login.html") || url.equals("/user/login_failed.html")) {
				requestHeaderMap = getHeader(br);
				String cookieStr = requestHeaderMap.get("Cookie");
				
				if (!"".equals(cookieStr)) {
					String[] param = cookieStr.split("[=]");
					requestHeaderMap.put(param[0], param[1]);
				}
				
				responseBody = Files.readAllBytes(new File("./webapp" + url).toPath());
			} else if (url.startsWith("/user/create")) {
				if ("GET".equals(method)) {
					int index = url.indexOf("?");
					String requestPath = url.substring(0, index);
					String params = url.substring(index+1);
					user = parseUser(params);
				} else if ("POST".equals(method)) {
					requestHeaderMap = getHeader(br);
					int contentLength = Integer.parseInt(requestHeaderMap.get("Content-Length"));
					String params = getBody(br, contentLength);
					user = parseUser(params);
				}
				
				DataBase.addUser(user);
				
				responseStatusCode = "302";
				responseStatusMessage = "Found";
				responseHeader.append("Location: http://210.97.178.50:9999/index.html \r\n");
				
			} else if (url.startsWith("/user/login")) {
				
				requestHeaderMap = getHeader(br);
				int contentLength = Integer.parseInt(requestHeaderMap.get("Content-Length"));
				String params = getBody(br, contentLength);
				User currentUser = parseUser(params);
				
				User user2 = DataBase.findUserById(currentUser.getUserId());
				boolean logined = user2 != null && currentUser.getPassword().equals(user2.getPassword());
				
				if (logined) {
					responseHeader.append("Set-Cookie: logined=true \r\n");
					responseHeader.append("Location: http://210.97.178.50:9999/index.html \r\n");
				} else {
					responseHeader.append("Set-Cookie: logined=false \r\n");
					responseHeader.append("Location: http://210.97.178.50:9999/user/login_failed.html \r\n");
				}
			
				responseStatusCode = "302";
				responseStatusMessage = "Found";
				
			} else {
				responseStatusCode = "404";
				responseStatusMessage = "NotFound";
			}
			
			response200Header(dos, responseBody.length, responseStatusCode, responseStatusMessage);
			responseBody(dos, responseBody);
			
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private Map<String, String> getHeader(BufferedReader br) throws IOException {
		Map<String, String> map = new HashMap<>();
		
		String line = null;
		while ((line = br.readLine()) != null && !Strings.isNullOrEmpty(line)) {
			String[] arr = line.split("[:][ ]");
			String key = arr[0];
			String value = arr[1];
			
			map.put(key, value);
		}
		return map;
	}
	
	private String getBody(BufferedReader br, int contentLength) throws IOException {
		return IOUtils.readData(br, contentLength);
	}
	
	private User parseUser(String params) {
		Map<String, String> map = util.HttpRequestUtils.parseQueryString(params);
		return new User( map.get("userId"), map.get("password"), map.get("name"), "");
	}
	
	private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String statusCode, String statusMessage) {
		String delimiter = " ";
		try {
			dos.writeBytes("HTTP/1.1" + delimiter + statusCode + delimiter + statusMessage + delimiter + "\r\n");
			dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			if ("302".equals(statusCode)) {
				dos.writeBytes("Location: http://210.97.178.50:9999/index.html \r\n");
			}
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
}

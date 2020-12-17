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

import model.User;
import util.IOUtils;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
	
	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		/* 참고사항 : InputSteam > InputStreamReader > BufferedReader
		// 1byte 읽기 ("a")		: InputStream
		int a = in.read();
		System.out.println(a);
		
		// 3byte 읽기	 ("abc")	: InputStream
		byte[] b = new byte[3];
		in.read(b);
		System.out.println(b[0]);
		System.out.println(b[1]);
		System.out.println(b[2]);
		
		// 문자로 읽기				: InputStreamReader(new InputStream)
		InputStreamReader reader = new InputStreamReader(in);
		char[] c = new char[3];
		reader.read(c);
		System.out.println(c);
		
		// 한 줄(행) 단위로 읽기			: BufferedReader(new InputStreamReader(new InputStream))
		BufferedReader br = new BufferedReader(reader);
		String d = br.readLine();
		System.out.println(d);
	*/
		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
			DataOutputStream dos = new DataOutputStream(out);
			InputStreamReader reader = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(reader);
			
			String requestLine = br.readLine();
			String[] tokens = requestLine.split(" ");
			String method = tokens[0];
			String url = tokens[1];
			String httpVersion = tokens[2];
			
			byte[] body = null;
			User user = null;
			Map<String, String> headerMap = null;
			
			if (url.equals("/index.html") || url.equals("/user/form.html")) {
				body = Files.readAllBytes(new File("./webapp" + url).toPath());
			} else if (url.startsWith("/user/create")) {
				
				if ("GET".equals(method)) {
					int index = url.indexOf("?");
					String requestPath = url.substring(0, index);
					String params = url.substring(index+1);
					user = parseUser(params);
				} else if ("POST".equals(method)) {
					headerMap = getHeader(br);
					int contentLength = Integer.parseInt(headerMap.get("Content-Length"));
					String params = getBody(br, contentLength);
					user = parseUser(params);
				}
				
				System.out.println(user.toString());
				
			} else {
				body = "Hello ZINO".getBytes();
			}
			
			response200Header(dos, body.length);
			responseBody(dos, body);
			
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
	
	private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
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

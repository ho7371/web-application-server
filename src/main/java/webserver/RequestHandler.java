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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		log.debug("New Client Connected! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());

		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
			InputStreamReader	isr	= new InputStreamReader(in);
			BufferedReader		br	= new BufferedReader(isr);
			DataOutputStream	dos	= new DataOutputStream(out);

			// TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
			String requestLine = br.readLine();

			if (requestLine == null || "".equals(requestLine.trim())) {
				return;
			}

			String[]	tokens	= requestLine.split(" ");
			String		url		= tokens[1];

			byte[] body = "Hello World".getBytes();

			if (url.endsWith(".html")) {
				String filePath = "./webapp" + url;
				body = Files.readAllBytes(new File(filePath).toPath());
			} else if (url.startsWith("/user/create")) {
				int indexOfQuestion = url.indexOf("?");
				// index + 1을 하지 않으면 ?가 포함된다.
				String queryString = url.substring(indexOfQuestion + 1, url.length());
				Map<String, String> paramMap = HttpRequestUtils.parseQueryString(queryString);
				
				String userId	= paramMap.get("userId");
				String password	= paramMap.get("password");
				String name		= paramMap.get("name");
				String email	= paramMap.get("email");
				
				DataBase.addUser(new User(userId, password, name, email));
				
				log.debug("회원가입에 성공했습니다. 유저 = {}", DataBase.findUserById(userId));
			}

			response200Header(dos, body.length);
			responseBody(dos, body);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void response200Header(DataOutputStream dos, int contentLength){
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + contentLength +" \r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e ) {
			log.error(e.getMessage());
		}
	}

	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.writeBytes("\r\n");
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}

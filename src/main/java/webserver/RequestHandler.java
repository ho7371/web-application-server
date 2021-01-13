package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
//		log.debug("New Client Connected! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());

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
			String		method	= tokens[0];
			String		url		= tokens[1];

			byte[] body = "Hello World".getBytes();

			if (url.endsWith(".html")) {
				String filePath = "./webapp" + url;
				body = Files.readAllBytes(new File(filePath).toPath());
			} else if (url.startsWith("/user/create")) {
				if ("GET".equals(method)) {
					int indexOfQuestion = url.indexOf("?");
					// index + 1을 하지 않으면 ?가 포함된다.
					String queryString = url.substring(indexOfQuestion + 1, url.length());
					Map<String, String> paramMap = HttpRequestUtils.parseQueryString(queryString);

					// 4. User 객체를 생성 & 저장한다.
					String userId	= paramMap.get("userId");
					String password	= paramMap.get("password");
					String name		= paramMap.get("name");
					String email	= paramMap.get("email");

					DataBase.addUser(new User(userId, password, name, email));
					
					log.debug("회원가입에 성공했습니다. 유저 = {}", DataBase.findUserById(userId));
				} else if ("POST".equals(method)) {

					// 1. 헤더에서 Content-Length 값을 추출한다.
					Map<String, String> headerMap = new HashMap<>();
					String line = null;

					while ((line = br.readLine()) != null && !line.isBlank()) {
						String[] arr = line.split(": ");
						headerMap.put(arr[0], arr[1]);
					}

					String contentLength = headerMap.get("Content-Length");

					// 2. request에서 빈 라인 \r\n 을 기준으로 MessageBody를 Content-Length만큼 추출한다.
					String messageBody = IOUtils.readData(br, Integer.parseInt(contentLength));

					// 3. 추출한 MessageBody에서 파라미터를 파싱한다.
					Map<String, String> paramMap = HttpRequestUtils.parseQueryString(messageBody);

					// 4. User 객체를 생성 & 저장한다.
					String userId	= paramMap.get("userId");
					String password	= paramMap.get("password");
					String name		= paramMap.get("name");
					String email	= paramMap.get("email");

					DataBase.addUser(new User(userId, password, name, email));

					log.debug("회원가입에 성공했습니다. 유저 = {}", DataBase.findUserById(userId));
				}
				
			}
			// 6. 정상적으로 응답한다.
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

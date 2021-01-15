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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
				// 로그인 쿠키 확인
				String line = null;
				while ((line = br.readLine()) != null && !line.isBlank()) {
					String[] arr = line.split(": ");
					if ("Cookie".equals(arr[0])) {
						log.debug(">>>>> 로그인 쿠키가 있습니다. {}", arr[1]);
					}
				}

				String filePath = "./webapp" + url;
				body = Files.readAllBytes(new File(filePath).toPath());

				// 정상적으로 응답한다.
				response200Header(dos, body.length);
				responseBody(dos, body);
			} else if (url.equals("/user/create")) {

				Map<String, String> paramMap = null;

				if ("GET".equals(method)) {

					// 1. 쿼리스트링을 추출한다.
					int indexOfQuestion = url.indexOf("?");
					// (index + 1을 하지 않으면 ?가 포함된다.)
					String queryString = url.substring(indexOfQuestion + 1, url.length());

					// 2. 쿼리스트링에서 파라미터를 추출한다.
					paramMap = HttpRequestUtils.parseQueryString(queryString);
					
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

					// 3. MessageBody에서 파라미터를 추출한다.
					paramMap = HttpRequestUtils.parseQueryString(messageBody);
				}

				// 4. User 객체를 생성 & 저장한다.
				String userId	= paramMap.get("userId");
				String password	= paramMap.get("password");
				String name		= paramMap.get("name");
				String email	= paramMap.get("email");

				DataBase.addUser(new User(userId, password, name, email));

				// 5. 회원가입 여부를 기록한다.
				if (DataBase.findUserById(userId) != null) {
					log.debug(">>>>> 회원가입에 성공했습니다. <<<<<");
				} else {
					log.debug("===== 회원가입에 실패 =====");
				}

				// 6. URL 리다이렉션을 통해 브라우저에 남아있는 회원가입 정보를 재사용하기 어렵게 처리한다.
				response302Header(dos);
				dos.flush();
			} else if (url.equals("/user/login")) {

				Map<String, String> paramMap = null;

				if ("GET".equals(method)) {
					// 1. 쿼리스트링을 추출한다.
					int indexOfQuestion = url.indexOf("?");
					// (index + 1을 하지 않으면 ?가 포함된다.)
					String queryString = url.substring(indexOfQuestion + 1, url.length());

					// 2. 쿼리스트링에서 파라미터를 추출한다.
					paramMap = HttpRequestUtils.parseQueryString(queryString);
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

					// 3. MessageBody에서 파라미터를 추출한다.
					paramMap = HttpRequestUtils.parseQueryString(messageBody);
				}

				// 4. User 
				String userId	= paramMap.get("userId");
				String password	= paramMap.get("password");

				User user = DataBase.findUserById(userId);

				String location = null;
				boolean logined = false;
				if (user == null) {
					log.debug("===== 해당하는 아이디가 없습니다. =====");
					location = "/user/login_failed.html";
				} else if (password.equals(user.getPassword()) == false) {
					log.debug("===== 비밀번호가 틀렸습니다. =====");
					location = "/user/login_failed.html";
				} else {
					log.debug(">>>>> 로그인에 성공했습니다. <<<<<");
					location = "/index.html";
					logined = true;
				}

				// 6. URL 리다이렉션을 통해 로그인 결과를 보여준다.
				response302HeaderWithLocationAndCookie(dos, logined, location);
				dos.flush();
			} else if (url.equals("/user/list")) {
				Map<String, String> cookieMap = null;

				//1. 로그인 정보를 헤더에서 뽑는다.
				String line = null;
				while ((line = br.readLine()) != null && !line.isBlank()) {
					String[] arr = line.split(": ");
					if ("Cookie".equals(arr[0])) {
						cookieMap = HttpRequestUtils.parseCookies(arr[1]);
					}
				}

				boolean logined = false;
				if (cookieMap != null) {
					logined = Boolean.parseBoolean(cookieMap.get("logined"));
				}

				if (logined) {
					log.debug("=== 로그인이라서 사용자를 출력합니다.");
					//2. 로그인 상태라면 사용자 목록을 불러온다. 사용자 목록을 출력한다.
					Collection<User> collection = DataBase.findAll();
					Iterator<User> it = collection.iterator();
					
					StringBuilder sb = new StringBuilder();
					while (it.hasNext()) {
						User user = it.next();
						sb.append(user.toString());
					}
					
					body = sb.toString().getBytes();
					response200HeaderWithCookie(dos, logined, body.length);
					responseBody(dos, body);
				} else {
					//3. 비로그인 상태라면 로그인페이지로 이동한다.
					String location = "/user/login.html";
					log.debug("=== 비로그인이라서 로그인페이지로 이동합니다.");
					response302HeaderWithLocation(dos, location);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 리다이렉션 (임시 이동)
	 * 응답 헤더 중 Location을 이용하여 리다이렉션할 위치를 지정한다.
	 * 리다이렉션을 통해 클라이언트측 브라우저의 URL창 경로도 바뀐다.
	 * @param dos
	 */
	private void response302Header(DataOutputStream dos){
		try {
			dos.writeBytes("HTTP/1.1 302 OK \r\n");
			dos.writeBytes("Location: /index.html \r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e ) {
			log.error(e.getMessage());
		}
	}
	private void response302HeaderWithLocation(DataOutputStream dos, String url){
		try {
			dos.writeBytes("HTTP/1.1 302 OK \r\n");
			dos.writeBytes("Location: " + url + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e ) {
			log.error(e.getMessage());
		}
	}

	private void response302HeaderWithLocationAndCookie(DataOutputStream dos, boolean logined, String location){
		try {
			dos.writeBytes("HTTP/1.1 302 OK \r\n");
			dos.writeBytes("Location: " + location +" \r\n");
			dos.writeBytes("Set-Cookie: logined=" + logined +" \r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e ) {
			log.error(e.getMessage());
		}
	}

	private void response200HeaderWithCookie(DataOutputStream dos, boolean logined, int contentLength){
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			dos.writeBytes("Set-Cookie: logined=" + logined +" \r\n");
			dos.writeBytes("Content-Length: " + contentLength +" \r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e ) {
			log.error(e.getMessage());
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

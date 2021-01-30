package webserver;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		log.debug("New Client Connected! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());

		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
			HttpRequest req = new HttpRequest(in);
			HttpResponse res = new HttpResponse(out);

			// 로그인 쿠키 확인
			boolean logined = false;
			String cookieStr = req.getHeader("Cookie");
			if (cookieStr != null) {
				String[] cookieParam = cookieStr.split("=");
				logined = Boolean.parseBoolean(cookieParam[1]);
			}

			String url = req.getPath();

			if (url.endsWith(".html") || url.endsWith(".css") || url.endsWith(".js")) {
				if (url.endsWith(".html"))		{ res.setHeader("Content-Type", "text/html;charset=utf-8");	}
				else if (url.endsWith(".css"))	{ res.setHeader("Content-Type", "text/css");				}
				else if (url.endsWith(".js"))	{ res.setHeader("Content-Type", "application/js");			}
				
				res.forward(url);
			} else if (url.equals("/user/create")) {
				addUser(req);
				res.sendRedirect("/index.html");
			} else if (url.equals("/user/login")) {
				String location = null;
				User user = getUser(req);
				
				if (user == null) {
					location = "/user/login_failed.html";
				} else {
					logined = true;
					location = "/index.html";
				}
				res.setCookie("logined", Boolean.toString(logined));
				res.sendRedirect(location);
			} else if (url.equals("/user/list")) {
				if (logined) {
					res.setCookie("logined", Boolean.toString(logined));
					res.setBody(getUserList().getBytes());
				} else {
					res.sendRedirect("/user/login.html");
				}
			}
			
			res.response();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void addUser(HttpRequest req) {
		String userId	= req.getParameter("userId");
		String password	= req.getParameter("password");
		String name		= req.getParameter("name");
		String email	= req.getParameter("email");

		DataBase.addUser(new User(userId, password, name, email));

		if (DataBase.findUserById(userId) != null) {
			log.debug(">>>>> 회원가입에 성공했습니다. <<<<<");
		} else {
			log.debug("===== 회원가입에 실패 =====");
		}
	}
	
	private User getUser (HttpRequest req) {
		String userId	= req.getParameter("userId");
		String password	= req.getParameter("password");

		User user = DataBase.findUserById(userId);
		
		if (user != null && password.equals(user.getPassword())) {
			return user;
		} else if (user == null) {
			log.debug("===== 해당하는 아이디가 없습니다. =====");
		} else if (user != null && password.equals(user.getPassword())) {
			log.debug("===== 비밀번호가 틀렸습니다. =====");
		}
		
		return null;
	}
	
	private String getUserList () {
		Collection<User> collection = DataBase.findAll();
		Iterator<User> it = collection.iterator();
		
		StringBuilder sb = new StringBuilder();
		while (it.hasNext()) {
			User user = it.next();
			sb.append(user.toString());
		}
		
		return sb.toString();
	}
}

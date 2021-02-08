package webserver;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

			String url = req.getPath();

			if (url.endsWith(".html") || url.endsWith(".css") || url.endsWith(".js")) {
				
				if (url.endsWith(".html"))		{
					res.addHeader("Content-Type", "text/html;charset=utf-8");
				} else if (url.endsWith(".css")) {
					res.addHeader("Content-Type", "text/css");
				} else if (url.endsWith(".js"))	{
					res.addHeader("Content-Type", "application/js");
				}
				res.forward(url);
				
			} else if (url.equals("/user/create")) {
				User user = new User(req.getParameter("userId"),
						req.getParameter("password"),
						req.getParameter("name"),
						req.getParameter("email"));
				
				log.debug("user : {}", user);
				
				DataBase.addUser(user);
				res.sendRedirect("/index.html");
				
			} else if (url.equals("/user/login")) {
				
				User user = DataBase.findUserById(req.getParameter("userId"));
				
				if (user != null) {
					if (user.login(req.getParameter("userId"))) {
						res.addHeader("Set-Cookie", "logined=true");
						res.sendRedirect("/index.html");
					} else {
						res.sendRedirect("/user/login_failed.html");
					}
				} else {
					res.sendRedirect("/user/login_failed.html");
				}
				
			} else if (url.equals("/user/list")) {
				if (!req.isLogin()) {
					res.sendRedirect("/usr/login.html");
					return;
				}
				
				Collection<User> users = DataBase.findAll();
				StringBuilder sb = new StringBuilder();
				sb.append("<table border='1'>");
				
				for (User user : users) {
					sb.append("<tr>");
					sb.append("<td>" + user.getUserId() + "</td>");
					sb.append("<td>" + user.getName() + "</td>");
					sb.append("<td>" + user.getEmail() + "</td>");
					sb.append("</tr>");
				}
				
				res.forwardBody(sb.toString());
			} else {
				res.forward(url);
			}
			
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public String getDefaultPath(String path) {
		if ("/".equals(path)) {
			return "index.html";
		}
		return path;
	}
}

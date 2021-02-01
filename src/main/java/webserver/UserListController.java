package webserver;

import java.util.Collection;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;

public class UserListController extends AbstractController implements Controller {
	
	private static final Logger log = LoggerFactory.getLogger(UserListController.class);
	
	@Override
	public void service(HttpRequest request, HttpResponse response) {
		doGet(request, response);
	}
	
	@Override
	public void doGet(HttpRequest request, HttpResponse response) {
		if (isLogin(request)) {
			response.setCookie("logined", Boolean.toString(true));
			response.setBody(getUserList().getBytes());
			response.response();
		} else {
			response.sendRedirect("/user/login.html");
		}
	}
	
	private boolean isLogin(HttpRequest request) {
		boolean logined = false;
		// 로그인 쿠키 확인
		String cookieStr = request.getHeader("Cookie");
		if (cookieStr != null) {
			String[] cookieParam = cookieStr.split("=");
			logined = Boolean.parseBoolean(cookieParam[1]);
		}
		return logined;
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

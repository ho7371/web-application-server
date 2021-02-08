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
//		if (request.isLogin()) {
//			response.setCookie("logined", Boolean.toString(true));
//			response.setBody(getUserList().getBytes());
//			response.response();
//		} else {
//			response.sendRedirect("/user/login.html");
//		}
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

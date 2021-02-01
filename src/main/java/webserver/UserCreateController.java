package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;

public class UserCreateController implements Controller {

	private static final Logger log = LoggerFactory.getLogger(UserCreateController.class);
	
	@Override
	public void service(HttpRequest request, HttpResponse response) {
		doPost(request, response);
	}
	
	public void doPost(HttpRequest request, HttpResponse response) {
		String userId	= request.getParameter("userId");
		String password	= request.getParameter("password");
		String name		= request.getParameter("name");
		String email	= request.getParameter("email");

		DataBase.addUser(new User(userId, password, name, email));

		if (DataBase.findUserById(userId) != null) {
			log.debug(">>>>> 회원가입에 성공했습니다. <<<<<");
		} else {
			log.debug("===== 회원가입에 실패 =====");
		}
		response.sendRedirect("/index.html");
	}
}

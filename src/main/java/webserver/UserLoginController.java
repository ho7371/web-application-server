package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;

public class UserLoginController extends AbstractController implements Controller {

	private static final Logger log = LoggerFactory.getLogger(UserLoginController.class);
	
	@Override
	public void service(HttpRequest request, HttpResponse response) {
		doPost(request, response);
	}
	
	public void doPost(HttpRequest request, HttpResponse response) {
		String location = null;
		
		String userId	= request.getParameter("userId");
		String password	= request.getParameter("password");

		User user = DataBase.findUserById(userId);
		
		if (user != null && password.equals(user.getPassword())) {
			location = "/index.html";
//			response.setCookie("logined", Boolean.toString(true));
		} else {
			location = "/user/login_failed.html";
			log.debug("===== 아이디가 없거나 비밀번호가 틀렸습니다. =====");
		}
	
		response.sendRedirect(location);
	}
}

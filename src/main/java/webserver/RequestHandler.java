package webserver;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;

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

			Map<String, Controller> controllerMap = new HashMap<>();

			if (url.endsWith(".html") || url.endsWith(".css") || url.endsWith(".js")) {
				controllerMap.put(req.getPath(), new ForwardController());
			} else if (url.equals("/user/create")) {
				controllerMap.put(req.getPath(), new UserCreateController());
			} else if (url.equals("/user/login")) {
				controllerMap.put(req.getPath(), new UserLoginController());
			} else if (url.equals("/user/list")) {
				controllerMap.put(req.getPath(), new UserListController());
			}
			
			Controller controller = controllerMap.get(req.getPath());
			if (controller != null) {
				controller.service(req, res);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getDefaultPath(String path) {
		if ("/".equals(path)) {
			return "index.html";
		}
		return path;
	}
}

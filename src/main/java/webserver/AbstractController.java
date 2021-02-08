package webserver;

public abstract class AbstractController implements Controller {

	@Override
	public void service(HttpRequest request, HttpResponse response) {
		
		if (request.getMethod().isGet()) {
			doGet(request, response);
		} else if (request.getMethod().isPost()) {
			doPost(request, response);
		}
	}
	
	public void doGet(HttpRequest request, HttpResponse response) {
		
	}
	
	public void doPost(HttpRequest request, HttpResponse response) {
		
	}
}

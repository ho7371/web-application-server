package webserver;

public abstract class AbstractController implements Controller {

	@Override
	public void service(HttpRequest request, HttpResponse response) {
		
		if ("GET".equals(request.getMethod())) {
			doGet(request, response);
		} else if ("POST".equals(request.getMethod())) {
			doPost(request, response);
		}
	}
	
	public void doGet(HttpRequest request, HttpResponse response) {
		
	}
	
	public void doPost(HttpRequest request, HttpResponse response) {
		
	}
}

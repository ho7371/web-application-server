package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForwardController implements Controller {
	
	private static final Logger log = LoggerFactory.getLogger(ForwardController.class);
	
	@Override
	public void service(HttpRequest request, HttpResponse response) {
		String url = request.getPath();
		
		if (url.endsWith(".html"))		{
			
			response.addHeader("Content-Type", "text/html;charset=utf-8");
			
		} else if (url.endsWith(".css")) {
			
			response.addHeader("Content-Type", "text/css");
			
		} else if (url.endsWith(".js"))	{
			
			response.addHeader("Content-Type", "application/js");
			
		}
		
		response.forward(url);
	}
}

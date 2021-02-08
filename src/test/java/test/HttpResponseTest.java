package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.junit.Test;

import webserver.HttpResponse;

public class HttpResponseTest {
	private String testDirectory = "./src/test/resource/";
	
	@Test
	public void responseForward() throws Exception {
		HttpResponse response = new HttpResponse(createOutputStream("Http_Forward.txt"));
		response.forward("/index.html");
	}
	
	@Test
	public void responseRedirect() throws Exception {
		HttpResponse response = new HttpResponse(createOutputStream("Http_Redirect.txt"));
		response.sendRedirect("/index.html");
	}
	
	@Test
	public void responseCookies() throws Exception{
		HttpResponse response = new HttpResponse(createOutputStream("Http_Cookie.txt"));
		response.addHeader("Set-Cookie", "logined=true");
		response.sendRedirect("/index.html");
	}
	
//	@Test
	private OutputStream createOutputStream(String fileName) throws FileNotFoundException{
		return new FileOutputStream(new File(testDirectory + fileName));
	}
}

package test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import webserver.HttpRequest;
import org.junit.Test;

public class HttpRequestTest {
	private String testDirectory = "./src/test/resource/";
	
	public void test() {
		String str = "/user/create?userId=zino&password=hhh111&name=HyunJinHo";
		int length = str.length();
		int indexOfQ = str.indexOf("?");
		
		System.out.println("index = " + indexOfQ);
		System.out.println("length = " + length);
		
		System.out.println(str.substring(indexOfQ + 1, length));
		System.out.println(str.substring(0, indexOfQ));
	}
	@Test
	public void request_GET() throws Exception {
		InputStream in = new FileInputStream(new File(testDirectory + "Http_GET.txt"));
		HttpRequest request = new HttpRequest(in);
		
		
		assertEquals("GET", request.getMethod());
		assertEquals("/user/create", request.getPath());
		assertEquals("keep-alive", request.getHeader("Connection"));
		assertEquals("zino", request.getParameter("userId"));
	}
	
	@Test
	public void request_POST() throws Exception {
		InputStream in = new FileInputStream(new File(testDirectory + "Http_POST.txt"));
		HttpRequest request = new HttpRequest(in);
		
		assertEquals("POST", request.getMethod());
		assertEquals("/user/create", request.getPath());
		assertEquals("keep-alive", request.getHeader("Connection"));
		assertEquals("zino", request.getParameter("userId"));
	}
}

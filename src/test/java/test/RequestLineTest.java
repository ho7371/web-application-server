package test;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import webserver.HttpMethod;
import webserver.RequestLine;

public class RequestLineTest {

	@Test
	public void create_method() {
		RequestLine line = new RequestLine("GET /index.html HTTP/1.1");
		
		assertEquals(HttpMethod.GET, line.getMethod());
		assertEquals("/index.html", line.getPath());

		line = new RequestLine("POST /index.html HTTP/1.1");
		assertEquals(HttpMethod.POST, line.getMethod());
		assertEquals("/index.html", line.getPath());
	}
	
	@Test
	public void create_and_parameterMap() {
		RequestLine line = new RequestLine("GET /user/create?userId=zino&password=hhh111 HTTP/1.1");
		assertEquals(HttpMethod.GET, line.getMethod());
		assertEquals("/user/create", line.getPath() );
		
		Map<String, String> params = line.getParameterMap();
		assertEquals(2, params.size());
	}
}

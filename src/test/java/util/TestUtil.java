package util;

import org.junit.Test;

public class TestUtil {

	@Test
	public void StringTest() {
		String str = "GET /index.html HTTP/1.1";
		String[] arr = str.split(" ");
		System.out.println(arr[1]);
	}
}

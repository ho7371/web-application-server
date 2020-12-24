package util;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.google.common.base.Strings;

import model.User;

public class ZinoTest {

	@Test
	public void regex() {
		String str = "/user/index.html";
		
	}
	
	public void post_header() {
		String str = "Content-Length: 59";
		String[] arr = str.split("[:][ ]");
		System.out.println(arr[0]);
		System.out.println(arr[1]);
	}
	
	/* 문자열을 Split 할 떄는 delimiter를 []로 감싸서 명확하게 표현한다.  */
	public void 물음표_text() {
		String str = "/user/create?userId=javajigi&password=password&name=JaeSung";
		String[] arr = str.split("[?]");
		System.out.println(arr[0]);
		System.out.println(arr[1]);
	}
	
	/* null이나 empty체크용 유틸은 이미 있었다. */
	public void 문자열_체크() {
		assertTrue(Strings.isNullOrEmpty(""));
	}
	
	public void http리퀘스트_파싱() {
		String queryStr = "userId=javajigi&password=password&name=JaeSung";
		Map<String, String> map = util.HttpRequestUtils.parseQueryString(queryStr);
		
		Map<String, Object> expected = new HashMap<>();
		expected.put("userId", "javajigi");
		expected.put("password", "password");
		expected.put("name", "JaeSung");
		
		assertTrue(expected.equals(map));
	}

	/* 두 Map의 엔트리를 비교할 때는 map.equals()를 사용한다. */
	public void 회원가입_파싱() {
		String str = "/user/create?userId=javajigi&password=password&name=JaeSung";
		
		String[] arr = str.split("[?]");
		String url = arr[0];
		String queryString = arr[1];
		String[] params = queryString.split("[&]");
		
		Map<String, Object> user = new HashMap<>();
		
		for (int i=0; i < params.length; i++) {
			String paramStr = params[i];
			String[] paramArr = paramStr.split("=");
			String key = paramArr[0];
			String value = paramArr[1];
			
			user.put(key, value);
		}
		
		Map<String, Object> expected = new HashMap<>();
		expected.put("userId", "javajigi");
		expected.put("password", "password");
		expected.put("name", "JaeSung");
		
		assertTrue(expected.equals(user));
	}
}

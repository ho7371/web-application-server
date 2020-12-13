package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
	
	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		/* 참고사항 : InputSteam > InputStreamReader > BufferedReader
		// 1byte 읽기 ("a")		: InputStream
		int a = in.read();
		System.out.println(a);
		
		// 3byte 읽기	 ("abc")	: InputStream
		byte[] b = new byte[3];
		in.read(b);
		System.out.println(b[0]);
		System.out.println(b[1]);
		System.out.println(b[2]);
		
		// 문자로 읽기				: InputStreamReader(new InputStream)
		InputStreamReader reader = new InputStreamReader(in);
		char[] c = new char[3];
		reader.read(c);
		System.out.println(c);
		
		// 한 줄(행) 단위로 읽기			: BufferedReader(new InputStreamReader(new InputStream))
		BufferedReader br = new BufferedReader(reader);
		String d = br.readLine();
		System.out.println(d);
	*/
		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
			DataOutputStream dos = new DataOutputStream(out);
			
			InputStreamReader reader = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(reader);
			
			String line = null;
			
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				
//				Matcher m = Pattern.compile("GET /(.*) HTTP/1.1").matcher(line);
//				if (m.find()) {
//					fileName = m.group(1);
//				}
			}
			
			byte[] body = "Hello ZINO".getBytes();
			response200Header(dos, body.length);
			responseBody(dos, body);
			
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	/* 가장 기본영
	public void run() {
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());

		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
			// TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
			DataOutputStream dos = new DataOutputStream(out);
			byte[] body = "Hello ZINO".getBytes();
			response200Header(dos, body.length);
			responseBody(dos, body);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}*/
	private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}

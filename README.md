# 실습을 위한 개발 환경 세팅
* https://github.com/slipp/web-application-server 프로젝트를 자신의 계정으로 Fork한다. Github 우측 상단의 Fork 버튼을 클릭하면 자신의 계정으로 Fork된다.
* Fork한 프로젝트를 eclipse 또는 터미널에서 clone 한다.
* Fork한 프로젝트를 eclipse로 import한 후에 Maven 빌드 도구를 활용해 eclipse 프로젝트로 변환한다.(mvn eclipse:clean eclipse:eclipse)
* 빌드가 성공하면 반드시 refresh(fn + f5)를 실행해야 한다.

# 웹 서버 시작 및 테스트
* webserver.WebServer 는 사용자의 요청을 받아 RequestHandler에 작업을 위임하는 클래스이다.
* 사용자 요청에 대한 모든 처리는 RequestHandler 클래스의 run() 메서드가 담당한다.
* WebServer를 실행한 후 브라우저에서 http://localhost:8080으로 접속해 "Hello World" 메시지가 출력되는지 확인한다.

#### 학습내용
* 자바 프로그램에서 직접 클라이언트의 request에 응답할 수 있다.

```java
	int port = 8080;
	ServerSocket serverSocket = new ServerSocket(port);
	Socket socket = serverSocket.accept();
	RequestHandler requestHandler = new RequestHandler(socket);
	requestHandler.start();
```

# 각 요구사항별 학습 내용 정리
* 구현 단계에서는 각 요구사항을 구현하는데 집중한다. 
* 구현을 완료한 후 구현 과정에서 새롭게 알게된 내용, 궁금한 내용을 기록한다.
* 각 요구사항을 구현하는 것이 중요한 것이 아니라 구현 과정을 통해 학습한 내용을 인식하는 것이 배움에 중요하다. 

### 요구사항 1 - http://localhost:8080/index.html로 접속시 응답

#### 학습 내용
* 클라이언트가 보낸 request를 받는 것도, 클라이언트에게 response를 보내는 것도 I/O이다.  
이에 대한 InputStream과 OutputStream은 소켓에서 구한다.  

```java
	InputStream in = socket.getInputStream();
	OutputStream out = socket.getOutputStream();
```

* HttpResponse의 header도, body도 별거 아니고, 그냥  아웃풋으로 출력하는 것이다.

```java
	DataOutputStream dos = new DataOutputStream(out);
	dos.writeBytes("HTTP/1.1 200 OK \r\n");
```

* try-with-resource 를 이용하면 자원해제를 쉽게 할 수 있다.  

```java
	try ( InputStream in = connection.getInputStream();
		OutputStream out = connection.getOutputStream() ) {
		// 코드작성
	}
```

* InputStream 과 OutputStream에 대해서 알게 되었다.  
InputSteam > InputStreamReader > BufferedReader 으로 확장되며  
결과적으로 <addr>BufferedReader(new InputStreamReader(new InputStream))</addr> 이렇게 사용된다.  

```java
	InputStream in = connection.getInputStream();
	InputStreamReader reader = new InputStreamReader(in);
	BufferedReader br = new BufferedReader(reader);
```

* read 관련 함수의 가장 원형이 되는 InputStream의 read()는 1 byte 단위로 읽는다.  
문자단위로 읽으려면 InputStreamReader의 read()를 ,  
한 줄 단위로 읽으려면 BufferedReader의 readLine()을 이용한다.  

```java
	// 1byte 읽기 ("a")		: InputStream
	int a = in.read();
	System.out.println(a);
	
	// 3byte 읽기	("abc")	: InputStream
	byte[] b = new byte[3];
	in.read(b);
	System.out.println(b[0]);
	System.out.println(b[1]);
	System.out.println(b[2]);
	
	// 문자로 읽기		: InputStreamReader(new InputStream)
	InputStreamReader reader = new InputStreamReader(in);
	char[] c = new char[3];
	reader.read(c);
	System.out.println(c);
	
	// 한 줄(행) 단위로 읽기	: BufferedReader(new InputStreamReader(new InputStream))
	BufferedReader br = new BufferedReader(reader);
	String d = br.readLine();
	System.out.println(d);
```

* 경로에 맞는 파일을 불러온다.

```java
	String url = "/index.html";
	byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
```

### 요구사항 2 - get 방식으로 회원가입
* requestLine에 메소드, 파라미터가 모두 들어있기 때문에 readLine 한번만 하면 필요한 데이터를 모두 얻는다.  
여기서는 requestLine에서 파라미터를 분리하여 파싱하는 것이 포인트다.

### 요구사항 3 - post 방식으로 회원가입
* post로 들어오는 request는 requestLine, 헤더 영역, 본문 영역이 분리되어 있다.  
그래서 각 영역마다 파라미터를 분리하여 원하는 데이터를 추출해야 한다.  
get 메소드와 분기도 처리해줘야 한다.

### 요구사항 4 - redirect 방식으로 이동
* 

### 요구사항 5 - cookie
* 

### 요구사항 6 - stylesheet 적용
* 

### heroku 서버에 배포 후
* 

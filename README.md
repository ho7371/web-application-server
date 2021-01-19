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

* 서버는 웹 페이지를 구성하는 모든 자원들을 한꺼번에 응답으로 보내지 않는다. 서버가 HTML만 응답하면 브라우저는 HTML을 분석하여 css, js, 이미지 등의 자원이 포함되어 있으면 해당 자원을 다시 서버에게 요청한다.


* 클라이언트가 보낸 request를 받는 것도, 클라이언트에게 response를 보내는 것도 I/O이다. 이에 대한 InputStream과 OutputStream은 소켓에서 구한다.

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

* InputStream 과 OutputStream에 대해서 알게 되었다. InputSteam > InputStreamReader > BufferedReader 으로 확장되며 결과적으로 아래와 같이 사용된다.

```java
	InputStream in = connection.getInputStream();
	InputStreamReader reader = new InputStreamReader(in);
	BufferedReader br = new BufferedReader(reader);
```

* read 관련 함수의 가장 원형이 되는 InputStream의 read()는 1 byte 단위로 읽는다. 문자단위로 읽으려면 InputStreamReader의 read()를 , 한 줄 단위로 읽으려면 BufferedReader의 readLine()을 이용한다. 

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

# 각 요구사항별 학습 내용 정리
* 구현 단계에서는 각 요구사항을 구현하는데 집중한다.


* 구현을 완료한 후 구현 과정에서 새롭게 알게된 내용, 궁금한 내용을 기록한다.


* 각 요구사항을 구현하는 것이 중요한 것이 아니라 구현 과정을 통해 학습한 내용을 인식하는 것이 배움에 중요하다. 

### 요구사항 1 - http://localhost:8080/index.html로 접속시 응답

##### Client에서 서버로 전송하는 데이터의 모습을 로그로 출력해보고 확인해보자.
* 한 번의 클라이언트를 요청했는데, 2 번의 요청이 들어온 것을 확인할 수 있다.


* 그 2번의 클라이언트 요청은 클라이언트의 서로 다른 포트에서 넘어온 것이다.


* 서버는 각 요청에 대해 순차적으로 실행하는 것이 아니라, 쓰레드를 이용해 비동기적으로 실행한다.


* 각 요청의 마지막은 빈 문자열 "" 로 구성되어 있다.

##### HTTP 규약

##### HTTP Request

```json
	POST /user/create HTTP/1.1							[요청라인] (필수)
	HOST: localhost:8080								[요청헤더]
	Connection-Length: 59								[요청헤더]
	Content-Type: application/x-www-form-urlencoded		[요청헤더]
	Accept: */*											[요청헤더]
														[공백라인]
	userId=zino&password=abcd1234						[요청본문] (Optional)
```

##### 요청라인

* 각 요청의 첫번쨰 라인은 [Http Method]+[URI]+[Http version] 형식으로 구성되어 있다.


* [Http Method]는 요청의 종류를 나타낸다.


* [URI]는 클라이언트가 서버에 유일하게 식별할 수 있는 요청 자원의 경로를 나타낸다.	URI = URL 이라고 봐도 무방하다.


* [Http version]는 현재의 HTTP 버전을 의미한다.

##### 요청헤더

* 나머지 요청 데이터는 [필드이름 : 필드값] 형태로 구성되어 있다.


* 필드 하나에 여러 필드값을 전달하고 싶다면, [필드이름 : 필드값1, 필드값2]처럼 쉼표 구분자로 전달한다.


##### HTTP Response

```json
	HTTP/1.1 200 OK							[상태라인] (필수)
	Content-Type: text/html;charset=utf-8	[응답헤더]
	Connection-Length: 20					[응답헤더]
											[공백라인] (필수)
	<h1>Hello World</h1>					[요청본문] (Optional)
```

##### 상태 라인

* 응답의 첫번쨰 라인은 [Http version]+[상태코드]+[응답구문] 형식으로 구성되어 있다.


* 상태코드는 200(성공), 302(임시이동) 외에도 다양한 상태코드가 있다.


##### 작업 내용

* 요청라인에서 URI를 분리하여, "index.html" 이라는 경로를 추출해낸다.

```java
	String url = requestLine.split(" ")[1];
```

* 프로젝트에서 index.html 파일을 바이트 배열로 읽어들여서 리턴해준다.

```java
	byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
	response200Header(dos, body);
	responseBody(dos, body);
```

##### 배운 내용

* 우리가 서버로 요청한 것은 1번인데, 서버가 받은 요청의 개수는 매우 많다.

```java
	GET /index.html HTTP/1.1
	GET /css/bootstrap.min.css HTTP/1.1
	GET /css/styles.css HTTP/1.1
	GET /js/jquery-2.2.0.min.js HTTP/1.1
	GET /js/scripts.js HTTP/1.1
	GET /favicon.ico HTTP/1.1
```

### 요구사항 2 - get 방식으로 회원가입

```java
	GET /user/create?userId=zino&password=abcd1234&name=jinho&email=zino@naver.com HTTP/1.1
```

* GET 방식에서 매개변수는 URI에 포함되어 쿼리스트링의 형태로 서버로 전달된다. 쿼리스트링을 파싱하여 사용자가 보낸 입력값을 추출해야 한다.


* 문제는 이렇게 URI에 입력값을 포함하여 전달하면, 아래와 같은 단점이 있다.


* 1) URI는 브라우저 주소창에 노출되기 때문에 보다 보안적으로 취약하다.


* 2) request line은 길이 제한이 있기 때문에, 보낼 수 있는 값의 크기가 제한적이다.


* 정리하자면 GET 방식은 서버에 존재하는 데이터를 가져오기 위한 방식이지, 데이터의 상태를 변경하지 않는다.

### 요구사항 3 - post 방식으로 회원가입

```json
	POST /user/create HTTP/1.1
	HOST: localhost:8080
	Connection-Length: 59
	Content-Type: application/x-www-form-urlencoded
	Accept: */*	

	userId=zino&password=abcd1234&name=jinho
```

* GET 방식에서는 URI에 포함되어 전달되던 쿼리스트링이, 메시지 본문을 통해 전달된다. 메소드에 따라 분기처리를 해준다.


* 본문 데이터의 길이가 헤더에 Content-Length 라는 필드로 전달된다.


* 본문을 Content-Length 만큼 읽어서, 데이터를 파싱하면 된다.


* HTML은 기본적으로 GET과 POST 방식만을 지원한다.


* 1) HTML은 모든 자원( a태그 링크, css, javascript, image ..)을 GET방식으로 요청한다.


* 2) HTML의 Form 태그에서는 GET과 POST 방식만을 지원한다.


* 정리하자면 GET 방식은 서버에 존재하는 데이터를 조회하기 위한 방식이며, POST는 데이터의 상태를 변경하는 작업을 위한 방식이다.

* 그 이외의 PUT, DELETE 와 같은 방식은 REST API 설계와 ajax 기반으로 사용된다.

### 요구사항 4 - redirect 방식으로 이동
* 

### 요구사항 5 - cookie
* 

### 요구사항 6 - stylesheet 적용
* 

### heroku 서버에 배포 후
* 

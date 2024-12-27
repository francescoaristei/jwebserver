import org.example.http.HttpRequestHandler;
import org.example.http.HttpRequestLine;
import org.example.http.HttpRequestParser;
import org.example.http.errors.InvalidHttpRequestLineException;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

public class HttpRequestHandlerTest {
    private static Socket mockedSever;
    private static HttpRequestParser mockedParser;
    private static HttpRequestHandler testHttpRequestHandler;
    private static String inputRequestString;
    private static StringBuilder outputResponseString;
    private static ByteArrayOutputStream mockedSocketOutputStream;
    private static ByteArrayInputStream mockedSocketInputStream;

    @BeforeAll
    public static void setUp() {
        mockedSever = Mockito.mock(Socket.class);
        mockedParser = Mockito.mock(HttpRequestParser.class);
        testHttpRequestHandler = new HttpRequestHandler(mockedSever, mockedParser);
    }

    @BeforeEach
    public void clearOutputString() {
        outputResponseString = new StringBuilder();
    }

    @Nested
    public class TestGetHttpRequest {
        private StringBuilder getTestResource() throws IOException {
            StringBuilder testResource = new StringBuilder();
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("www/index.html");
            assert inputStream != null;
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while((line = bufferedReader.readLine()) != null) {
                testResource.append(line).append("\n");
            }
            return testResource;
        }


        @Test
        public void testGenerateCorrectHttpResponseOnCorrectGetRequest() {
            try {
                // setup
                inputRequestString = "GET / HTTP/1.1\\r\\n\\r\\n";
                outputResponseString
                        .append("HTTP/1.1 200 OK")
                        .append("\r\n\r\n")
                        .append(getTestResource());
                mockedSocketOutputStream = new ByteArrayOutputStream();
                mockedSocketInputStream = new ByteArrayInputStream(inputRequestString.getBytes());
                when(mockedSever.getOutputStream()).thenReturn(mockedSocketOutputStream);
                when(mockedSever.getInputStream()).thenReturn(mockedSocketInputStream);
                when(mockedParser.parseHttpRequestLine()).thenReturn(
                        new HttpRequestLine("HTTP/1.1", "GET", "/"));

                // exercise
                testHttpRequestHandler.generateHttpResponse();

                // assert
                assertEquals(outputResponseString.toString(), mockedSocketOutputStream.toString());
            } catch (IOException | InvalidHttpRequestLineException e) {
                fail(e.getMessage());
            }
        }

        @Test
        public void testGenerate404NotFoundResponseOnMissingResourceGetRequest() {
            try {
                // setup
                inputRequestString = "GET /missing_resource.html HTTP/1.1\\r\\n\\r\\n";
                outputResponseString
                        .append("HTTP/1.1 404 Not Found")
                        .append("\r\n\r\n");
                mockedSocketOutputStream = new ByteArrayOutputStream();
                mockedSocketInputStream = new ByteArrayInputStream(inputRequestString.getBytes());
                when(mockedSever.getOutputStream()).thenReturn(mockedSocketOutputStream);
                when(mockedSever.getInputStream()).thenReturn(mockedSocketInputStream);
                when(mockedParser.parseHttpRequestLine()).thenReturn(
                        new HttpRequestLine("HTTP/1.1", "GET", "/missing_resource.html"));


                // exercise
                testHttpRequestHandler.generateHttpResponse();

                // assert
                assertEquals(outputResponseString.toString(), mockedSocketOutputStream.toString());
            } catch (IOException | InvalidHttpRequestLineException e) {
                fail(e.getMessage());
            }
        }

        @Test
        public void testGenerate403ForbiddenResponseOnForbiddenResourceGetRequest() {
            try {
                // setup
                inputRequestString = "GET ../../../password.txt HTTP/1.1\\r\\n\\r\\n";
                outputResponseString
                        .append("HTTP/1.1 403 Forbidden")
                        .append("\r\n\r\n");
                mockedSocketOutputStream = new ByteArrayOutputStream();
                mockedSocketInputStream = new ByteArrayInputStream(inputRequestString.getBytes());
                when(mockedSever.getOutputStream()).thenReturn(mockedSocketOutputStream);
                when(mockedSever.getInputStream()).thenReturn(mockedSocketInputStream);
                when(mockedParser.parseHttpRequestLine()).thenReturn(
                        new HttpRequestLine("HTTP/1.1", "GET", "../../../password.txt"));


                // exercise
                testHttpRequestHandler.generateHttpResponse();

                // assert
                assertEquals(outputResponseString.toString(), mockedSocketOutputStream.toString());
            } catch (IOException | InvalidHttpRequestLineException e) {
                fail(e.getMessage());
            }
        }
    }

    @Test
    public void testReturnMethodNoAllowedOnIncorrectMethodRequest() {
        try {
            // setup
            inputRequestString = "INCORRECT_METHOD / HTTP/1.1\\r\\n\\r\\n";
            outputResponseString
                    .append("HTTP/1.1 402 Method Not Allowed")
                    .append("\r\n\r\n");
            mockedSocketOutputStream = new ByteArrayOutputStream();
            mockedSocketInputStream = new ByteArrayInputStream(inputRequestString.getBytes());
            when(mockedSever.getOutputStream()).thenReturn(mockedSocketOutputStream);
            when(mockedSever.getInputStream()).thenReturn(mockedSocketInputStream);
            when(mockedParser.parseHttpRequestLine()).thenReturn(
                    new HttpRequestLine("HTTP/1.1", "INCORRECT_METHOD", "/"));


            // exercise
            testHttpRequestHandler.generateHttpResponse();

            // assert
            assertEquals(outputResponseString.toString(), mockedSocketOutputStream.toString());
        } catch(IOException | InvalidHttpRequestLineException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testReturnInternalServerErrorOnMissingHttpMethodInRequestLine() {
        try {
            // setup
            inputRequestString = "/ HTTP/1.1\\r\\n\\r\\n";
            outputResponseString
                    .append("HTTP/1.1 500 Internal Server Error")
                    .append("\r\n\r\n");
            mockedSocketOutputStream = new ByteArrayOutputStream();
            mockedSocketInputStream = new ByteArrayInputStream(inputRequestString.getBytes());
            when(mockedSever.getOutputStream()).thenReturn(mockedSocketOutputStream);
            when(mockedSever.getInputStream()).thenReturn(mockedSocketInputStream);
            when(mockedParser.parseHttpRequestLine()).thenReturn(
                    new HttpRequestLine("HTTP/1.1", null, "/"));


            // exercise
            testHttpRequestHandler.generateHttpResponse();

            // assert
            assertEquals(outputResponseString.toString(), mockedSocketOutputStream.toString());
        } catch(IOException | InvalidHttpRequestLineException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testReturnInternalServerErrorOnWrongHttpVersionInRequestLine() {
        try {
            // setup
            inputRequestString = "/ HTTP/1.2\\r\\n\\r\\n";
            outputResponseString
                    .append("HTTP/1.1 500 Internal Server Error")
                    .append("\r\n\r\n");
            mockedSocketOutputStream = new ByteArrayOutputStream();
            mockedSocketInputStream = new ByteArrayInputStream(inputRequestString.getBytes());
            when(mockedSever.getOutputStream()).thenReturn(mockedSocketOutputStream);
            when(mockedSever.getInputStream()).thenReturn(mockedSocketInputStream);
            when(mockedParser.parseHttpRequestLine()).thenReturn(
                    new HttpRequestLine("HTTP/1.2", null, "/"));


            // exercise
            testHttpRequestHandler.generateHttpResponse();

            // assert
            assertEquals(outputResponseString.toString(), mockedSocketOutputStream.toString());
        } catch(IOException | InvalidHttpRequestLineException e) {
            fail(e.getMessage());
        }
    }

    @AfterAll
    public static void clean() throws IOException {
        mockedSever.close();
    }
}
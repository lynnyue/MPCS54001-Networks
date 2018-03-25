// Zhilin Yue
// HTTPServer
// 1. support GET and HEAD requests
// 2. Accept server port number as the
// only command-line argument
// 3. use TCP as the underlying reliable transport protocol

import java.io.*;
import java.net.*;
import java.lang.String;
import java.util.StringTokenizer;

public class HTTPServer {

    public static void main(String[] args) throws IOException {
        // get the working directory and print it
        String workingDir = System.getProperty("user.dir");
        System.out.println("Working dir: " + workingDir);
        // check if input has the correct format
        // input is in incorrect format
        if (args.length != 1 || !args[0].startsWith("--serverPort=")) {
            System.err.println("Usage: java EchoServer --port=XXXX");
            System.exit(1);
        }
        // get the port number from arg[0]
        // correct format of input should be "--serverPort=XXXX"
        StringTokenizer argTokens = new StringTokenizer(args[0], "=");
        argTokens.nextToken();
        int portNum = Integer.parseInt(argTokens.nextToken());
        System.out.println("\tCurrently Listening on port number [" + portNum + "] ");

        // opens sockets and establish connections
        System.out.println("\nStarting New Session\nWaiting for New Connections...");
        try {
            // create a new server socket at the port number
            ServerSocket serverSocket = new ServerSocket(portNum);
            while (true) {
                // open socket on the client end
                Socket clientSocket = serverSocket.accept();
                try {
                    DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                    // read request from the client
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    System.out.println("Connection accepted");

                    // call http_handler method to process the request
                    httpHandler(in, out);
                    // close socket

                    out.close();
                    in.close();
                    clientSocket.close();
                } catch (IOException e) {
                }
            }// end while
        } catch (IOException e) {
            // when an IOException is catched
            System.out.println("Exception caught when trying to listen on port" + portNum);
            System.out.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("Client disconnected.");
    }

    // method to handle http requests and output the results
    private static void httpHandler(BufferedReader input, DataOutputStream output) throws IOException {
        // handling redirecting
        RedirectMap redirectMap = new RedirectMap("www/redirect.defs");
        METHOD method;
        String strMethod = input.readLine();
        // edge case: no input
        if (strMethod == null) {
            System.out.println("No input");
            return;
        }
        // read the input and separate it
        // the correct format of input is method plus directory
        // separated by space
        String[] temp = strMethod.split(" ");
        // identify the http version used
        String two = temp[2];
        output.writeBytes(two);
        // check if the request is in right format
        if (temp.length != 3) {
            output.writeBytes(new HTTPHeader(400, MIME.NA, "").getHeader());
            return;
        }
        // handle the request by the type of the method
        if (strMethod.startsWith("GET")) {
            method = METHOD.GET;
        } else if (strMethod.startsWith("HEAD")) {
            method = METHOD.HEAD;
        } else {
            output.writeBytes(new HTTPHeader(405, MIME.NA, "").getHeader());
            return;
        }

        // handle redirect: if someone tries to fetch it return 404
        if (temp[1].equals("/redirect.defs")) {
            output.writeBytes(new HTTPHeader(404, MIME.NA, "").getHeader());
            return;
        }
        // if anyone tries to fetch one of the urls, return 301
        String original = temp[1];
        String localPath = redirectMap.get(original);
        if (localPath != null) {
            output.writeBytes(new HTTPHeader(301, MIME.NA, localPath).getHeader());
            return;
        }

        // handling content types
        MIME contentType;
        FileInputStream inputStream;
        // edge case: input is null
        try {
            inputStream = new FileInputStream("www" + original);
        } catch (IOException e) {
            inputStream = null;
        }
        if (inputStream == null) {
            output.writeBytes(new HTTPHeader(404, MIME.NA, "").getHeader());
            return;
        }
        if (original.endsWith(".txt")) {
            contentType = MIME.TXT;
        } else if (original.endsWith(".html")) {
            contentType = MIME.HTML;
        } else if (original.endsWith(".jpg") || original.endsWith(".jpeg")) {
            contentType = MIME.JPEG;
        } else if (original.endsWith(".png")) {
            contentType = MIME.PNG;
        } else if (original.endsWith(".pdf")) {
            contentType = MIME.PDF;
        } else {
            contentType = MIME.TXT;
        }

        output.writeBytes(new HTTPHeader(200, contentType, "").getHeader());
        byte[] buffer = new byte[2000];
        int length;
        while ((length = inputStream.read(buffer)) >= 1) {
            output.write(buffer, 0, length);
        }
        inputStream.close();
        output.flush();

        if (method == METHOD.HEAD) {
            if (inputStream != null) inputStream.close();
            output.writeBytes(new HTTPHeader(200, contentType, "").getHeader());
        }
    }
}
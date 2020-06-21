MPCS 54001 Project2

Compilation and Running Notes:
-- You need to compile the HTTPServer.java file using: javac HTTPServer.java
-- You need to pass the port number in HTTPServer through the command: java HTTPServer --serverPort=8080

Other Notes:
- Unless specified by the user, the HTTPServer runs on a loop and will always be on standy to listen for a connection. The only way to exit the program is to simply sigkill it
- As soon as you start the program, the server will look through the redirect list in order to keep track of what needs to be redirected, it will then:
	- start a new session
	- listen from the port
	- read the request
	- interpret it
	- spit out the response
	- close the session
	- start over with a new session

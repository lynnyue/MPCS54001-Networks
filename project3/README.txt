ZHILIN YUE
MPCS 54001
PROJECT 3

1. Files:
PingClient.java
PingServer.java
README.txt

2. Running Instructions
The code for the Ping client is in the file PingClient.java. To start the ping client, you need to compile the java code first by typing:
javac PingClient.java
Then run the code with the client commandline format:
java PingClient.java --server_ip = <server ip addr> --server_port=<server port> --count=<number of pings to send> --period=<wait interval> --timeout=<timeout>

3. Implementation:
My code contains three main parts: 
i: Scheduler class to set a timer to send each packet and track the data.
ii: EchoFunc class to handle sending and receiving
iii: StatPrinter class to print the overall statistics -- it is scheduled at the start of the session and it is scheduled to run after every packet is send and recieved plus the timeout time from the start of the last packet. 

So you need to wait for a while when the time out period is long. It will eventually come out!! 
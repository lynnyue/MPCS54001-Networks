// Zhilin Yue
// MPCS 54001 Networks
// Ping Client

import java.io.*;
import java.net.*;
import java.util.*;
import static java.lang.Math.toIntExact;
import java.net.DatagramSocket;
import java.util.Timer;
import java.util.TimerTask;

public class PingClient {

	// Static variable being used in multiple functions
	private static String serverIP;
	private static int pings[];
	private static int success[];
	private static long startTime;
	private static long endTime;
	
	public static void main(String[] argv) {
		// define command-line input
        // initialize command-line values
		serverIP = "";
		int portNum = -1;
		int pingCount = -1;
		int pingCooldown = -1;
		int pingTimeout = -1; 

		// process command-line arguments
        for (String arg : argv) {
            String[] input = arg.split("=");
            // server IP address: in the form of
            // --server_ip=<server ip addr>
            if (input.length == 2 && input[0].equals("--server_ip")) {
                serverIP = input[1];
                // server port number: in the form of
                // --server_port=<server port>
            } else if (input.length == 2 && input[0].equals("--server_port")) {
                portNum = Integer.parseInt(input[1]);
                // number of pings to send: in the form of
                // --count=<number of pings to send>
            } else if (input.length == 2 && input[0].equals("--count")) {
                pingCount = Integer.parseInt(input[1]);
                // wait intervals: in the form of
                // --period=<wait interval>
            } else if (input.length == 2 && input[0].equals("--period")) {
                pingCooldown = Integer.parseInt(input[1]);
                // time out: in the form of
                // --timeout=<timeout>
            } else if (input.length == 2 && input[0].equals("--timeout")) {
                pingTimeout = Integer.parseInt(input[1]);
            } else { // otherwise print error message
                System.err.println("Usage: java pingClient --server_ip=<server ip addr> --server_port=<server port> --count=<number of pings to send> --period=<wait interval> --timeout=<timeout>");
                return;
            }
        } // end user input

        // checks that the user put in all the arguments
        // if not, print error message
        if (serverIP.equals("") || portNum == -1 || pingCount == -1 || pingCooldown == -1 || pingTimeout == -1) {
            System.err.println("Error: One or more command line arguments missing");
            return;
        }
        // user need to put port number larger than 1024
        // if not, print error message
        if (portNum <= 1024) {
            System.err.println("Warning: Port numbers smaller than 1024 are potentially reserved. Port number should be larger than 1024");
            return;
        }

	    // Prints single line stating that we are pinging the specified IP Address
	    System.out.println("\nPING " + serverIP);

	    // Initalizes the pings array
	    pings = new int[pingCount];
	    success = new int[pingCount];

		// start a new timer for each ping session
		for (int i = 0; i < pingCount; i++) {
			new Scheduler(pingCooldown, i, portNum, pingTimeout, pingCount);
		}

		// The StatPrinter class is schedulued to run after all the pings have been sent plus the timeout period starting from the last ping
		// This ensures that the printOverallPingStats is not run before all pings are received
		new StatPrinter(pingCooldown, pingCount, pingTimeout);
	}

	// Scheduler class used to schedule ping sending and single ping statistic print out
	private static class Scheduler 
	{
    	Timer timer;
    	int number;
    	int port;
    	int timeout;
    	int totalNum;

    	// Scheduler Constructor
    	public Scheduler(int pingCooldown, int pingNum, int portNum, int pingTimeout, int totalNumber) {
    		number = pingNum;
    		port = portNum;
    		timeout = pingTimeout;
    		totalNum = totalNumber;

        	timer = new Timer();
        	timer.schedule(new EchoTask(), pingCooldown * number);
		}

		// EchoTask class handles every task related to a single ping
    	class EchoTask extends TimerTask {
        	public void run() {
        		// Gets the time difference, stores it in the array and prints out the ping stat
        		try {
        			int diff = echoFunc(port, timeout, number, totalNum);
        			pings[number] = diff;
        			success[number] = 1;
					System.out.println("PONG " + serverIP + ": seq=" + (number + 1) + " time=" + diff + " ms");
            		timer.cancel();
            	}
            	// Catches SocketTimeoutExceptions
				catch (SocketTimeoutException e) {
					pings[number] = timeout;
					success[number] = 0;
					if (number == (totalNum - 1)) {
						endTime = System.currentTimeMillis();
					}
					timer.cancel();
	    		}
	    		// Catches IOExceptions
	    		catch (IOException e) {
			      	System.err.println("Error processing ping request: " + e.getMessage());
			      	pings[number] = timeout;
			      	success[number] = 0;
			      	timer.cancel();
			    }
        	}
    	}
	}

	// main part: handle sending and receiving
    // send and receive datagram on the client side
    // return the total time taken for the process
	public static int echoFunc(int portNum, int pingTimeout, int seqNum, int totalNum) throws IOException, SocketTimeoutException
	{	
		String message = "PING " + (seqNum + 1) + ' ' + System.currentTimeMillis();
		// byte array store data that needs to be sent
		byte[] sendBuf = new byte[1024]; // byte array store data that needs to be sent
		sendBuf = message.getBytes();
		DatagramSocket socket = new DatagramSocket();

		InetAddress address = InetAddress.getByName(serverIP);
		DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, address, portNum);

		// set the timeout period to pingTimeout
        // socket method: enable/disable SO_TIMEOUT with the specified timeout
		socket.setSoTimeout(pingTimeout);

		// log the time when starting to send the packet
        // sent the packet
		Long begin = System.currentTimeMillis();
		if (seqNum == 0) {
			startTime = begin;
		}
		socket.send(packet);

		// receive the packet
        // log the time when receving the packet
		packet = new DatagramPacket(sendBuf, sendBuf.length);
		socket.receive(packet);
		Long end = System.currentTimeMillis();
		if (seqNum == (totalNum - 1)) {
			endTime = end;
		}

		// return the total time taken
		return (toIntExact(end - begin));
	}

	// Printer class that needs to be scheduled to print out the overall statistics
	private static class StatPrinter {
		Timer timer;
		int number;
		int timeout;
		int cooldown;

		// Constructor
		public StatPrinter(int pingCooldown, int packetNums, int pingTimeout) {
			number = packetNums;
			timeout = pingTimeout;
			cooldown = pingCooldown;

			timer = new Timer();
        	timer.schedule(new PrintTask(), (number * cooldown + timeout));
		}

		// PrintTask class prints out the overall ping statistics after a certain period of time
		class PrintTask extends TimerTask {
        	public void run() {
        		printStats(number, pings, success, timeout, cooldown);
        		timer.cancel();
        	}
    	}
	}


	// print out the aggregate statistics after all replies have either been received or timed out
	private static void printStats(int transmitted, int[] timeList, int[] successList, int pingTimeout, int pingCooldown) {
		
		// Sets up a couple of variables first
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		int avg = 0;
		int count = timeList.length;
		long totalTimeElapsed = 0;
		int pingsReceived = 0;
		int pingsLost = 0;
		int lossPercentage = 0;

		totalTimeElapsed = endTime - startTime;

		// Calculate the min, max, and avg
		for (int i = 0; i < count; i++) {

			// total time elapsed by calculating the longest time taken for a ping to be sent and received
			//if (totalTimeElapsed < (timeList[i] + i * pingCooldown)) {
				//totalTimeElapsed = (timeList[i] + i * pingCooldown);
			//}

			//if (timeList[i] == pingTimeout) {
				//pingsLost += 1;
				//continue;
			//}
			// calculate the total number of lost packet
			if (successList[i] == 0) {
				pingsLost += 1;
				continue;
			}

			// get maximum and minimum
			if (timeList[i] > max) {
				max = timeList[i];
			}

			if (timeList[i] < min) {
				min = timeList[i];
			}

			// Increments the number of pings received 
			// adds it to the average and total time elapsed
			pingsReceived += 1;
			avg += timeList[i];
		}

		// Calculate the average and the loss percentage
		if (pingsReceived > 0) {
			avg = avg /pingsReceived;
		}
		else
		{
			min = 0;
			max = 0;
			avg = 0;
		}

		lossPercentage = pingsLost * 100 / transmitted;

		// print everything
		System.out.println("\n--- " + serverIP + " ping statistics ---");
		System.out.println(transmitted + " transmitted, " + pingsReceived + " received, " + lossPercentage + "% loss, time " + totalTimeElapsed + "ms");
		System.out.println("rtt min/avg/max = " + min + "/" + avg + "/" + max + " ms\n");
	}
}
#!/usr/bin/env python3
from socket import *
import sys

serverName = 'localhost'
serverPort = int(sys.argv[1])
while True:
	clientSocket = socket(AF_INET, SOCK_STREAM)
	clientSocket.connect((serverName, serverPort))
	sentence = input('Input text:')
	clientSocket.send(sentence.encode())
	modifiedSentence = clientSocket.recv(1024)
	print('From server: ',modifiedSentence.decode())
	clientSocket.close()
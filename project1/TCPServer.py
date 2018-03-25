#!/usr/bin/env python3
from socket import *
import sys

serverPort = int(sys.argv[1])
serverSocket = socket(AF_INET, SOCK_STREAM)
serverSocket.bind(('', serverPort))
serverSocket.listen(1)
print('The server is ready to receive')

while True:
    connectionSocket, addr = serverSocket.accept()
    sentence = connectionSocket.recv(1024).decode()
    print('From client: ', sentence)
    connectionSocket.send(sentence.encode())
    connectionSocket.close()
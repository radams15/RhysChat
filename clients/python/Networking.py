import socket
import json

ip = "127.0.0.1"
port = 3000

BUFSIZE = 2048

username = "RhysPython"


class JsonInterpreter:
    @staticmethod
    def to_json(text):
        data = json.dumps({"text": text, "fromName": username, "commands":[]})
        return data

    @staticmethod
    def from_json(jsonData):
        message = json.loads(jsonData)
        return message

class Networking:
    def __init__(self):
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.socket.connect((ip, port))

    def recv(self):
        return self.socket.recv(BUFSIZE).decode()

    def sendData(self, data):
        self.socket.send((data+"\n").encode())

    def end(self):
        self.socket.close()
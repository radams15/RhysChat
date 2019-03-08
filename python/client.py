from socket import AF_INET, socket, SOCK_STREAM, IPPROTO_TCP, TCP_NODELAY
from threading import Thread
import time
import datetime as date
import json

def format_date(d):
	if type(d) == date.datetime:
		d = d.strftime("%b %d, %Y %I:%M:%S %p")
	return d

class Message:
	def __init__(self, text, frm, date, commands=[]):
		self.text = text
		self.frm = frm
		self.date = format_date(date)
		self.commands = commands
		
	def __repr__(self):
		return "({} at {}): {}\t\tcommands: {}".format(self.frm, self.date, self.text, self.commands)

class JsonInterpreter:
	def __init__(self):
		pass
		
	def to_json(self, mc):
		json_data = json.dumps({"text":mc.text, "from":mc.frm, "date":mc.date, "commands":mc.commands})
		return json_data
		
	def to_message(self, json_data):
		jd = json.loads(json_data)
		message = Message(jd["text"], jd["from"], jd["date"], jd["commands"])
		return message
		
ji = JsonInterpreter()
		
HOST = 'localhost'
PORT = 3000
BUFSIZ = 1024
ADDR = (HOST, PORT)

username = "Guest"

SERVER = socket(AF_INET, SOCK_STREAM)
SERVER.setsockopt(IPPROTO_TCP, TCP_NODELAY, 1)
SERVER.connect(ADDR)

class ClientThread(Thread):
	def __init__(self):
		Thread.__init__(self)


	def run(self):
		while True:
			data = SERVER.recv(BUFSIZ)
			data = data.decode('utf-8')
			if not data:
				break
			message = ji.to_message(data)
			print ("({} at {}): {}".format(message.frm, message.date, message.text))

newthread = ClientThread()
newthread.start()

while True:
	time.sleep(0.1)
	input_message = input()
	message = Message(input_message, username, date.datetime.now(), commands=[])
	json_message = ji.to_json(message)+"\n"
	data = json_message.encode('utf-8')
	SERVER.sendall(data)

SERVER.close()

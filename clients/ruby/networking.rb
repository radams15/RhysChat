class Networking
  class JsonInterpreter
    def toJson(text)
      data = {"text": text, "fromName": $myName, "date": DateTime.now, "commands":[]}
      return JSON.generate data
    end

    def fromJson(jsonData)
      return JSON.parse jsonData
    end
  end

  def initialize
    @socket = TCPSocket.open $ip, $port
    @json_interpreter = JsonInterpreter.new
  end

  def getSocket
    return @socket
  end

  def getJson
    return @json_interpreter
  end

  def sendData(data)
    @socket.puts data+"\n"
    @socket.flush
  end
end
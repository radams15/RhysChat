require "socket"
require "json"
require "date"
require "gtk3"

require_relative "networking"

$append_to_top = true

$ip = "127.0.0.1"
$port = 3000

$myName = "Rhys"

$networking = Networking.new

$main_window_file = "ui/mainWindow.glade"

class Gui < Gtk::Application
  def initialize
    super("org.gtk.guiapp", :handles_open)

    builder = Gtk::Builder.new
    builder.add_from_file $main_window_file

    builder.connect_signals do |handler|
      begin
        method(handler)
      rescue
        puts "#{handler} not yet implemented!"
      end
    end

    $window = builder.get_object "window"
    $incoming = builder.get_object "incoming"
    $outgoing = builder.get_object "outgoing"

    $send_button = builder.get_object "send_button"

    $in_buf = $incoming.buffer

    $window.show_all
    $window.resize 800, 600

  end

  def send_button_clicked(x)
    text = $outgoing.text
    $outgoing.set_text ""
    json = $networking.getJson.toJson text
    $networking.sendData json
  end

  def window_closed(x)
    exit(0)
  end

end

def receive_loop
  socket = $networking.getSocket
  json_interpreter = $networking.getJson
  while true
    if line = socket.gets
      message = json_interpreter.fromJson line
      if $append_to_top
        @iter = $in_buf.start_iter
      else
        @iter = $in_buf.end_iter
      end
      if message["commands"].include? "clear"
        $in_buf.set_text ""
      end
      $in_buf.insert(@iter, "\n[#{message["date"]}] #{message["fromName"]}: #{message["text"]}")
    end
  end
end

def main

  Thread.new{
    receive_loop
  }

  app = Gui.new
  app.run
  Gtk.main
end

main
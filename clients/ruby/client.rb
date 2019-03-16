require "socket"
require "json"
require "date"
require "gtk3"

require_relative "networking"

$append_to_top = true

$ip = "127.0.0.1"
$port = 3000

$myName = "RhysRuby"

$ui_folder = File.join("../", "ui")

$main_window_file = File.join($ui_folder, "mainWindow.glade")

$entry_box_file = File.join($ui_folder, "serverEntryBox.glade")

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

  def append_text(text)
    if $append_to_top
      @iter = $in_buf.start_iter
    else
      @iter = $in_buf.end_iter
    end
    $in_buf.insert(@iter, ("\n"+text))
  end

end

class IpEntryBox < Gtk::Application
  def initialize
    super("org.gtk.ipentryboxapp", :handles_open)

    builder = Gtk::Builder.new
    builder.add_from_file $entry_box_file

    builder.connect_signals do |handler|
      begin
        method(handler)
      rescue
        puts "#{handler} not yet implemented!"
      end
    end

    @window = builder.get_object "window"
    @entry_box = builder.get_object "entry_box"

    @window.show_all
    @window.resize 300, 100

  end

  def ok_button_pressed(x)
    raw_data = @entry_box.text
    data = raw_data.split(":")

    if data.length == 1
      $ip = data[0]
    end
    if data.length == 2
      $port = data[1].to_i
    end
    @window.destroy
    Gtk.main_quit
  end

end

def receive_loop
  socket = $networking.getSocket
  json_interpreter = $networking.getJson
  while true
    if line = socket.gets
      message = json_interpreter.fromJson line
      output_text = "[#{message["date"]}] #{message["fromName"]}: #{message["text"]}"

      if message["commands"].include? "clear"
        $in_buf.set_text ""
        next
      elsif message["commands"].include? "joining"
         $app.append_text "#{message["fromName"]} Joined The Chat"
         next
      elsif message["commands"].include? "leaving"
        $app.append_text "#{message["fromName"]} Left The Chat"
        next
      end

      $app.append_text output_text
    end
  end
end

def main

  entry_box = IpEntryBox.new
  entry_box.run
  Gtk.main

  begin
    $networking = Networking.new
  rescue SocketError
    puts("Invalid Ip: #{$ip}:#{$port}")
    exit(1)
  end

  puts($ip, $port)

  Thread.new{
    receive_loop
  }

  $app = Gui.new
  $app.run
  Gtk.main
end

main
import threading
import os
import signal

import gi
gi.require_version('Gtk', '3.0')
from gi.repository import Gtk

import Networking

ui_dir = os.path.join(os.getcwd(), "../", "ui")

main_window_file = os.path.join(ui_dir, "mainWindow.glade")
ipbox_window_file = os.path.join(ui_dir, "serverEntryBox.glade")

class ReceiveLoop:
    def __init__(self, gui_app):

        self.gui_app = gui_app
        self.is_running = True

        thread = threading.Thread(target=self.run, args=())

        thread.start()

    def run(self):
        while self.is_running:
            data = networking.recv()
            if data:
                message = Networking.JsonInterpreter.from_json(data)

                output_text = f'[{message["date"]}] {message["fromName"]}: {message["text"]}'

                if "clear" in message["commands"]:
                    self.gui_app.incoming_buffer.set_text("")
                elif "joining" in message["commands"]:
                    self.gui_app.append_text(f'{message["fromName"]} Joined The Chat')
                elif "leaving" in message["commands"]:
                    self.gui_app.append_text(f'{message["fromName"]} Left The Chat')
                elif message["text"] is None:
                    continue
                else:
                    self.gui_app.append_text(output_text)
        return

    def end(self):
        self.is_running = False
        exit(0)

class Gui(Gtk.Window):
    def __init__(self):
        builder = Gtk.Builder()
        builder.add_from_file(main_window_file)
        self.window = builder.get_object("window")
        self.incoming = builder.get_object("incoming")
        self.outgoing = builder.get_object("outgoing")

        self.incoming_buffer = self.incoming.get_buffer()

        self.window.resize(800, 600)

        self.window.show_all()

        builder.connect_signals(self)


    def append_text(self, text):
        view_iter = self.incoming_buffer.get_start_iter()
        self.incoming_buffer.insert(view_iter, ("\n"+text))

    def send_button_clicked(self, x):
        text = self.outgoing.get_text()
        self.outgoing.set_text("")
        json_data = Networking.JsonInterpreter.to_json(text)
        networking.sendData(json_data)

    def window_closed(self, x):
        networking.end()
        os.kill(os.getpid(), signal.SIGTERM)

class IpEntryBox(Gtk.Window):
    def __init__(self):
        builder = Gtk.Builder()
        builder.add_from_file(ipbox_window_file)
        self.window = builder.get_object("window")
        self.entry_box = builder.get_object("entry_box")

        self.window.resize(300, 100)

        self.window.show_all()

        builder.connect_signals(self)

    def ok_button_pressed(self, x):
        raw_data = self.entry_box.get_text()

        data = raw_data.split(":")

        if len(data) == 1:
            Networking.ip = data[0]

        if len(data) == 2:
            try:
                Networking.port = int(data[1])
            except ValueError:
                pass

        self.window.destroy()

        Gtk.main_quit()

if __name__ == '__main__':
    entry_box = IpEntryBox()
    Gtk.main()

    networking = Networking.Networking()

    app = Gui()
    receiver = ReceiveLoop(app)
    Gtk.main()
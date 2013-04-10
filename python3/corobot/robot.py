"""Python 3 user library for the Corobots project.

Z. Butler, Jan 2013
M. Bogue, April 2013

"""
from collections import deque
import os
from queue import Queue
import socket
from threading import Event, Lock, Thread

from corobot.map import Map

class CorobotException(Exception):
    pass

class Robot():

    class Future():

        def __init__(self):
            self._data = None
            self._error = None
            self._event = Event()
            self._callbacks = []
            self._error_callbacks = []

        def wait(self):
            self._event.wait()
            if self._error:
                raise CorobotException(self._error)

        def then(self, callback=None, error=None):
            if callback is not None:
                if not callable(callback):
                    raise CorobotException("Callback must be callable.")
                self._callbacks.append(callback)
            if error is not None:
                if not callable(error):
                    raise CorobotException("Error callback must be callable.")
                self._error_callbacks.append(error)
            return self

        def get(self):
            self.wait()
            return self._data

        def fulfilled(self):
            return self._event.is_set()

    def __init__(self, addr, port):
        """Creates connection to robot."""
        try:
            self.socket = socket.create_connection((addr, port))
        except OSError:
            raise CorobotException("Couldn't connect to robot at %s:%d" % (addr, port))
        self.next_msg_id = 1
        self.msg_queue = deque()
        self.msg_queue_event = Event()
        self.futures = {}
        self.socket_out = self.socket.makefile("w")
        self.out_lock = Lock()
        self.reader_thread = Thread(target=self._socket_reader)
        self.message_thread = Thread(target=self._message_handler)
        self.running = True
        self.reader_thread.start()
        self.message_thread.start()

    def _socket_reader(self):
        with self.socket.makefile("r") as socket_in:
            for line in socket_in:
                self.msg_queue.append(line)
                self.msg_queue_event.set()

    def _message_handler(self):
        while self.running:
            while self.msg_queue:
                msg = self.msg_queue.popleft()
                tokens = msg.split(" ")
                msg_id = int(tokens[0])
                key = tokens[1]
                data = tokens[2:]
                future = self.futures.pop(msg_id)
                if key == "POS":
                    future._data = tuple(map(float, data))
                if key != "ERROR":
                    for callback in future._callbacks:
                        callback()
                else:
                    future._error = " ".join(data)
                    for error_callback in future._error_callbacks:
                        error_callback(future._error)
                future._event.set()
            self.msg_queue_event.wait()
            self.msg_queue_event.clear()

    def _write_message(self, msg):
        with self.out_lock:
            msg_id = self.next_msg_id
            self.next_msg_id += 1
            self.socket_out.write("%d %s\n" % (msg_id, msg))
            self.socket_out.flush()
            future = Robot.Future()
            self.futures[msg_id] = future
            return future

    def nav_to(self, location):
        """Drives the robot to the given location with path planning."""
        return self._write_message("NAVTOLOC " + location.upper())

    def nav_to_xy(self, x, y):
        """Drives the robot to the given location with path planning."""
        return self._write_message("NAVTOXY %d %d" % (x, y))

    def go_to(self, location):
        """Drives the robot in a straight line to the given location."""
        return self._write_message("GOTOLOC " + location.upper())

    def go_to_xy(self, x, y):
        """Drives the robot in a straight line to the given coordinates."""
        return self._write_message("GOTOXY %d %d" % (x, y))

    def get_pos(self):
        """Returns the robot's position as an (x, y, theta) tuple."""
        return self._write_message("GETPOS")

    def get_closest_loc(self):
        """Returns the closest node to the current robot location."""
        raise NotImplementedError()

    def close(self):
        self.running = False
        with self.out_lock:
            try:
                self.socket_out.close()
            except socket.error:
                pass
        try:
            self.socket.close()
        except socket.error:
            pass

    def __enter__(self):
        return self

    def __exit__(self, type, value, traceback):
        self.close()

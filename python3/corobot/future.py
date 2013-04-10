from threading import Event

from corobot.common import CorobotException

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

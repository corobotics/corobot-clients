"""Simple example showing usage of the Corobot user API."""

from corobot import Robot

def callback():
    print("arrived!")

def main():
    """Test out simple API stuff."""
    with Robot("127.0.1.1", 15001) as r:
        p = r.go_to_xy(5, 5).then(callback)
        p.wait()
        print("fin")

main()

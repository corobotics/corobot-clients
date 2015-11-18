"""Simple example showing usage of the Corobot Face Recognition API."""

from sys import argv
from corobot import Robot

def main():
    if len(argv) < 4:
        print("Usage: python3 %s <landmark> <subject> <message>" % argv[0])
        return
    
    destination = argv[1] 
    recipient = argv[2]
    message = argv[3]
    
    message_delivered = False

    with Robot() as r:
        # Starting position
        pos = r.get_pos().get()
        p = r.nav_to(destination)
        p.wait()
        
        while not message_delivered:
            subject, confidence, pos_x, pos_y = r.recognize_face().get()
            if subject == recipient:
                # Deliver message 
                r.request_confirm(message,30).wait()
                message_delivered = True
        
        # Return to starting position
        r.go_to_xy(pos[0],pos[1]).wait()

if __name__ == "__main__":
    main()

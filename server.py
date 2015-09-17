#!/usr/bin/env python
# server.py
 
import RPi.GPIO as GPIO
import socket
import select
import config as cfg
import Queue
from threading import Thread
from time import sleep
from random import randint
import sys
import time
 
directions = ['1', '2', '3', '4', '5', 'q']


class Robot(Thread):
    def __init__(self):
        Thread.__init__(self)
        self.cur_dir = '0'
        self.next_dir = '0'
        self.current_direction = (0,0)
        self.daemon = True
        self.start()

    def run(self):
        # Motor PINs
        MOTOR1A = 18
        MOTOR1B = 17
        MOTOR2A = 22
        MOTOR2B = 23

        # freq of pwm outputs
        PWM_FREQ = 300 #hz

        # uses processor pin numbering
        GPIO.setmode(GPIO.BCM)

        # speed = pwm duty cycle, 0 = off, 100 = max
        speed = 70

        # list to convert key presses into motor on/off values to correspond with the direction
        direction = {
            '1' : (1,1), '2' : (2,2), '3' : (2,1), '4': (1,2), '5' : (0,0) #up, down, left, right, stop
        }

        # setup pins
        GPIO.setup(MOTOR1A, GPIO.OUT)
        GPIO.setup(MOTOR1B, GPIO.OUT)
        GPIO.setup(MOTOR2A, GPIO.OUT)
        GPIO.setup(MOTOR2B, GPIO.OUT)

        # set pins as PWM
        pin1A = GPIO.PWM(MOTOR1A, PWM_FREQ)
        pin1B = GPIO.PWM(MOTOR1B, PWM_FREQ)
        pin2A = GPIO.PWM(MOTOR2A, PWM_FREQ)
        pin2B = GPIO.PWM(MOTOR2B, PWM_FREQ)

        # start PWM
        pin1A.start (0)
        pin1B.start (0)
        pin2A.start (0)
        pin2B.start (0)

        started = 1
        while True:
            if self.cur_dir == self.next_dir:
                continue
            else:
               if self.next_dir == 'q':
                   self.cur_dir = '5'
                   self.next_dir = '5'
                   break
               self.cur_dir = self.next_dir
               self.current_direction = direction[self.cur_dir]
            ## Motor 1
            # fwd
            if self.current_direction[0] == 1 :
                pin1B.ChangeDutyCycle(0)
                pin1A.ChangeDutyCycle(speed)
            # rev
            elif self.current_direction[0] == 2 :
                pin1A.ChangeDutyCycle(0)
                pin1B.ChangeDutyCycle(speed)
            # stop
            else :
                pin1A.ChangeDutyCycle(0)
                pin1B.ChangeDutyCycle(0)
            ## Motor 2
            # fwd
            if self.current_direction[1] == 1 :
                pin2B.ChangeDutyCycle(0)
                pin2A.ChangeDutyCycle(speed)
            # rev
            elif self.current_direction[1] == 2 :
                pin2A.ChangeDutyCycle(0)
                pin2B.ChangeDutyCycle(speed)
            # stop
            else :
                pin2A.ChangeDutyCycle(0)
                pin2B.ChangeDutyCycle(0)
        started = 0
        # Stop and cleanup the PWM
        pin1A.stop()
        pin1B.stop()
        pin2A.stop()
        pin2B.stop()
        GPIO.cleanup()

r = Robot()
time.sleep(0.5)
 
# process input from android app (one of directions list)
def process(value):
    if value in directions:
        r.next_dir = value
    else:
        print("value (" + repr(value) + ") not a valid direction")

def main():
    global r
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)         # Create socket object
    host = ''
    port = cfg.PORT                # Reserve port
    s.bind((host, port))        # Bind to the port
    print "Listening on port {p}...".format(p=port)
    while True:
        try:
            s.listen(1)
            client, addr = s.accept()
            r.next_dir = 'q'   # reset robot
            time.sleep(0.3)
            r = Robot()
            while True:
                data = client.recv(2) # only get the one character, ignore the newline at the end
                if data == '': # if socket closed
                    break
                process(data[0])
                if data == 'q': # if connection stopped cleanly, still want to process data to stop and cleanup robot
                    break
        except Exception as e:
            print "error: " + str(e)
            cleanup()
            exit()
    cleanup()
 
def cleanup():
    r.join()
    exit()
 

main()

#########################################################
 
#if __name__ == "__main__":
#    main()

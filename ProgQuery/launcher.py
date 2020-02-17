import sys
import os
import time

instrumentation_time= False


def capture(command):
  #command = command + " > .\\temp\\output.txt"
  print("> Command: " + command)
  iniTime=time.time()
  os.system(command)
  endTime=time.time()
  file = open(".\\temp\\output.txt")
  execTime = int(file.readline())
  file.close()  
  #print(time)
  return endTime-iniTime if instrumentation_time else execTime
'''
# original version
if __name__ == "__main__":
  command = ""
  for i in range(1,len(sys.argv)):
      command = command + " " + sys.argv[i]
  #print("> Command: " + command)
  command = command + " > .\\temp\\output.txt"
  os.system(command)
  file = open(".\\temp\\output.txt")
  time = int(file.readline())
  file.close()  
  #print(time)
  sys.exit(time)
'''

    

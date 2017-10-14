import os

import startup
import steady
import launcher

import datetime

isInstrumented=False

def run_startup_ins(list):
##    run_startup(list)
    launcher.instrumentation_time=True
    run_startup(list)

def run_startup(list):
    out_files = [".\\startup.test.csv"]
    #print file header
    for file_name in out_files:
        file = open(file_name, "a")
        file.write("Timestamp: {}\tComputerName: {}\n".format(datetime.datetime.now(), os.environ['COMPUTERNAME']))
        file.close()
    for command, it in list:
        startup.run(command, command, out_files[0])


def run_steady(list):
    out_files = [".\\steady.test.csv"]
    #print file header
    for file_name in out_files:
        file = open(file_name, "a")
        file.write("Start timestamp: {}\tComputerName: {}\n".format(datetime.datetime.now(), os.environ['COMPUTERNAME']))
        file.close()
    for command, it in list:
        steady.run(command, command, out_files[0])

def java_compile(batName):
    print("Compiling java "+batName)
    os.system(batName)

if __name__ == "__main__":
   # cs_compile("myTest")
   # cs_compile("points")
    java_compile("compile8")
    benchmarks_info = [
     #   ["executeWithWeaver.bat" , 500000],

["..\..\Wiggle_Adapted\executeWithWeaver.bat" , 500000],
        ["executeWithWeaver.bat" , 500000]
                        ]

    run_startup_ins(benchmarks_info)
    #run_steady(benchmarks_info)

   # run_startup(benchmarks_info)
   #run_steady(benchmarks_info)

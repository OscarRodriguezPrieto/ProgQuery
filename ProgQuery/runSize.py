import os

import startup
import steady
import launcher

import datetime
import bench

isInstrumented=False
dbRefresh=True
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
        print(dbRefresh)
        if dbRefresh:
            os.system("refreshDb.bat");
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
    #java_compile("compile8")
    '''
    launcher.instrumentation_time=True
    benchmarks_info = [
     #   ["executeWithWeaver.bat" , 500000],
#2 clases 
#27 classes 137 methods
["executeWithWeaver.bat" , 500000],

["executeWithEulerBig.bat" , 500000],
["pdgTest.bat" , 500000],
                        ]

    run_startup_ins(benchmarks_info)
    '''
    dbRefresh=False#NOS ESTABA DESTROZANDO LAS EXTRACCIONESPara compilacion solo en Java no es necesario
    #Limpiar en cada ejecucion del benchmark solo necesario para insercion
    bench.dbRefresh=False
    launcher.instrumentation_time=False
    '''
    for i in range(9):
        launcher.capture("getJavaFiles"+str(i)+".bat")
        benchmarks_info = [["executeEvaluationProject"+ str(i)+".bat" , 500000]]
        run_startup(benchmarks_info)'''
    for i in range(9):
        os.system("refreshDB.bat")
        os.system("getJavaFiles"+str(i)+".bat")
        os.system("executeEvaluationProject"+str(i)+"PQ.bat")
        for j in range(1):
            benchmarks_info = [["executeRule"+str(13)+".bat" , 500000]]
            run_startup(benchmarks_info)
    '''
        for i in range(9):
        launcher.capture("getJavaFiles"+str(i)+".bat")
        benchmarks_info = [["executeEvaluationProject"+ str(i)+"Wiggle.bat" , 500000]]
        run_startup(benchmarks_info)'''
    '''
    benchmarks_info = [

    ["executeQuery.bat" , 500000],
    ["executeQuery1.bat" , 500000],
    ["executeQuery2.bat" , 500000],
    ["executeQuery3.bat" , 500000],
    ["executeQuery4.bat" , 500000],
    ["executeQuery5.bat" , 500000]
                        ]

    run_startup(benchmarks_info)'''
    #run_steady(benchmarks_info)

   # run_startup(benchmarks_info)
   #run_steady(benchmarks_info)

# Descargar e instalar el paquete NumPy de http://sourceforge.net/projects/numpy/
# Descargar e instalar el paquete SciPy de http://sourceforge.net/projects/scipy/

import scipy
import scipy.stats.distributions as distributions
import math
import os
import time
import launcher

dbRefresh=''

def confidence(samples, confidence_level):
  """This function determines the confidence interval for a given set of samples, 
  as well as the mean, the standard deviation, and the size of the confidence 
  interval as a percentage of the mean.
  """
  mean = scipy.mean(samples)
  sdev = scipy.std(samples)
  n    = len(samples)
  df   = n - 1
  t    = distributions.t.ppf((1+confidence_level)/2.0, df)
  interval = (interval_low, interval_high) = ( mean - t * sdev / math.sqrt(n) , mean + t * sdev / math.sqrt(n) )
  interval_size = interval_high - interval_low
  interval_percentage = interval_size / mean * 100.0
  return (interval, mean, sdev, interval_percentage) 

def startup(command, confidence_level, p_iterations, break_if_interval_percentage_is):
    print("", command, " in Startup Modeddd")

    execution_times = []
    for i in range(1, p_iterations+1):
      print(dbRefresh)
      if dbRefresh !='':
          os.system("refreshDB.bat")
      execution_time = launcher.capture(command)
      #print("Iteration ", i, ". Times in millis ", execution_time, ".")
      execution_times.append(execution_time)
      interval,mean,sdev,interval_percentage = confidence(execution_times, confidence_level)
      print(i, execution_time, mean, interval_percentage, break_if_interval_percentage_is)
      if interval_percentage <= 1:
        print("Break")
        break
    return interval, mean, sdev, interval_percentage

def steady(command, confidence_level, p_iterations, break_if_interval_percentage_is, max_bench_invocations, k, CoV):
    command += " " + str(max_bench_invocations) + " " + str(k) + " " + str(CoV)
    print("", command, " in Steady-state Mode")
    
    execution_times = []
    for i in range(1, p_iterations+1):
      execution_time = launcher.capture(command)
      #print("Iteration ", i, ". Times in millis ", execution_time, ".")
      execution_times.append(execution_time)
      interval,mean,sdev,interval_percentage = confidence(execution_times, confidence_level)
      print(i, execution_time, mean, interval_percentage, break_if_interval_percentage_is)
      if interval_percentage <= break_if_interval_percentage_is:
        break
    return interval, mean, sdev, interval_percentage

if __name__ == "__main__":
    # Parameters
    print("------STARTUP------")
    interval, mean, sdev, interval_percentage = startup("java Benchmark", 0.95, 30, 2)
    print("Results Startup:")
    print("Interval:", interval)
    print("Mean:", mean)
    print("Standard deviation:", sdev)
    print("Interval percentage:", interval_percentage)

    print("------STEADY-STATE------")
    interval, mean, sdev, interval_percentage = steady("java Benchmark", 0.95, 30, 2, 30, 10, 0.02)
    print("Results Steady-state:")
    print("Interval:", interval)
    print("Mean:", mean)
    print("Standard deviation:", sdev)
    print("Interval percentage:", interval_percentage)

    

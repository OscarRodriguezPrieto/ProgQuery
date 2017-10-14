import bench
from parameters import *

import os
import time
import sys

def spanish_number(number):
		return str(number).replace(".", ",")
		
def run(command, bench_name, out_file):
	#print("Steady test; Interval low; Interval high; Mean; Std Dev; Percentage;")
	interval, mean, sdev, interval_percentage = bench.steady(command, confidence_level, p_iterations, break_if_interval_percentage_is, max_bench_iterations, k, CoV)
	results_str = str.format("{} ; ", bench_name)
	results_str += str.format("{} ; ", spanish_number(interval[0]))
	results_str += str.format("{} ; ", spanish_number(interval[1]))
	results_str += str.format("{} ; ", spanish_number(mean))
	results_str += str.format("{} ; ", spanish_number(sdev))
	results_str += str.format("{} ; {} ;\n", spanish_number(interval_percentage), "Steady")
	result_file = open(out_file, "a")
	result_file.write(results_str)
	result_file.close()

if __name__ == "__main__":
	runsteady(sys.argv[1])
	

	

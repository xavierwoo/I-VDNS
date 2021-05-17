# I-VDNS code for the min-max arc crossing problem

This project is the experiment codes for the paper _A Variable Depth Neighborhood Search Algorithm for the Min-Max Arc Crossing Problem_.

## About the source code

The scr folder contains all the source code for the algorithm.
No other third party library is used.
Although I coded this algorithm using JDK11, I have restricted myself to use only JDK8 gramma.
Thus, the code should be compiled under JDK8 or later version with any java IDE.

The main logic of the algorithm lies in MMACSolver.java
The first two member variables are the tunable parameters discussed in the paper.
The third varibale is the time limit in seconds.

If you pour all the *.java files to the IDE and compile, you can produce the same MMAC.jar file in the repo.

## How to run the code

If you have Installed JDK11 already, you can run the MMAC.jar without recompile the code.
To run the code, using the following command:

```
java -jar MMAC.jar <time_limit_in_seconds> <run_times> <P denominator> <perturbation strength>
```

For example, 

```
java -jar MMAC.jar uniform/noug3-rnd-001.txt 60 10 0.5 0.1
```

will test the instance uniform/noug3-rnd-001.txt for 10 times, the time limit for each run is 60 seconds.
For these ten runs, the maximum length of the move chain is $0.5\kappa$, while the perturbation strength is 0.1.
The recommended parameter setting in the paper is

```
java -jar MMAC.jar uniform/noug3-rnd-001.txt 60 10 1 0.1
```

The objective obtained by each run will be stored in resTotal.csv file.
The best objective overall is also reported.

## The instance format

```
<number of vertices> <number of arcs> <number of layers>
<number of vertices in each layer>
<arc>
...
```

The three numbers in the first line represent the number of vertices, the number of arcs and the number of layers.

The second line gives the number of vertices in each layer.

Each of the following lines represents one arc.

Take the instance north.30.29.7 as example. The following is the first ten lines of this instance.

```
30 29 7
2 7 10 8 1 1 1
29 30
28 29
20 28
21 28
22 28
23 28
10 23
3 10
...
```

The first line indicates that there are 30 vertices, 29 edges and 7 layers.
For each layer, there are 2, 7, 10, 8, 1, 1 and 1 vertices respectively.
The following lines represent the edges.

## The output description

The following is the first few lines of the standard output of the algorithm.

```
uniform/noug3-rnd-001.txt	 run 0
Initializing...
Initial obj: 287
Iteration: 2000, Obj: 267, Best: 257
Iteration: 3000, Obj: 270, Best: 257
```

The first line gives the file name of the instance and the times it is currently running.
Then the algorithm reports the initial objective on the third line.
The following lines report the running state, i.e., the current iteration count, the current objective, and the ever best objective.
To avoid too many output strings, the running state is reported per 1000 iterations.

The algorithm also reports the result for each run and best overall in the resTotal.csv file.

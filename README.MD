# I-VDNS code for the min-max arc crossing problem

## How to run the code

MMAC.jar is the executable file.
To run the code, using the following command:

```
java -jar MMAC.jar <time_limit_in_seconds> <run_time>
```

For example, 

```
java -jar MMAC.jar uniform/noug3-rnd-001.txt 60 10
```

will test the instance uniform/noug3-rnd-001.txt for 10 times, the time limit for each run is 60 seconds.


The result will be stored in resTotal.csv file.

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

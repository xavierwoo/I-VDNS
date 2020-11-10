import mmac.MMACSolver;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public class Main {

    static ArrayList<String> testUniform(){
        ArrayList<String> instances = new ArrayList<>();

        for (int i=3; i<=8; ++i){
            for (int j = 1; j<=10; ++j){
                String instance = "uniform/noug" + i + "-rnd-" + String.format("%1$03d",j) + ".txt";
                instances.add(instance);
            }
        }

//        instances.clear();
//        instances.add("instances/noug8-rnd-010.txt");

        System.out.println(instances);
        return instances;
    }

    static ArrayList<String> testConnected(){

        String[] ins = {"connected/c1000_2000_100_2_1.txt",
                "connected/c1000_2000_100_2_2.txt",
                "connected/c1000_2000_100_2_3.txt",
                "connected/c1000_2000_25_2_1.txt",
                "connected/c1000_2000_25_2_2.txt",
                "connected/c1000_2000_25_2_3.txt",
                "connected/c1000_2000_50_2_1.txt",
                "connected/c1000_2000_50_2_2.txt",
                "connected/c1000_2000_50_2_3.txt",
                "connected/c1000_3000_100_2_1.txt",
                "connected/c1000_3000_100_2_2.txt",
                "connected/c1000_3000_100_2_3.txt",
                "connected/c1000_3000_25_2_1.txt",
                "connected/c1000_3000_25_2_2.txt",
                "connected/c1000_3000_25_2_3.txt",
                "connected/c1000_3000_50_2_1.txt",
                "connected/c1000_3000_50_2_2.txt",
                "connected/c1000_3000_50_2_3.txt",
                "connected/c1000_4000_100_2_1.txt",
                "connected/c1000_4000_100_2_2.txt",
                "connected/c1000_4000_100_2_3.txt",
                "connected/c1000_4000_25_2_1.txt",
                "connected/c1000_4000_25_2_2.txt",
                "connected/c1000_4000_25_2_3.txt",
                "connected/c1000_4000_50_2_1.txt",
                "connected/c1000_4000_50_2_2.txt",
                "connected/c1000_4000_50_2_3.txt",
                "connected/c1000_5000_100_2_1.txt",
                "connected/c1000_5000_100_2_2.txt",
                "connected/c1000_5000_100_2_3.txt",
                "connected/c1000_5000_25_2_1.txt",
                "connected/c1000_5000_25_2_2.txt",
                "connected/c1000_5000_25_2_3.txt",
                "connected/c1000_5000_50_2_2.txt",
                "connected/c1000_5000_50_2_3.txt",
                "connected/c1000_2000_100_4_1.txt",
                "connected/c1000_2000_100_4_2.txt",
                "connected/c1000_2000_100_4_3.txt",
                "connected/c1000_2000_25_4_1.txt",
                "connected/c1000_2000_25_4_2.txt",
                "connected/c1000_2000_25_4_3.txt",
                "connected/c1000_2000_50_4_1.txt",
                "connected/c1000_2000_50_4_2.txt",
                "connected/c1000_2000_50_4_3.txt",
                "connected/c1000_3000_100_4_1.txt",
                "connected/c1000_3000_100_4_2.txt",
                "connected/c1000_3000_100_4_3.txt",
                "connected/c1000_3000_25_4_1.txt",
                "connected/c1000_3000_25_4_2.txt",
                "connected/c1000_3000_25_4_3.txt",
                "connected/c1000_3000_50_4_1.txt",
                "connected/c1000_3000_50_4_2.txt",
                "connected/c1000_3000_50_4_3.txt",
                "connected/c1000_4000_100_4_1.txt",
                "connected/c1000_4000_100_4_2.txt",
                "connected/c1000_4000_100_4_3.txt",
                "connected/c1000_4000_25_4_1.txt",
                "connected/c1000_4000_25_4_2.txt",
                "connected/c1000_4000_25_4_3.txt",
                "connected/c1000_4000_50_4_1.txt",
                "connected/c1000_4000_50_4_2.txt",
                "connected/c1000_4000_50_4_3.txt",
                "connected/c1000_5000_100_4_1.txt",
                "connected/c1000_5000_100_4_2.txt",
                "connected/c1000_5000_100_4_3.txt",
                "connected/c1000_5000_25_4_1.txt",
                "connected/c1000_5000_25_4_2.txt",
                "connected/c1000_5000_25_4_3.txt",
                "connected/c1000_5000_50_4_1.txt",
                "connected/c1000_5000_50_4_2.txt",
                "connected/c1000_5000_50_4_3.txt",
                "connected/c1000_2000_100_8_1.txt",
                "connected/c1000_2000_100_8_2.txt",
                "connected/c1000_2000_100_8_3.txt",
                "connected/c1000_2000_25_8_1.txt",
                "connected/c1000_2000_25_8_2.txt",
                "connected/c1000_2000_25_8_3.txt",
                "connected/c1000_2000_50_8_1.txt",
                "connected/c1000_2000_50_8_2.txt",
                "connected/c1000_2000_50_8_3.txt",
                "connected/c1000_3000_100_8_1.txt",
                "connected/c1000_3000_100_8_2.txt",
                "connected/c1000_3000_100_8_3.txt",
                "connected/c1000_3000_25_8_1.txt",
                "connected/c1000_3000_25_8_2.txt",
                "connected/c1000_3000_50_8_2.txt",
                "connected/c1000_3000_50_8_3.txt",
                "connected/c1000_4000_100_8_1.txt",
                "connected/c1000_4000_100_8_2.txt",
                "connected/c1000_4000_100_8_3.txt",
                "connected/c1000_4000_25_8_1.txt",
                "connected/c1000_4000_25_8_2.txt",
                "connected/c1000_4000_50_8_3.txt",
                "connected/c1000_5000_25_8_1.txt",
                "connected/c1000_5000_25_8_2.txt"
                };

        ArrayList<String> instances = new ArrayList<>();
        for (String i : ins) {
            instances.add(i);
        }
        return instances;
    }

    static ArrayList<String> testNorth(){
        String[] ins = {"North/north.30.29.11.txt",
                "North/north.30.29.12.txt",
                "North/north.30.29.7.txt",
                "North/north.30.33.19.txt",
                "North/north.30.33.5.txt",
                "North/north.30.34.20.txt",
                "North/north.30.35.14.txt",
                "North/north.30.35.18.txt",
                "North/north.30.35.4.txt",
                "North/north.30.37.10.txt",
                "North/north.30.37.9.txt",
                "North/north.30.39.17.txt",
                "North/north.30.40.13.txt",
                "North/north.30.40.21.txt",
                "North/north.30.41.3.txt",
                "North/north.30.43.1.txt",
                "North/north.30.44.15.txt",
                "North/north.30.45.2.txt",
                "North/north.30.45.8.txt",
                "North/north.30.55.16.txt",
                "North/north.30.62.6.txt",
                "North/north.40.131.15.txt",
                "North/north.40.39.11.txt",
                "North/north.40.39.12.txt",
                "North/north.40.39.1.txt",
                "North/north.40.39.9.txt",
                "North/north.40.46.6.txt",
                "North/north.40.47.3.txt",
                "North/north.40.48.5.txt",
                "North/north.40.49.14.txt",
                "North/north.40.49.2.txt",
                "North/north.40.49.4.txt",
                "North/north.40.54.8.txt",
                "North/north.40.56.13.txt",
                "North/north.40.60.16.txt",
                "North/north.40.72.10.txt",
                "North/north.40.73.7.txt",
                "North/north.45.45.7.txt",
                "North/north.45.46.5.txt",
                "North/north.45.47.2.txt",
                "North/north.45.47.3.txt",
                "North/north.45.56.4.txt",
                "North/north.45.57.6.txt",
                "North/north.45.59.1.txt",
                "North/north.50.49.2.txt",
                "North/north.50.69.3.txt",
                "North/north.50.75.1.txt",
                "North/north.55.105.10.txt",
                "North/north.55.105.11.txt",
                "North/north.55.105.2.txt",
                "North/north.55.105.3.txt",
                "North/north.55.105.4.txt",
                "North/north.55.111.9.txt",
                "North/north.55.130.6.txt",
                "North/north.55.63.1.txt",
                "North/north.55.65.5.txt",
                "North/north.55.72.8.txt",
                "North/north.55.82.7.txt"
        };
        ArrayList<String> instances = new ArrayList<>();
        for (String i : ins) {
            instances.add(i);
        }
        return instances;

    }

    static ArrayList<String> testRome(){
        String[] ins = {"Rome/rome.10.10.43.txt",
                "Rome/rome.10.10.54.txt",
                "Rome/rome.11.12.65.txt",
                "Rome/rome.11.17.88.txt",
                "Rome/rome.12.11.10.txt",
                "Rome/rome.12.11.32.txt",
                "Rome/rome.12.13.81.txt",
                "Rome/rome.13.17.80.txt",
                "Rome/rome.13.19.87.txt",
                "Rome/rome.15.15.85.txt",
                "Rome/rome.15.18.75.txt",
                "Rome/rome.15.24.83.txt",
                "Rome/rome.16.17.21.txt",
                "Rome/rome.18.26.86.txt",
                "Rome/rome.20.21.5.txt",
                "Rome/rome.20.25.9.txt",
                "Rome/rome.20.26.78.txt",
                "Rome/rome.21.21.15.txt",
                "Rome/rome.21.24.40.txt",
                "Rome/rome.21.24.44.txt",
                "Rome/rome.21.24.46.txt",
                "Rome/rome.21.24.79.txt",
                "Rome/rome.21.26.11.txt",
                "Rome/rome.21.26.24.txt",
                "Rome/rome.21.27.20.txt",
                "Rome/rome.21.33.41.txt",
                "Rome/rome.21.36.25.txt",
                "Rome/rome.22.23.28.txt",
                "Rome/rome.22.24.55.txt",
                "Rome/rome.22.26.73.txt",
                "Rome/rome.22.27.52.txt",
                "Rome/rome.22.30.14.txt",
                "Rome/rome.22.31.34.txt",
                "Rome/rome.23.24.63.txt",
                "Rome/rome.23.24.76.txt",
                "Rome/rome.23.25.70.txt",
                "Rome/rome.23.26.19.txt",
                "Rome/rome.23.30.82.txt",
                "Rome/rome.24.25.47.txt",
                "Rome/rome.24.28.27.txt",
                "Rome/rome.24.32.50.txt",
                "Rome/rome.24.33.7.txt",
                "Rome/rome.25.37.51.txt",
                "Rome/rome.26.30.2.txt",
                "Rome/rome.26.31.61.txt",
                "Rome/rome.26.38.57.txt",
                "Rome/rome.27.28.62.txt",
                "Rome/rome.27.37.68.txt",
                "Rome/rome.28.29.12.txt",
                "Rome/rome.28.31.1.txt",
                "Rome/rome.28.32.71.txt",
                "Rome/rome.28.34.29.txt",
                "Rome/rome.28.35.36.txt",
                "Rome/rome.28.38.4.txt",
                "Rome/rome.29.28.35.txt",
                "Rome/rome.29.30.31.txt",
                "Rome/rome.29.32.45.txt",
                "Rome/rome.29.40.69.txt",
                "Rome/rome.30.31.3.txt",
                "Rome/rome.30.31.53.txt",
                "Rome/rome.30.32.48.txt",
                "Rome/rome.30.33.60.txt",
                "Rome/rome.30.35.64.txt",
                "Rome/rome.30.35.74.txt",
                "Rome/rome.30.36.77.txt",
                "Rome/rome.30.36.84.txt",
                "Rome/rome.30.40.23.txt",
                "Rome/rome.30.40.59.txt",
                "Rome/rome.31.34.38.txt",
                "Rome/rome.31.36.18.txt",
                "Rome/rome.31.37.33.txt",
                "Rome/rome.31.40.49.txt",
                "Rome/rome.31.40.56.txt",
                "Rome/rome.32.37.13.txt",
                "Rome/rome.33.34.42.txt",
                "Rome/rome.33.37.30.txt",
                "Rome/rome.33.38.39.txt",
                "Rome/rome.33.42.66.txt",
                "Rome/rome.35.42.16.txt",
                "Rome/rome.35.42.67.txt",
                "Rome/rome.38.48.26.txt",
                "Rome/rome.40.42.8.txt",
                "Rome/rome.40.49.22.txt",
                "Rome/rome.41.45.72.txt",
                "Rome/rome.41.54.37.txt",
                "Rome/rome.43.58.6.txt",
                "Rome/rome.48.58.17.txt",
                "Rome/rome.49.62.58.txt"
        };
        ArrayList<String> instances = new ArrayList<>();
        for (String i : ins) {
            instances.add(i);
        }
        return instances;
    }

    public static void main(String[] args) throws IOException {
	// write your code here

        String resFile = "resTotal.csv";
        BufferedWriter bf = new BufferedWriter(new FileWriter(resFile, true));
        bf.write("\ninstance, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, best, bestTime, bestIter\n");
        bf.close();

//        ArrayList<String> instances = testConnected();
        ArrayList<String> instances = testUniform();
//        ArrayList<String> instances = testNorth();
//        ArrayList<String> instances = testRome();

        for(String instance : instances) {
            ArrayList<MMACSolver.Solution> sols = new ArrayList<>();

            bf = new BufferedWriter(new FileWriter(resFile, true));
            bf.write(instance);
            bf.write(",");
            bf.close();

            for (int i = 0; i < 10; ++i) {
                System.out.println(instance + "\t run " + i);
                MMACSolver solver = new MMACSolver(instance, i);
                solver.solve();
                MMACSolver.Solution solution = solver.getBestSol();
                sols.add(solution);

                bf = new BufferedWriter(new FileWriter(resFile, true));
                bf.write(String.valueOf(solution.getM()));
                bf.write(",");
                bf.close();
            }

            MMACSolver.Solution bestSol = sols.stream().min(Comparator.comparing(MMACSolver.Solution::getM)).get();
            bf = new BufferedWriter(new FileWriter(resFile, true));
            bf.write(String.valueOf(bestSol.getM()));
            bf.write(",");
            bf.write(String.valueOf(bestSol.getTimeToSol()));
            bf.write(",");
            bf.write(String.valueOf(bestSol.getIterations()));
            bf.write("\n");
            bf.close();
        }
    }
}

import mmac.MMACSolver;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws IOException {
	// write your code here

        ArrayList<String> instances = new ArrayList<>();

        for (int i=8; i<=8; ++i){
            for (int j = 4; j<=10; ++j){
                String instance = "noug" + i + "-rnd-" + String.format("%1$03d",j) + ".txt";
                instances.add(instance);
            }
        }

        instances.clear();
        instances.add("noug8-rnd-010.txt");

        System.out.println(instances);


        for(String instance : instances) {
            ArrayList<Integer> objs = new ArrayList<>();
            ArrayList<Double> times = new ArrayList<>();

            for (int i = 0; i < 500; ++i) {
                MMACSolver solver = new MMACSolver("instances/" + instance, i);
                solver.solve();
                int obj = solver.getObj();
                double time = solver.getTime();
                objs.add(obj);
                times.add(time);
            }
            System.out.println(objs);
//            System.out.println(objs.stream().min(Integer::compareTo).get());
            System.out.println(times);


            BufferedWriter bf = new BufferedWriter(new FileWriter("resTotal.csv", true));

            bf.write(instance);
            bf.write(",");
            bf.write(String.valueOf(objs.stream().min(Integer::compareTo).get()));
            bf.write("\n");
            bf.close();
        }
    }
}

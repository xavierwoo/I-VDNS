import mmac.MMACSolver;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
	// write your code here
//        MMACSolver solver = new MMACSolver("instances/test.txt");
        MMACSolver solver = new MMACSolver("instances/noug3-rnd-001.txt");
//        MMACSolver solver = new MMACSolver("instances/noug8-rnd-009.txt");
        solver.solve();
    }
}

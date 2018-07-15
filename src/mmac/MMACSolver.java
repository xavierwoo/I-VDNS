package mmac;

import java.io.*;
import java.util.*;

import org.jetbrains.annotations.NotNull;
import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

public class MMACSolver {

    private final int LAMBDA = 10000;
    private final int MOVE_MAX_DISTANCE = 10;
    private ArrayList<ArrayList<Node>> layers;
    private FibonacciHeap<Edge> edgeFibonacciHeap = new FibonacciHeap<>();
    private Random random = new Random();
    private String instance;
    //private HashSet<Edge> mostCrossEdges = new HashSet<>();
    private long startTime;

    public MMACSolver(String instanceFile) throws IOException {
        instance = instanceFile;
        BufferedReader br = new BufferedReader(new FileReader(instanceFile));

        String line = br.readLine();

        String[] data = line.split(" ");

        int nodeNum = Integer.parseInt(data[0]);
        int edgeNum = Integer.parseInt(data[1]);
        int layerNum = Integer.parseInt(data[2]);

        var allNodes = new ArrayList<Node>(nodeNum);
        for (int i = 1; i <= nodeNum; ++i) {
            allNodes.add(new Node(i, -1));
        }

        layers = new ArrayList<>(layerNum);

        line = br.readLine();
        data = line.split(" ");

        int sIndex = 0;
        int layerId = 0;
        for (String s : data) {
            int eIndex = sIndex + Integer.parseInt(s);
            var lay = new ArrayList<>(allNodes.subList(sIndex, eIndex));
            for (int i = 0; i < lay.size(); ++i) {
                lay.get(i).pos = i;
                lay.get(i).layerID = layerId;
            }
            layers.add(lay);
            ++layerId;
            sIndex = eIndex;
        }

        for (int i = 0; i < edgeNum; ++i) {
            line = br.readLine();
            data = line.split(" ");
            int source = Integer.parseInt(data[0]);
            int sink = Integer.parseInt(data[1]);
            Edge edge = new Edge(allNodes.get(source - 1), allNodes.get(sink - 1));
            allNodes.get(source - 1).outEdges.add(edge);
            allNodes.get(sink - 1).inEdges.add(edge);
        }

        br.close();
        //checkGraph();
    }

    public void solve() throws IOException {
        startTime = System.currentTimeMillis();
        init();
        localSearch();
    }

    private void initM() {
        edgeFibonacciHeap.clear();
        for (ArrayList<Node> layer : layers) {
            for (Node n : layer) {
                for (Edge e : n.outEdges) {
                    int lam = calcCross(e, layer);
                    edgeFibonacciHeap.insert(e.fHeapNode, 1.0 / lam);
                }
            }
        }
    }

    private int getM(){
        return edgeFibonacciHeap.min().getData().cross;
    }

    /***
     * check if (i,j) and (k, l) is a cross
     * @param i pos of the layer
     * @param j pos of the layer
     * @param k pos of the layer
     * @param l pos of the layer
     * @return whether is a cross
     */
    private boolean isCross(int i, int j, int k, int l) {
        return i < k && j > l
                ||
                i > k && j < l;
    }

    private int calcCross(Edge e, ArrayList<Node> layer) {
        int lam = 0;
        for (Node n : layer) {
            if (n == e.source) continue;
            for (Edge ep : n.outEdges) {
                if (ep.sink == e.sink) continue;
                lam += isCross(e.source.pos, e.sink.pos, ep.source.pos, ep.sink.pos) ?
                        1 : 0;
            }
        }
        e.cross = lam;
        return lam;
    }

//    private boolean checkGraph(){
//        for (ArrayList<Node> layer : layers){
//            for (Node node : layer){
//                for(Edge e : node.outEdges){
//                    Node source = e.source;
//                    if (source != node){
//                        throw new Error("checkGraph 1");
//                    }
//                    Node sink = e.sink;
//                    if (!sink.inEdges.contains(e)){
//                        throw new Error("checkGraph 2");
//                    }
//                }
//                for (Edge e : node.inEdges){
//                    Node source = e.source;
//                    Node sink = e.sink;
//                    if (sink != node){
//                        throw new Error("checkGraph 3");
//                    }
//                    if (!source.outEdges.contains(e)){
//                        throw new Error("checkGraph 4");
//                    }
//                }
//            }
//        }
//        return true;
//    }

    private void init() {
        System.out.println("Initializing...");
        constructSolution();
        initM();
        System.out.println("Initial obj: "+ edgeFibonacciHeap.min().getData().cross);
    }

    private void constructSolution(){
        var CL = new HashSet<Node>();
        var V = new HashSet<Node>();
        for (ArrayList<Node> layer : layers){
            CL.addAll(layer);
        }

        int lP = random.nextInt(layers.size());
        int vP = random.nextInt(layers.get(lP).size());
        Node vStar = layers.get(lP).get(vP);
        vStar.pos = 0;
        CL.remove(vStar);
        V.add(vStar);

        while(!CL.isEmpty()){
            ArrayList<Node> RCL = getRCL(V);
            vStar = RCL.get(random.nextInt(RCL.size()));
            int bc = calcBC(vStar, V);
            vStar.pos = findBCNearst(layers.get(vStar.layerID), bc, V);

            CL.remove(vStar);
            V.add(vStar);
        }

        for (ArrayList<Node> layer : layers){
            layer.sort(Comparator.comparing(a->a.pos));
            for (int i=0; i<layer.size(); ++i){
                layer.get(i).pos = i;
            }
        }
    }

    private int findBCNearst(ArrayList<Node> layer, int bc, HashSet<Node> V){
        var usedPos = new HashSet<Integer>();
        for (Node node : layer){
            if (V.contains(node)){
                usedPos.add(node.pos);
            }
        }
        if (!usedPos.contains(bc)) return bc;
        for (int i=1; ;++i){
            if (!usedPos.contains(bc + i)) return bc +i;
            if (!usedPos.contains(bc-i)) return bc - i;
        }
    }

    private int calcBC(Node vStar, HashSet<Node> V){
        int count = 0;
        int sum = 0;
        for (Edge e : vStar.outEdges){
            if (V.contains(e.sink)){
                ++count;
                sum += e.sink.pos;
            }
        }
        for (Edge e : vStar.inEdges){
            if (V.contains(e.source)){
                ++count;
                sum += e.source.pos;
            }
        }
        return sum / count;
    }

    private int calcD(Node node, HashSet<Node> V){
        int d = 0;
        for (Edge e : node.outEdges){
            if (V.contains(e.sink))++d;
        }
        for (Edge e:node.inEdges){
            if (V.contains(e.source))++d;
        }
        return d;
    }

    private ArrayList<Node> getRCL(HashSet<Node> V){
        var RCL = new ArrayList<Node>();
        for (Node node : V){
            for (Edge e : node.outEdges){
                RCL.add(e.sink);
            }
            for (Edge e : node.inEdges){
                RCL.add(e.source);
            }
        }
        return RCL;
    }

    private void localSearch() throws IOException {
        int iter = 0;

        Solution bestSol = new Solution(this, false);
        boolean wFlag = false;
        var moveDistances = new ArrayList<Integer>();
        for(;;++iter) {
            Move mv = findMove();
            if (!wFlag && mv.delta.deltaM >= 0){
                wFlag = true;
                bestSol = new Solution(this, false);
            }
            if (mv.delta.deltaCross >=0 && mv.delta.deltaM >=0 )break;
            moveDistances.add(Math.abs(mv.newPos - mv.node.pos));
            makeMove(mv);
            int obj = edgeFibonacciHeap.min().getData().cross;
            System.out.println("Iteration: " + iter + " Obj:" + obj);

            if (wFlag && obj < bestSol.M){
                bestSol = new Solution(this, false);
            }

            if (obj == 0) break;

        }

        checkSolution();
        if (edgeFibonacciHeap.min().getData().cross <= bestSol.M) {
            new Solution(this, true);
        }
        System.out.println("Iteration: " + iter);
        System.out.println(edgeFibonacciHeap.min().getData().cross);

        writeMoveDistances(moveDistances);
    }

    private void writeMoveDistances(ArrayList<Integer> distances) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("moveDistances.txt"));
        for (Integer d : distances){
            bw.write(d + ",");
        }
        bw.close();
    }

    private Delta checkMoveCross(Node n, int newPos){

        int currM = getM();
        Delta delta = new Delta(0,0);

        int lbIndex;
        int ubIndex;
        if (n.pos < newPos){
            lbIndex = n.pos;
            ubIndex = newPos;
        }else{
            lbIndex = newPos;
            ubIndex = n.pos;
        }

        ArrayList<Node> layer = layers.get(n.layerID);
        for(int index = lbIndex; index<=ubIndex; ++index){
            Node node = layer.get(index);
            for (Edge e : node.outEdges){
                int iO = node.pos;
                int iN = calcNewPos(n.pos, newPos, node.pos);
                int j = e.sink.pos;
                int deltaCross = 0;
                for (int indexP = lbIndex; indexP<=ubIndex; ++indexP){
                    Node nodeP = layer.get(indexP);
                    if (nodeP == node)continue;
                    for (Edge eP : nodeP.outEdges){
                        int kO = nodeP.pos;
                        int kN = calcNewPos(n.pos, newPos, nodeP.pos);
                        int l = eP.sink.pos;
                        if (j == l)continue;
                        deltaCross += (isCross(iN,j,kN,l) ? 1 : 0)
                        - (isCross(iO, j, kO, l) ? 1 : 0);
                    }
                }
                updateDelta(delta, deltaCross, e.cross, currM);
                if (delta.deltaM > LAMBDA / 2){
                    //System.err.println("Drop move, deltaM:" + delta.deltaM);
                    return null;
                }
            }
        }

        for(int index = lbIndex; index<=ubIndex; ++index){
            Node node = layer.get(index);
            for (Edge e : node.inEdges){
                int i = e.source.pos;
                int jO = node.pos;
                int jN = calcNewPos(n.pos, newPos, e.sink.pos);
                int deltaCross = 0;
                for (int  indexP = lbIndex; indexP<=ubIndex; ++indexP){
                    Node nodeP = layer.get(indexP);
                    if (nodeP == node) continue;
                    for (Edge eP : nodeP.inEdges){
                        int k = eP.source.pos;
                        if (k == i)continue;
                        int lO = nodeP.pos;
                        int lN = calcNewPos(n.pos, newPos, eP.sink.pos);
                        deltaCross += (isCross(i,jN,k,lN) ? 1 : 0)
                        -(isCross(i, jO, k, lO) ? 1 : 0);
                    }
                }
                updateDelta(delta, deltaCross, e.cross, currM);
                if (delta.deltaM > LAMBDA / 2){
                    //System.err.println("Drop move, deltaM:" + delta.deltaM);
                    return null;
                }
            }
        }

        return delta;
    }

    private void updateDelta(Delta delta, int deltaCross, int oriCross, int M){
        delta.deltaCross += deltaCross;
        int newCross = oriCross + deltaCross;
        if (oriCross < M && newCross == M) {
            delta.deltaM += 1;
        }else if (oriCross <= M && newCross > M){
            delta.deltaM += LAMBDA * (newCross - M);
        }else if (oriCross == M && newCross < M){
            delta.deltaM -= 1;
        }
    }

    private int calcNewPos(int mvNodePos, int mvNodeNewPos, int pos){
        if (mvNodePos == pos){
            return mvNodeNewPos;
        }else if (mvNodePos < mvNodeNewPos){
            if (pos < mvNodePos || pos > mvNodeNewPos){
                return pos;
            }else{
                return pos - 1;
            }
        }else{
            if (pos < mvNodeNewPos || pos > mvNodePos){
                return pos;
            }else{
                return pos + 1;
            }
        }
    }

    private Move findMove() {
        int bestCount = 0;
        Move bestMv = new Move(null, -1, new Delta(Integer.MAX_VALUE, 0));
        for (ArrayList<Node> layer : layers){
            for (Node n : layer){
                for (int i = 0; i < layer.size(); ++i){
                    if (i == n.pos || i == n.pos - 1 || Math.abs(n.pos - i) > MOVE_MAX_DISTANCE)continue;
                    Delta delta = checkMoveCross(n, i);
                    if (delta == null)continue;
//                    if(delta.deltaM < 0){
//                        return new Move(n,i,delta);
//                    }
                    int cmp = delta.compareTo(bestMv.delta);
                    if( cmp < 0){
                        bestMv = new Move(n, i, delta);
                        bestCount = 1;
                    }else if(cmp == 0 && random.nextInt(++bestCount) == 0){
                        bestMv = new Move(n, i, delta);
                    }
                }
            }
        }
        return bestMv;
    }

    private void makeMove(Move mv){
        System.out.println(mv);
        ArrayList<Node> layer = layers.get(mv.node.layerID);

        int lbIndex;
        int ubIndex;
        if (mv.newPos > mv.node.pos){
            lbIndex = mv.node.pos;
            ubIndex = mv.newPos;
        }else{
            lbIndex = mv.newPos;
            ubIndex = mv.node.pos;
        }

        for(int index = lbIndex; index<=ubIndex; ++index){
            Node node = layer.get(index);
            for (Edge e : node.outEdges){
                int iO= node.pos;
                int iN = calcNewPos(mv.node.pos, mv.newPos, node.pos);
                int j = e.sink.pos;
                int deltaCross = 0;
                for (int indexP = lbIndex; indexP<=ubIndex; ++indexP){
                    Node nodeP = layer.get(indexP);
                    if (nodeP == node)continue;
                    for (Edge eP :nodeP.outEdges){
                        int kO = nodeP.pos;
                        int kN = calcNewPos(mv.node.pos, mv.newPos, nodeP.pos);
                        int l = eP.sink.pos;
                        if (j==l)continue;
                        deltaCross += (isCross(iN,j,kN,l) ? 1 : 0)
                                - (isCross(iO, j, kO, l) ? 1 : 0);
                    }
                }
                updateEdgeCross(e, e.cross + deltaCross);
            }
        }

        for(int index = lbIndex; index<=ubIndex; ++index){
            Node node = layer.get(index);
            for (Edge e : node.inEdges){
                int i = e.source.pos;
                int jO = node.pos;
                int jN = calcNewPos(mv.node.pos, mv.newPos, node.pos);
                int deltaCross = 0;
                for(int indexP = lbIndex; indexP<=ubIndex; ++indexP){
                    Node nodeP = layer.get(indexP);
                    if (nodeP == node) continue;
                    for (Edge eP : nodeP.inEdges){
                        int k = eP.source.pos;
                        if (k == i)continue;
                        int lO = nodeP.pos;
                        int lN = calcNewPos(mv.node.pos, mv.newPos, nodeP.pos);
                        deltaCross += (isCross(i,jN,k,lN) ? 1 : 0)
                        -(isCross(i, jO, k, lO) ? 1 : 0);
                    }
                }
                updateEdgeCross(e, e.cross + deltaCross);
            }
        }

        //resort nodes in layer
        if (mv.newPos > mv.node.pos){
            for(int i = mv.node.pos; i < mv.newPos; ++i){
                layer.set(i, layer.get(i+1));
                layer.get(i).pos -= 1;
            }
        }else{
            for(int i = mv.node.pos; i >mv.newPos; --i){
                layer.set(i, layer.get(i-1));
                layer.get(i).pos += 1;
            }
        }
        layer.set(mv.newPos, mv.node);
        mv.node.pos = mv.newPos;
    }

    private void updateEdgeCross(Edge e, int newCross){
        if(e.cross == newCross)return;
        if (newCross < e.cross){
            //System.out.println("update edge " + e + " D oriCross: " + e.cross + " newCross:" + newCross);
            edgeFibonacciHeap.delete(e.fHeapNode);
            edgeFibonacciHeap.insert(e.fHeapNode, 1.0/newCross);
        }else{
            //System.out.println("update edge " + e + " U oriCross: " + e.cross + " newCross:" + newCross);
            edgeFibonacciHeap.decreaseKey(e.fHeapNode, 1.0/newCross);
        }
        e.cross = newCross;
    }

    private void checkSolution(){
        int M = 0;
        for (ArrayList<Node> layer : layers){
            for (Node node : layer){
                for (Edge e : node.outEdges){
                    int i=e.source.pos;
                    int j = e.sink.pos;
                    int currCross = 0;
                    for(Node nodeP : layer){
                        if (nodeP == node)continue;
                        for (Edge eP : nodeP.outEdges){
                            int k = eP.source.pos;
                            int l = eP.sink.pos;
                            if(j == l)continue;
                            currCross += isCross(i,j,k,l) ? 1 : 0;
                        }
                    }
                    if (currCross != e.cross){
                        System.err.println("Edge: " + e + " currCross: " + currCross + ", e.cross: " + e.cross);
                        throw new Error("Cross error!");
                    }
                    if (Double.compare(1.0/e.cross, e.fHeapNode.getKey()) != 0){
                        throw new Error("Cross consistency error!");
                    }

                    if (M < currCross){
                        M = currCross;
                    }
                }
            }
        }
        if (edgeFibonacciHeap.min().getData().cross != M){
            throw new Error("FibonacciHeap error!");
        }
    }

    private class MvDisManager{
        private int[] lastDis = new int[10];
        private int pos = 0;

        void record(int dis){
            lastDis[pos] = dis;
            ++pos;
            if (pos == lastDis.length) pos = 0;
        }

        int getMax(){
            int m = 0;
            for (int d : lastDis){
                if (d > m) m = d;
            }
            return m;
        }
    }

    private class Node {
        int ID;
        int layerID;
        int pos;
        ArrayList<Edge> outEdges = new ArrayList<>();
        ArrayList<Edge> inEdges = new ArrayList<>();

        Node(int id, int lyid) {
            ID = id;
            layerID = lyid;
        }

        @Override
        public String toString() {
            return String.valueOf(ID);
        }

        @Override
        public int hashCode(){
            return Objects.hash(ID);
        }

        @Override
        public boolean equals(Object o){
            return o!=null && o.getClass() == Node.class && ID == ((Node)o).ID;
        }
    }

    private class Edge {
        Node source;
        Node sink;

        int cross = 0;

        FibonacciHeapNode<Edge> fHeapNode;

        Edge(Node src, Node snk) {
            source = src;
            sink = snk;
            fHeapNode = new FibonacciHeapNode<>(this);
        }

        @Override
        public String toString() {
            return "(" + source + "," + sink + ")";
        }

        @Override
        public int hashCode() {
            return Objects.hash(source.ID, sink.ID);
        }

        @Override
        public boolean equals(Object o) {
            return o != null && o.getClass() == Edge.class
                    && source.ID == ((Edge) o).source.ID
                    && sink.ID == ((Edge) o).sink.ID;
        }
    }

    private class Move {
        Node node;
        int newPos;
        Delta delta;

        Move(Node n, int p, Delta d) {
            node = n;
            newPos = p;
            delta = d;
        }

        public String toString(){
            return "Move Node: " + node.ID + ", " + node.pos + "->" + newPos + "/" +delta;
        }
    }

    private class Solution{
        int M;
        ArrayList<ArrayList<Integer>> sol;
        String instance;

        Solution(MMACSolver solver, boolean isFinal) throws IOException {
            solver.checkSolution();
            instance = solver.instance;
            M = solver.edgeFibonacciHeap.min().getData().cross;
            sol = new ArrayList<>(solver.layers.size());
            for (ArrayList<Node> layer : solver.layers){
                ArrayList<Integer> nodeIndexes = new ArrayList<>(layer.size());
                for (Node n : layer){
                    nodeIndexes.add(n.ID);
                }
                sol.add(nodeIndexes);
            }
            write(isFinal);
        }

        void write(boolean isFinal) throws IOException {
            String outFile = "sol/sol" + M + (isFinal ? "F" : "")  + ".txt";
            BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));

            bw.write(instance);
            bw.newLine();
            bw.write(("Time: " + (System.currentTimeMillis() - startTime)/1000.0) + " seconds\n");
            bw.write("Objective: " + String.valueOf(M));
            bw.newLine();
            bw.write("Solution: \n");
            for (ArrayList<Integer> layer : sol){
                for (Integer n : layer){
                    bw.write(n + " ");
                }
                bw.newLine();
            }
            bw.close();
        }
    }

    private class Delta implements Comparable{
        int deltaM;
        int deltaCross;
        Delta(int dM, int dC){
            deltaM = dM;
            deltaCross = dC;
        }

        @Override
        public String toString(){
            return "dM: " + deltaM + ", dC: " + deltaCross;
        }

        @Override
        public int compareTo(@NotNull Object o) {
            Delta oo = (Delta)o;
            if (deltaM < oo.deltaM){
                return -1;
            }else if (deltaM > oo.deltaM){
                return 1;
            }else{
                return Integer.compare(deltaCross, oo.deltaCross);
            }
        }
    }
}

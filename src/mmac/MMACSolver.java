package mmac;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.jetbrains.annotations.NotNull;
import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

public class MMACSolver {

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

//        @Override
//        public int hashCode(){
//            return Objects.hash(ID);
//        }
//
//        @Override
//        public boolean equals(Object o){
//            return o!=null && o.getClass() == Node.class && ID == ((Node)o).ID;
//        }
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
    }

    private class Delta implements Comparable{
        int deltaM;
        int deltaCross;
        Delta(int dM, int dC){
            deltaM = dM;
            deltaCross = dC;
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

    private ArrayList<ArrayList<Node>> layers;
    //private HashSet<Edge> mostCrossEdges = new HashSet<>();

    private FibonacciHeap<Edge> edgeFibonacciHeap = new FibonacciHeap<>();

    private Random random = new Random(0);

    public MMACSolver(String instanceFile) throws IOException {
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

    public int solve() {
        init();
        localSearch();
        return 0;
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

    private void init() {
        initM();
        //checkSolution();
        System.out.println("Initial obj: "+ edgeFibonacciHeap.min().getData().cross);
    }

    private void localSearch() {
        int iter = 0;
        for(;;++iter) {
            Move mv = findMove();
            if (mv.delta.deltaCross >=0 && mv.delta.deltaM >=0 )break;
            makeMove(mv);
            checkSolution();//TODO: remove this line
            int obj = edgeFibonacciHeap.min().getData().cross;
            System.out.println("Iteration: " + iter + " Obj:" + obj);

            if (obj == 0)break;
        }

        checkSolution();
        System.out.println("Iteration: " + iter);
        System.out.println(edgeFibonacciHeap.min().getData().cross);
    }

    private Delta checkMoveCross(Node n, int newPos){

        int currM = getM();
        Delta delta = new Delta(0,0);

        for (Node node : layers.get(n.layerID)){
            for (Edge e : node.outEdges){
                int i = calcNewPos(n.pos, newPos, node.pos);
                int j = e.sink.pos;
                int currCross = 0;
                for (Node nodeP : layers.get(n.layerID)){
                    if (nodeP == node)continue;
                    for (Edge eP : nodeP.outEdges){
                        int k = calcNewPos(n.pos, newPos, nodeP.pos);
                        int l = eP.sink.pos;
                        if (j == l)continue;
                        currCross += isCross(i,j,k,l) ? 1 : 0;
                    }
                }
                updateDelta(delta, currCross, e.cross, currM);
            }
        }

        for (Node node : layers.get(n.layerID)){
            for (Edge e : node.inEdges){
                int i = e.source.pos;
                int j = calcNewPos(n.pos, newPos, e.sink.pos);
                int currCross = 0;
                for (Node nodeP : layers.get(n.layerID)){
                    if (nodeP == node) continue;
                    for (Edge eP : nodeP.inEdges){
                        int k = eP.source.pos;
                        if (k == i)continue;
                        int l = calcNewPos(n.pos, newPos, eP.sink.pos);
                        currCross += isCross(i,j,k,l) ? 1 : 0;
                    }
                }
                updateDelta(delta, currCross, e.cross, currM);
            }
        }

        return delta;
    }

    private void updateDelta(Delta delta, int newCross, int oriCross, int M){
        delta.deltaCross += newCross - oriCross;
        if (oriCross < M && newCross >= M){
            delta.deltaM += 1;
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
                    if (i == n.pos)continue;
                    Delta delta = checkMoveCross(n, i);
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
        ArrayList<Node> layer = layers.get(mv.node.layerID);

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

        for (Node node : layer){
            for (Edge e : node.outEdges){
                int i= e.source.pos;
                int j = e.sink.pos;
                int currCross = 0;
                for (Node nodeP: layer){
                    if (nodeP == node)continue;
                    for (Edge eP :nodeP.outEdges){
                        int k = eP.source.pos;
                        int l = eP.sink.pos;
                        if (j==l)continue;
                        currCross += isCross(i,j,k,l) ? 1 : 0;
                    }
                }
                updateEdgeCross(e, currCross);
            }
        }

        for (Node node : layer){
            for (Edge e : node.inEdges){
                int i = e.source.pos;
                int j = e.sink.pos;
                int currCross = 0;
                for (Node nodeP : layer){
                    if (nodeP == node) continue;
                    for (Edge eP : nodeP.inEdges){
                        int k = eP.source.pos;
                        if (k == i)continue;
                        int l = eP.sink.pos;
                        currCross += isCross(i,j,k,l) ? 1 : 0;
                    }
                }
                updateEdgeCross(e, currCross);
            }
        }
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

    private boolean checkSolution(){
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
                }
            }
        }
        return true;
    }
}

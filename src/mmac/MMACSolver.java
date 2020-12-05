package mmac;

import java.io.*;
import java.util.*;

public class MMACSolver {

    private final int LAMBDA = 10000;
    private final int MAX_MOVE_DISTANCE;
    private final int DISTANCE_MEMORY = 5;
    private final double TIME_LIMIT = 60;
    private final float PERTURB_STRENGTH = 0.1f;
    private int moveMaxDistance;
    private ArrayList<ArrayList<Node>> layers;
    private ArrayList<Node> allNodes;
    private Random random;
    private String instance;
    private long startTime;

    private Solution bestSol;
    int iterationLS = 1;

    public MMACSolver(String instanceFile, int seed) throws IOException {
        random = seed >= 0 ? new Random(seed) : new Random();
        instance = instanceFile;
        BufferedReader br = new BufferedReader(new FileReader(instanceFile));

        String line = br.readLine();

        String[] data = line.split(" ");

        int nodeNum = Integer.parseInt(data[0]);
        int edgeNum = Integer.parseInt(data[1]);
        int layerNum = Integer.parseInt(data[2]);

        allNodes = new ArrayList<>(nodeNum);
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

        int md = 0;
        for (ArrayList<Node> layer : layers) {
            if (md < layer.size()) md = layer.size();
        }
        MAX_MOVE_DISTANCE = md;
    }

    public void solve() throws IOException {
        startTime = System.currentTimeMillis();

        init();
        bestSol = new Solution(this);
        for(;;) {
            localSearch();

            double usedTime = (System.currentTimeMillis() - startTime)/1000.0;
            if(usedTime < TIME_LIMIT){
                randomShufflePerturb(PERTURB_STRENGTH);
            }else {
                break;
            }
            if (bestSol.M == 0)break;
        }
    }

    public Solution getBestSol() {
        return bestSol;
    }

    private void initM() {
        for (Node n : allNodes) {
            for (Edge e : n.outEdges) {
                calcCross(e, layers.get(n.layerID));
            }
        }
        calcAllNodeMaxCross();
        allNodes.sort((a, b) -> -Integer.compare(a.maxCross, b.maxCross));
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

    private void calcCross(Edge e, ArrayList<Node> layer) {
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
    }

    private void init() {
        System.out.println("Initializing...");
        randomConstruction();
        initM();
        System.out.println("Initial obj: " + allNodes.get(0).maxCross);
    }

    private void randomConstruction() {
        for (ArrayList<Node> layer : layers) {
            Collections.shuffle(layer, random);

            for (int i = 0; i < layer.size(); ++i) {
                layer.get(i).pos = i;
            }
        }
    }

    private void constructSolution() {
        var CL = new HashSet<Node>();
        var V = new HashSet<Node>();
        for (ArrayList<Node> layer : layers) {
            CL.addAll(layer);
        }

        int lP = random.nextInt(layers.size());
        int vP = random.nextInt(layers.get(lP).size());
        Node vStar = layers.get(lP).get(vP);
        vStar.pos = 0;
        CL.remove(vStar);
        V.add(vStar);

        while (!CL.isEmpty()) {
            ArrayList<Node> RCL = getRCL(V);
            vStar = RCL.get(random.nextInt(RCL.size()));
            int bc = calcBC(vStar, V);
            vStar.pos = findBCNearst(layers.get(vStar.layerID), bc, V);

            CL.remove(vStar);
            V.add(vStar);
        }

        for (ArrayList<Node> layer : layers) {
            layer.sort(Comparator.comparing(a -> a.pos));
            for (int i = 0; i < layer.size(); ++i) {
                layer.get(i).pos = i;
            }
        }
    }

    private int findBCNearst(ArrayList<Node> layer, int bc, HashSet<Node> V) {
        var usedPos = new HashSet<Integer>();
        for (Node node : layer) {
            if (V.contains(node)) {
                usedPos.add(node.pos);
            }
        }
        if (!usedPos.contains(bc)) return bc;
        for (int i = 1; ; ++i) {
            if (!usedPos.contains(bc + i)) return bc + i;
            if (!usedPos.contains(bc - i)) return bc - i;
        }
    }

    private int calcBC(Node vStar, HashSet<Node> V) {
        int count = 0;
        int sum = 0;
        for (Edge e : vStar.outEdges) {
            if (V.contains(e.sink)) {
                ++count;
                sum += e.sink.pos;
            }
        }
        for (Edge e : vStar.inEdges) {
            if (V.contains(e.source)) {
                ++count;
                sum += e.source.pos;
            }
        }
        return sum / count;
    }

    private ArrayList<Node> getRCL(HashSet<Node> V) {
        var RCL = new ArrayList<Node>();
        for (Node node : V) {
            for (Edge e : node.outEdges) {
                RCL.add(e.sink);
            }
            for (Edge e : node.inEdges) {
                RCL.add(e.source);
            }
        }
        return RCL;
    }

    private void localSearch() throws IOException {



        boolean wFlag = false;
        moveMaxDistance = MAX_MOVE_DISTANCE;


        for (; ; ++iterationLS) {

            Move mv = findMove();
            if (!wFlag && mv.delta >= 0) {
                wFlag = true;
                if(bestSol.M > allNodes.get(0).maxCross) {
                    bestSol = new Solution(this);
                }
            }

            if (mv.delta >= 0) {
                iterationLS++;
                break;
            }

            makeMove(mv);
            int obj = allNodes.get(0).maxCross;

            if (iterationLS % 1000 == 0) {
                System.out.println("Iteration: " + iterationLS + ", Obj: " + obj + ", Best: " + bestSol.M + ", MvDis: " + moveMaxDistance);
            }
            if (wFlag && obj < bestSol.M) {
                System.out.println("Iteration: " + iterationLS + ", Obj: " + obj + ", Best: " + bestSol.M + ", MvDis: " + moveMaxDistance);
                bestSol = new Solution(this);
            }

            if (obj == 0) break;
        }

        if (allNodes.get(0).maxCross < bestSol.M) {
            bestSol = new Solution(this);
        }
    }


    public int getObj() {
        return bestSol.M;
    }

    public double getTime() {
        return bestSol.timeToSol;
    }


    private int calcNewPos(int mvNodePos, int mvNodeNewPos, int pos) {
        if (mvNodePos == pos) {
            return mvNodeNewPos;
        } else if (mvNodePos < mvNodeNewPos) {
            if (pos < mvNodePos || pos > mvNodeNewPos) {
                return pos;
            } else {
                return pos - 1;
            }
        } else {
            if (pos < mvNodeNewPos || pos > mvNodePos) {
                return pos;
            } else {
                return pos + 1;
            }
        }
    }


    private void calcAllNodeMaxCross() {
        for (Node node : allNodes) {
            calcNodeMaxCross(node);
        }
    }

    private void calcNodeMaxCross(Node node) {
        Edge maxOutEdge = node.outEdges.stream().max(Comparator.comparing(a -> a.cross)).orElse(null);
        Edge maxInEdge = node.inEdges.stream().max(Comparator.comparing(a -> a.cross)).orElse(null);
        if (maxOutEdge != null && maxInEdge != null) {
            node.maxCross = Math.max(maxOutEdge.cross, maxInEdge.cross);
        } else if (maxInEdge != null) {
            node.maxCross = maxInEdge.cross;
        } else if (maxOutEdge != null) {
            node.maxCross = maxOutEdge.cross;
        } else {
            throw new Error("Isolated Node!");
        }
    }

    private void makeMove(Move mv) {
        ArrayList<Node> layer = layers.get(mv.node.layerID);
        int lbIndex;
        int ubIndex;
        if (mv.newPos > mv.node.pos) {
            lbIndex = mv.node.pos;
            ubIndex = mv.newPos;
        } else {
            lbIndex = mv.newPos;
            ubIndex = mv.node.pos;
        }

        for (int index = lbIndex; index < ubIndex; ++index) {
            Node node = layer.get(index);
            for (Edge e : node.outEdges) {
                int iO = node.pos;
                int iN = calcNewPos(mv.node.pos, mv.newPos, node.pos);
                int j = e.sink.pos;
                for (int indexP = index + 1; indexP <= ubIndex; ++indexP) {
                    Node nodeP = layer.get(indexP);
                    for (Edge eP : nodeP.outEdges) {
                        int kO = nodeP.pos;
                        int kN = calcNewPos(mv.node.pos, mv.newPos, nodeP.pos);
                        int l = eP.sink.pos;
                        if (j == l) continue;
                        int delta = (isCross(iN, j, kN, l) ? 1 : 0)
                                - (isCross(iO, j, kO, l) ? 1 : 0);
                        e.cross += delta;
                        eP.cross += delta;
                    }
                }
            }
        }

        for (int index = lbIndex; index < ubIndex; ++index) {
            Node node = layer.get(index);
            for (Edge e : node.inEdges) {
                int i = e.source.pos;
                int jO = node.pos;
                int jN = calcNewPos(mv.node.pos, mv.newPos, node.pos);
                for (int indexP = index + 1; indexP <= ubIndex; ++indexP) {
                    Node nodeP = layer.get(indexP);
                    for (Edge eP : nodeP.inEdges) {
                        int k = eP.source.pos;
                        if (k == i) continue;
                        int lO = nodeP.pos;
                        int lN = calcNewPos(mv.node.pos, mv.newPos, nodeP.pos);
                        int delta = (isCross(i, jN, k, lN) ? 1 : 0)
                                - (isCross(i, jO, k, lO) ? 1 : 0);
                        e.cross += delta;
                        eP.cross += delta;
                    }
                }
            }
        }

        //resort nodes in layer
        if (mv.newPos > mv.node.pos) {
            for (int i = mv.node.pos; i < mv.newPos; ++i) {
                layer.set(i, layer.get(i + 1));
                layer.get(i).pos -= 1;
            }
        } else {
            for (int i = mv.node.pos; i > mv.newPos; --i) {
                layer.set(i, layer.get(i - 1));
                layer.get(i).pos += 1;
            }
        }
        layer.set(mv.newPos, mv.node);
        mv.node.pos = mv.newPos;

        recalcNodeMaxCross(layer, lbIndex, ubIndex);
        allNodes.sort((a, b) -> -Integer.compare(a.maxCross, b.maxCross));
    }

    private void recalcNodeMaxCross(ArrayList<Node> layer, int lbIndex, int ubIndex) {
        var recalcedNode = new HashSet<Node>();
        for (int i = lbIndex; i <= ubIndex; ++i) {
            Node node = layer.get(i);
            calcNodeMaxCross(node);

            for (Edge e : node.outEdges) {
                Node nodeP = e.sink;
                if (recalcedNode.contains(nodeP)) continue;
                calcNodeMaxCross(nodeP);
                recalcedNode.add(nodeP);
            }

            for (Edge e : node.inEdges) {
                Node nodeP = e.source;
                if (recalcedNode.contains(nodeP)) continue;
                calcNodeMaxCross(nodeP);
                recalcedNode.add(nodeP);
            }
        }
    }

    private void checkSolution() {
        checkAllNodesOrder();
        int M = 0;
        for (ArrayList<Node> layer : layers) {
            for (Node node : layer) {
                for (Edge e : node.outEdges) {
                    int i = e.source.pos;
                    int j = e.sink.pos;
                    int currCross = 0;
                    for (Node nodeP : layer) {
                        if (nodeP == node) continue;
                        for (Edge eP : nodeP.outEdges) {
                            int k = eP.source.pos;
                            int l = eP.sink.pos;
                            if (j == l) continue;
                            currCross += isCross(i, j, k, l) ? 1 : 0;
                        }
                    }
                    if (currCross != e.cross) {
                        System.err.println("Edge: " + e + " currCross: " + currCross + ", e.cross: " + e.cross);
                        throw new Error("Cross error!");
                    }
                    if (e.cross > e.source.maxCross || e.cross > e.sink.maxCross) {
                        throw new Error("Cross consistency error!");
                    }

                    if (M < currCross) {
                        M = currCross;
                    }
                }
            }
        }
        if (allNodes.get(0).maxCross != M) {
            throw new Error("allNodes cross record error!");
        }
    }

    private void checkAllNodesOrder() {
        for (int i = 0; i < allNodes.size() - 1; ++i) {
            if (allNodes.get(i).maxCross < allNodes.get(i + 1).maxCross) {
                throw new Error("Nodes order error!");
            }
        }
    }

    private Move findMove() {
        int currM = allNodes.get(0).maxCross;
        Move bestMv = new Move(null, -1, Integer.MAX_VALUE);
        int bestCount = 0;
        for (Node n : allNodes) {
            if (bestMv.delta < 0) {
                break;
            }
            int lbIndex = Math.max(0, n.pos - moveMaxDistance);
            int ubIndex = Math.min(n.pos + moveMaxDistance, layers.get(n.layerID).size() - 1);

            Move move = tryMoveNeg(n, lbIndex, currM);
            if(move.delta < bestMv.delta){
                bestMv = move;
                bestCount = 1;
            }else if(move.delta == bestMv.delta && random.nextInt(++bestCount) == 0){
                bestMv = move;
            }
            move = tryMovePos(n, ubIndex, currM);
            if(move.delta < bestMv.delta){
                bestMv = move;
                bestCount = 1;
            }else if(move.delta == bestMv.delta && random.nextInt(++bestCount) == 0){
                bestMv = move;
            }

        }
        return bestMv;
    }

    private Move tryMoveNeg(Node node, int lbIndex, int currM){
        Move bestMv = new Move(null, -1, Integer.MAX_VALUE);
        int bestCount = 0;
        ArrayList<Node> layer = layers.get(node.layerID);
        resetTmpCross(layer, lbIndex, node.pos);

        int delta = 0;
        int nodeOldPos = node.pos;
        for(int newIndex = node.pos - 1; newIndex >= lbIndex; --newIndex){
            Node prevNode = layer.get(newIndex);

            delta += calcSwapDelta(prevNode, node, prevNode.pos, prevNode.pos + 1, nodeOldPos, prevNode.pos, currM);
            if(delta < bestMv.delta){
                bestMv = new Move(node, newIndex, delta);
                bestCount = 1;
            }else if(delta == bestMv.delta && random.nextInt(++bestCount) == 0){
                bestMv = new Move(node, newIndex, delta);
            }
            updateTmpCross(node);
            updateTmpCross(prevNode);
            nodeOldPos = newIndex;
        }

        return bestMv;
    }


    private Move tryMovePos(Node node, int ubIndex, int currM) {
        Move bestMv = new Move(null, -1, Integer.MAX_VALUE);
        int bestCount = 0;
        ArrayList<Node> layer = layers.get(node.layerID);
        resetTmpCross(layer, node.pos, ubIndex);

        int delta = 0;
        int nodeOldPos = node.pos;
        for (int newIndex = node.pos + 1; newIndex <= ubIndex; ++newIndex) {
            Node nextNode = layer.get(newIndex);

            delta += calcSwapDelta(node, nextNode, nodeOldPos, newIndex, nextNode.pos, nextNode.pos - 1, currM);
            if(delta < bestMv.delta){
                bestMv = new Move(node, newIndex, delta);
                bestCount = 1;
            }else if(delta == bestMv.delta && random.nextInt(++bestCount)==0){
                bestMv = new Move(node, newIndex, delta);
            }
            updateTmpCross(node);
            updateTmpCross(nextNode);
            nodeOldPos = newIndex;
        }
        return bestMv;
    }

    private void clearTmpDeltaCross(Node n){
        for(Edge e: n.inEdges){
            e.tmpDeltaCross = 0;
        }
        for(Edge e: n.outEdges){
            e.tmpDeltaCross = 0;
        }
    }

    private int calcSwapDelta(Node node, Node nextNode,
                              int nodeOldPos, int nodeNewPos,
                              int nextNodeOldPos, int nextNodeNewPos, int currM){
        clearTmpDeltaCross(node);
        clearTmpDeltaCross(nextNode);
        for (Edge nE : nextNode.outEdges) {
            int nextNodeToPos = nE.sink.pos;
            for (Edge E : node.outEdges) {
                int nodeToPos = E.sink.pos;
                if(nextNodeToPos == nodeToPos)continue;
//                int deltaCross = (isCross(nodeNewPos, nodeToPos, nextNodeNewPos, nextNodeToPos)?1:0)
//                        - (isCross(nodeOldPos, nodeToPos, nextNodeOldPos, nextNodeToPos)?1:0);
                int deltaCross = isCross(nodeOldPos, nodeToPos, nextNodeOldPos, nextNodeToPos)?-1:1;
                E.tmpDeltaCross += deltaCross;
                nE.tmpDeltaCross += deltaCross;
            }
        }
        for(Edge nE : nextNode.inEdges){
            int nextNodeFromPos = nE.source.pos;
            for (Edge E : node.inEdges){
                int nodeFromPos = E.source.pos;
                if(nextNodeFromPos == nodeFromPos)continue;
//                int deltaCross = (isCross(nodeFromPos, nodeNewPos, nextNodeFromPos, nextNodeNewPos)?1:0)
//                        - (isCross(nodeFromPos, nodeOldPos, nextNodeFromPos, nextNodeOldPos)?1:0);
                int deltaCross = isCross(nodeFromPos, nodeOldPos, nextNodeFromPos, nextNodeOldPos)?-1:1;
                E.tmpDeltaCross += deltaCross;
                nE.tmpDeltaCross += deltaCross;
            }
        }

        return countTmpDelta(node, nextNode, currM);
    }

    private void resetTmpCross(ArrayList<Node> layer, int lbIndex, int ubIndex){
        for(int index = lbIndex; index <= ubIndex; ++index){
            Node node = layer.get(index);
            for (Edge e: node.outEdges){
                e.tmpCross = e.cross;
            }
            for (Edge e:node.inEdges){
                e.tmpCross = e.cross;
            }
        }
    }

    private void updateTmpCross(Node n){
        for (Edge E : n.outEdges){
            E.tmpCross += E.tmpDeltaCross;
        }
        for (Edge E : n.inEdges){
            E.tmpCross += E.tmpDeltaCross;
        }
    }

    private int countTmpDelta(Node node1, Node node2, int currM){
        int delta = 0;
        for (Edge E : node1.outEdges){
            delta += clacDelta(E.tmpDeltaCross, E.tmpCross, currM);
        }
        for(Edge E : node1.inEdges){
            delta += clacDelta(E.tmpDeltaCross, E.tmpCross, currM);
        }
        for(Edge E : node2.outEdges){
            delta += clacDelta(E.tmpDeltaCross, E.tmpCross, currM);
        }
        for(Edge E : node2.inEdges){
            delta += clacDelta(E.tmpDeltaCross, E.tmpCross, currM);
        }
        return delta;
    }

    private int clacDelta(int deltaCross, int oriCross, int currM){
        int delta = 0;
        int newCross = oriCross + deltaCross;
        if(oriCross < currM && newCross == currM){
            delta += 1;
        }else if (oriCross <= currM && newCross > currM){
            delta += LAMBDA * (newCross - currM);
        }else if(oriCross == currM && newCross < currM){
            delta -= 1;
        }
        return delta;
    }

    private void randomShufflePerturb(float strength){
        for(ArrayList<Node> layer : layers){
            if(layer.size() == 1)continue;
            int startIndex = random.nextInt((int)((1.0f-strength) * layer.size()));
            int endIndex = startIndex + (int)(strength * layer.size())+1;

            Collections.shuffle(layer.subList(startIndex, endIndex), random);
            for(int i =startIndex; i<endIndex; ++i){
                layer.get(i).pos = i;
            }
        }
        initM();
    }

    private class MvDisManager {
        private int[] lastDis = new int[DISTANCE_MEMORY];
        private int pos = 0;

        void record(int dis) {
            lastDis[pos] = dis;
            ++pos;
            if (pos == lastDis.length) pos = 0;
        }

        int getMax() {
            int m = 0;
            for (int d : lastDis) {
                if (d > m) m = d;
            }
            return m;
        }
    }

    private class Node {
        int ID;
        int layerID;
        int pos;

        int maxCross = 0;

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
        public int hashCode() {
            return Objects.hash(ID);
        }

        @Override
        public boolean equals(Object o) {
            return o != null && o.getClass() == Node.class && ID == ((Node) o).ID;
        }
    }

    private class Edge {
        Node source;
        Node sink;

        int cross = 0;
        int tmpCross = 0;
        int tmpDeltaCross = 0;


        Edge(Node src, Node snk) {
            source = src;
            sink = snk;
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
        int delta;

        Move(Node n, int p, int d) {
            node = n;
            newPos = p;
            delta = d;
        }

        public String toString() {
            return "Move Node: " + node.ID + ", " + node.pos + "->" + newPos + "/" + delta;
        }
    }

    public class Solution {
        private int M;
        ArrayList<ArrayList<Integer>> sol;
        private String instance;
        private double timeToSol;
        private int iterations;

        public int getM(){
            return M;
        }

        public double getTimeToSol(){
            return timeToSol;
        }

        public int getIterations(){
            return iterations;
        }

        Solution(MMACSolver solver) {
            timeToSol = (System.currentTimeMillis() - startTime) / 1000.0;
            solver.checkSolution();
            instance = solver.instance;
            M = allNodes.get(0).maxCross;
            sol = new ArrayList<>(solver.layers.size());
            for (ArrayList<Node> layer : solver.layers) {
                ArrayList<Integer> nodeIndexes = new ArrayList<>(layer.size());
                for (Node n : layer) {
                    nodeIndexes.add(n.ID);
                }
                sol.add(nodeIndexes);
            }
            iterations = solver.iterationLS;
        }

        public void write(boolean isFinal) throws IOException {
            String outFile = "sol/" + (isFinal ? instance.substring(10) + ".sol": "sol" + M + ".txt");
            BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));

            bw.write(instance);
            bw.newLine();
            bw.write("Time: " + timeToSol + " seconds\n");
            bw.write("Iterations: " + iterations + "\n");
            bw.write("Objective: " + String.valueOf(M));
            bw.newLine();
            bw.write("Solution: \n");
            for (ArrayList<Integer> layer : sol) {
                for (Integer n : layer) {
                    bw.write(n + " ");
                }
                bw.newLine();
            }
            bw.close();
        }
    }
}

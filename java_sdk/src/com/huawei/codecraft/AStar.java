package com.huawei.codecraft;

import java.util.*;

/**
 * A Star Algorithm
 *from StartNode to EndNode,expand border nodes on the predicited-cost-lowest node
 * Init：障碍地图信息，起点终点，边界节点集，移动代价
 */
public class AStar {
    private static int DEFAULT_HV_COST = 10; // Horizontal - Vertical Cost
    private static int DEFAULT_DIAGONAL_COST = 14;
    private int hvCost=1;//水平或垂直移动的代价
    private int diagonalCost=10000;//对角移动的代价
    private Node[][] searchArea;//搜索区域, 由200*200个节点组成，有的节点isBlock为true表示障碍
    private PriorityQueue<Node> openList;//优先队列，用于存放待扩展的节点
    private Set<Node> closedSet;//存放已经扩展过的节点
    private Node initialNode;//起始节点
    private Node finalNode;//终止节点





    /**
     * 创建寻路器，读取地图信息和必要参数并赋值给寻路器实例（此寻路器只能上下左右寻路，不能对角线）
     * //TODO 我不希望寻路器创建时就给定起点终点，但希望传入一个有障碍信息的地图
     * @param blockMap:地图信息(例如int[200][200])，每一个元素（int[]）表示地图上的一行，0表示可通行，1表示障碍,
     */
    public AStar(int[][] blockMap) {
        int rows=blockMap.length;
        int cols=blockMap[0].length;

        this.searchArea = new Node[rows][cols];
        this.openList = new PriorityQueue<Node>(new Comparator<Node>() {
            @Override
            public int compare(Node nodev20, Node nodev21) {
                return Integer.compare(nodev20.getF(), nodev21.getF());
            }
        });
        setNodes();//初始化搜索区域
        setBlocks(blockMap);
        this.closedSet = new HashSet<>();
    }



    /**
     * //初始化搜索区域：为搜索区域的每个网格创建节点并计算到终点的启发值
     */
    private void setNodes() {
        for (int i = 0; i < searchArea.length; i++) {
            for (int j = 0; j < searchArea[0].length; j++) {
                Node node = new Node(i, j);
                this.searchArea[i][j] = node;
            }
        }
    }

//    /**
//     *为寻路器的搜索区域设置障碍物
//     * @param blocksArray ： 一个二维数组来存储障碍物的坐标点int[x,y]
//     */
//    public void setBlocks(int[][] blocksArray) {
//        for (int i = 0; i < blocksArray.length; i++) {
//            int row = blocksArray[i][0];
//            int col = blocksArray[i][1];
//
//            setBlock(row, col);
//        }
//    }

    /**
     * 为寻路器的搜索区域设置障碍物
     * @param blocksMap:每一个元素（int[]）表示地图上的一行，0表示可通行，1表示障碍
     */
    public void setBlocks(int[][] blocksMap) {
        for (int i = 0; i < blocksMap.length; i++) {
            for (int j = 0; j < blocksMap[0].length; j++) {
                if (blocksMap[i][j] == 1) {
                    setBlock(i, j);
                }
            }
        }
    }

    /**
     * use .addBlocks(new int[][] {{1,2},{1,3}},...) to add some blocks in searchArea
     * @param blocksArray:将障碍点的坐标作为int[]存放在数组中。
     */
    public void addBlocks(int[][] blocksArray) {
        for (int i = 0; i < blocksArray.length; i++) {
            int row = blocksArray[i][0];
            int col = blocksArray[i][1];
            setBlock(row, col);
        }
    }

    /**
     * use 寻路器.deleteBlocks(new int[][] {{1,2},{1,3}},...) to delete some blocks in searchArea
     * @param blocksArray:将障碍点的坐标作为int[]存放在数组中。
     */
    public void deleteBlocks(int[][] blocksArray) {
        for (int i = 0; i < blocksArray.length; i++) {
            int row = blocksArray[i][0];
            int col = blocksArray[i][1];
            this.searchArea[row][col].setBlock(false);
        }
    }

    /**
     * 寻路器的核心方法，返回从起点到终点的路径
     * @param initialRow,initialCol：起点的row坐标和col坐标(整数)
     * @param  finalRow,finalCol：终点的row坐标和col坐标
     * @return List<Node>：包括起点和终点的所有途径节点
     */
    public List<Node> findPathNode(int initialRow, int initialCol, int finalRow, int finalCol) {//这里用传入参数替换了实例变量
        Node initialNode = new Node(initialRow, initialCol);
        Node finalNode = new Node(finalRow, finalCol);
        setInitialNode(initialNode);
        setFinalNode(finalNode);

        openList.add(initialNode);
        while (!isEmpty(openList)) {
            Node currentNode = openList.poll();//从边界中取出预估代价最小的点来扩展
            closedSet.add(currentNode);
            if (isFinalNode(currentNode)) {
                return getPath(currentNode);
            } else {
                addAdjacentNodes(currentNode);//从当前节点拓展边界，并更新边界值
            }
        }
        //全图搜索完毕，没有找到路径
        this.openList.clear();
        this.closedSet.clear();
        clearAllNodes();
        return new ArrayList<Node>();
    }

    /**
     * 寻路器的常用方法，返回从起点到终点间路径上的移动指令
     * @param initialRow,initialCol：起点的row坐标和col坐标(整数)
     * @param  finalRow,finalCol：终点的row坐标和col坐标
     * @return LinkedList<Integer>：移动方向指令数组
     */
    public LinkedList<Integer> findPathCommands(int initialRow, int initialCol, int finalRow, int finalCol) {
        List<Node> path = findPathNode(initialRow, initialCol, finalRow, finalCol);
        return pathToDirection(path);
    }


    /**
     *把路径节点列表转换为移动方向指令数组
     * @param path:List<Node>：路径节点列表
     * @return LinkedList<Integer>：移动方向指令数组
     */
    public LinkedList<Integer> pathToDirection(List<Node> path) {
        LinkedList<Integer> result = new LinkedList<>();
        for (int i = 0; i < path.size() - 1; i++) {
            Node currentNode = path.get(i);
            Node nextNode = path.get(i + 1);
            int rowMove = nextNode.getRow() - currentNode.getRow();
            int colMove = nextNode.getCol() - currentNode.getCol();
            if (rowMove == 0 && colMove == 1) {
                result.add(0);//右移
            } else if (rowMove == 0 && colMove == -1) {
                result.add(1);//左移
            } else if (rowMove == 1 && colMove == 0) {
                result.add(3);//下移
            } else {
                result.add(2);//上移
            }
        }
        return result;
    }


    /**
     * 从终点节点开始，根据父节点逐步回溯到起点节点，返回路径节点列表
     * @param currentNode
     * @return
     */
    private List<Node> getPath(Node currentNode) {
        List<Node> path = new ArrayList<Node>();
        path.add(currentNode);
        Node parent;
        while ((parent = currentNode.getParent()) != null) {
            path.add(0, parent);
            currentNode = parent;
        }
        this.openList.clear();
        this.closedSet.clear();
        clearAllNodes();
        return path;
    }

    /**
     * 清除搜索区域内所有节点的父节点和成本信息
     */
    private void clearAllNodes() {
        for (Node[] nodes : searchArea) {
            for (Node node : nodes) {
                node.setParent(null);
                node.setF(0);
                node.setH(0);
                node.setG(0);
            }
        }
    }


    /**
     * 从当前节点拓展边界，并更新边界值。
     * @param currentNode
     */
    private void addAdjacentNodes(Node currentNode) {//拓展分为上中下三行来进行
        addAdjacentUpperRow(currentNode);
        addAdjacentMiddleRow(currentNode);
        addAdjacentLowerRow(currentNode);
    }

    /**
     * 处理当前节点下方一行的邻接节点。它检查下一行中与当前节点水平对齐的节点，
     * 以及左下和右下的对角节点（如果对角移动被允许）。
     * @param currentNode
     */
    private void addAdjacentLowerRow(Node currentNode) {
        int row = currentNode.getRow();
        int col = currentNode.getCol();
        int lowerRow = row + 1;
        if (lowerRow < getSearchArea().length) {
//            if (col - 1 >= 0) {
//                checkNode(currentNode, col - 1, lowerRow, getDiagonalCost()); // Comment this line if diagonal movements are not allowed
//            }
//            if (col + 1 < getSearchArea()[0].length) {
//                checkNode(currentNode, col + 1, lowerRow, getDiagonalCost()); // Comment this line if diagonal movements are not allowed
//            }
            checkNode(currentNode, col, lowerRow, getHvCost());
        }
    }

    /**
     * 处理当前节点中间一行的邻接节点。它检查当前行中与当前节点水平对齐的节点。
     * @param currentNode
     */
    private void addAdjacentMiddleRow(Node currentNode) {
        int row = currentNode.getRow();
        int col = currentNode.getCol();
        int middleRow = row;
        if (col - 1 >= 0) {
            checkNode(currentNode, col - 1, middleRow, getHvCost());
        }
        if (col + 1 < getSearchArea()[0].length) {
            checkNode(currentNode, col + 1, middleRow, getHvCost());
        }
    }

    /**
     * 处理当前节点上方一行的邻接节点。它检查上一行中与当前节点水平对齐的节点，以及左上和右上的对角节点（如果对角移动被允许）。
     * 这个方法确保考虑了所有可能从上方接近当前节点的路径。
     * @param currentNode
     */
    private void addAdjacentUpperRow(Node currentNode) {//
        int row = currentNode.getRow();
        int col = currentNode.getCol();
        int upperRow = row - 1;
        if (upperRow >= 0) { //处理当前节点可能存在的上一行
//            if (col - 1 >= 0) {//处理当前节点左上角的节点
//                checkNode(currentNode, col - 1, upperRow, getDiagonalCost()); // Comment this if diagonal movements are not allowed
//            }
//            if (col + 1 < getSearchArea()[0].length) {
//                checkNode(currentNode, col + 1, upperRow, getDiagonalCost()); // Comment this if diagonal movements are not allowed
//            }
            checkNode(currentNode, col, upperRow, getHvCost());
        }
    }

    /**
     *评估某节点的邻接节点，并据此更新其路径和成本信息。
     * @param currentNode：当前节点
     * @param col ：邻接节点的列坐标
     * @param row ：邻接节点的行坐标
     * @param cost：移动代价
     */
    private void checkNode(Node currentNode, int col, int row, int cost) {//
        Node adjacentNode = getSearchArea()[row][col];
        if (!adjacentNode.isBlock() && !getClosedSet().contains(adjacentNode)) {
            if (!getOpenList().contains(adjacentNode)) {//第一次加入边界
                adjacentNode.calculateHeuristic(getFinalNode());//TODO 这里设置了节点的启发值
                adjacentNode.setNodeData(currentNode, cost);//TODO 这里用到了先前默认计算好的节点的启发值
                getOpenList().add(adjacentNode);
            } else {//已经在边界中
                boolean changed = adjacentNode.checkBetterPath(currentNode, cost);
                if (changed) {
                    // Remove and Add the changed node, so that the PriorityQueue can sort again its
                    // contents with the modified "finalCost" value of the modified node
                    getOpenList().remove(adjacentNode);
                    getOpenList().add(adjacentNode);
                }
            }
        }
    }

    private boolean isFinalNode(Node currentNode) {
        return currentNode.equals(finalNode);
    }

    private boolean isEmpty(PriorityQueue<Node> openList) {
        return openList.size() == 0;
    }

    /**
     * 为寻路器的搜索区域设置一个障碍物，使用其行，列坐标
     * @param row
     * @param col
     */
    private void setBlock(int row, int col) {
        this.searchArea[row][col].setBlock(true);
    }

    public Node getInitialNode() {
        return initialNode;
    }

    public void setInitialNode(Node initialNode) {
        this.initialNode = initialNode;
    }

    public Node getFinalNode() {
        return finalNode;
    }

    public void setFinalNode(Node finalNode) {
        this.finalNode = finalNode;
    }

    public Node[][] getSearchArea() {
        return searchArea;
    }

    public void setSearchArea(Node[][] searchArea) {
        this.searchArea = searchArea;
    }

    public PriorityQueue<Node> getOpenList() {
        return openList;
    }

    public void setOpenList(PriorityQueue<Node> openList) {
        this.openList = openList;
    }

    public Set<Node> getClosedSet() {
        return closedSet;
    }

    public void setClosedSet(Set<Node> closedSet) {
        this.closedSet = closedSet;
    }

    public int getHvCost() {
        return hvCost;
    }

    public void setHvCost(int hvCost) {
        this.hvCost = hvCost;
    }

    private int getDiagonalCost() {
        return diagonalCost;
    }

    private void setDiagonalCost(int diagonalCost) {
        this.diagonalCost = diagonalCost;
    }
}

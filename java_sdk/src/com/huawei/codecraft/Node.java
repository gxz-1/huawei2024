package com.huawei.codecraft;

/**
 * Node Class：
 * 一个节点包括行列坐标，父节点，待填写的g,h,f值，以及是否是障碍物
 * init：行列坐标
 * get set方法：获取和设置节点的各个属性
 * calculateHeuristic：计算启发式代价
 * checkBetterPath：传入父节点和移动代价，检查本节点是否是更好的路径，若是则更新本节点的预估代价
 *
 *
 *
 */
public class Node {

    private int g;//从起点移动到指定方格的移动代价
    private int f;//f=g+h，总预估代价
    private int h;//从指定的方格移动到终点的预估代价
    private int row;//行坐标
    private int col;//列坐标
    private boolean isBlock;//是否是障碍物
    private Node parent;//父节点

    /**
     * 行列坐标初始化
     * @param row
     * @param col
     */
    public Node(int row, int col) {
        super();
        this.row = row;
        this.col = col;
    }

    /**
     * 计算本节点到目标节点的曼哈顿距离,赋值给启发式代价
     * @param finalNode
     */
    public void calculateHeuristic(Node finalNode) {
        this.h = Math.abs(finalNode.getRow() - getRow()) + Math.abs(finalNode.getCol() - getCol());
    }

    /**
     * setNodeData：基于父节点和移动代价，设置本节点的代价
     * @param currentNode
     * @param cost
     */
    public void setNodeData(Node currentNode, int cost) {
        int gCost = currentNode.getG() + cost;
        setParent(currentNode);
        setG(gCost);
        calculateFinalCost();
    }

    /**
     * checkBetterPath：传入父节点和移动代价，检查本节点是否是更好的路径，若是则更新本节点的预估代价
     *
     * @param currentNode
     * @param cost
     * @return
     */
    public boolean checkBetterPath(Node currentNode, int cost) {
        int gCost = currentNode.getG() + cost;
        if (gCost < getG()) {
            setNodeData(currentNode, cost);
            return true;
        }
        return false;
    }

    /**
     * 计算本节点的总预估代价并赋值
     */
    private void calculateFinalCost() {
        int finalCost = getG() + getH();
        setF(finalCost);
    }

    /**
     * 两节点是否相同取决于它们的行列坐标
     * @param arg0
     * @return
     */
    @Override
    public boolean equals(Object arg0) {
        Node other = (Node) arg0;
        return this.getRow() == other.getRow() && this.getCol() == other.getCol();
    }

    @Override
    public String toString() {
        return "Node [row=" + row + ", col=" + col + "]";
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public int getG() {
        return g;
    }

    public void setG(int g) {
        this.g = g;
    }

    public int getF() {
        return f;
    }

    public void setF(int f) {
        this.f = f;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public boolean isBlock() {
        return isBlock;
    }

    public void setBlock(boolean isBlock) {
        this.isBlock = isBlock;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }
}

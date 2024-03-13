package com.huawei.codecraft;

import java.util.*;

/**
 * 寻路器
 * init: grid(map), limitOfGrid, frontier block queue, closed block list
 * findPath: start block, end block
 * heuristic: Manhattan distance
 * isValid: check if the block is valid
 * findNeighbors: find the neighbors of the block
 * reconstructPath: reconstruct the path from the end block to the start block
 */
public class AStarPathSearch {
    private String[] grid;//地图，每一行是一个字符串
    private PriorityQueue<Node> openList;//待探测的边界方块，优先队列能通过node的cost自动排序，每次取出代价最低的方块
    private boolean[][] closedList;//为了避免重复探测，记录地图上已经探测过的方块

    private int limitxbeg,limitxend;//地图摘取实际有效部分，每一行用x表示，每一列用y表示
    private int limitybeg,limityend;

    public int maxIterations = 10000; // 最大迭代次数
    public int iterations = 0;

    public AStarPathSearch(String[] grid,int limitxbeg,int linitybeg,int limitxend,int limityend) {
        this.grid = grid;
        this.limitxbeg=limitxbeg;//限制输入地图的大小[limitx,limity]
        this.limitybeg=linitybeg;
        this.limitxend=limitxend;
        this.limityend=limityend;

        this.openList = new PriorityQueue<>();
        this.closedList = new boolean[this.limitxend-this.limitxbeg+1][this.limityend-this.limitybeg+1];
        //例如 1~200, 共有200-1+1=200个元素
    }

    /**
     * use .findPath(start_point,destination) to get robot's commands sequence on the way to destination
     * @return linkedlist<Integer>  of the path
     */
    public LinkedList<Integer> findPath(int startX, int startY, int endX, int endY) {
        LinkedList<Integer> result = null;

        // 重置openList和closedList
        openList.clear();
        for (boolean[] row : closedList) {
            Arrays.fill(row, false);
        }

        Node startNode = new Node(startX, startY, null, 0, heuristic(startX, startY,endX,endY));
        openList.add(startNode);//待探测列表加入起点

        System.out.println("开始寻路：从 (" + startX + ", " + startY + ") 到 (" + endX + ", " + endY + ")");

        while (!openList.isEmpty()) {//还存在待探测节点
//            if (iterations++ > maxIterations) { // 防止无限循环，但debug时要注释掉
////                System.out.println("超过最大迭代次数，终止搜索。");
//                return new LinkedList<>(); // 未找到路径
//            }
            Node current = openList.poll();//取出代价最低的节点，作为当前考虑的节点
//            System.out.println("当前节点: (" + current.x + ", " + current.y + "), f=" + (current.g + current.h));
            if (current.x == endX && current.y == endY) {//这个方块是终点块，说明已找到前往终点块的最优路径
//                System.out.println("找到路径");
                result = reconstructPath(current);//从终点块按照父节点逆向重构路径
                break;
            }

            closedList[current.x][current.y] = true;//当前节点已是最优，但尚未到达终点。

            for (Node neighbor : findNeighbors(current)) {//需要取出可探测邻居节点继续探测：计算实际开销+曼哈顿距离开销
//                System.out.println("    邻居节点: (" + neighbor.x + ", " + neighbor.y + ")");

                int tentativeG = current.g + 1; // 假设从当前节点到邻居的成本为1，则邻居节点的前段开销=当前节点的前段开销+1



                if (!openList.contains(neighbor)) {//如果邻居节点尚不属于边界，计算其开销并加入边界
                    neighbor.h = heuristic(neighbor.x, neighbor.y,endX,endY); // 更新曼哈顿距离
                    neighbor.g = tentativeG;
                    neighbor.f = neighbor.g + neighbor.h;
                    neighbor.parent = current;
                    openList.add(neighbor);

                } else if (tentativeG < neighbor.g) {//如果邻居节点已经在边界中，但新路径前段代价更低，则重新计算其开销，并加入边界
                    neighbor.parent = current;
                    neighbor.g = tentativeG;
                    neighbor.f = neighbor.g + neighbor.h; // 这里的h已经在第一次添加时被计算出来了

                    openList.remove(neighbor); // 必须先移除
                    openList.add(neighbor); // 然后重新添加以更新其在队列中的位置
                }

            }

        }
        if (result == null) {
            result = new LinkedList<>();// 未找到路径
        }

        return result;
    }

    private int heuristic(int x, int y,int endX,int endY) {
        return Math.abs(x - endX) + Math.abs(y - endY); // 曼哈顿距离
    }

    /**
     * 判断方块是否是可探测节点
     * @param x
     * @param y
     * @return
     */
    private boolean isValid(int x, int y) {
//        System.out.println("x = " + x);
        return x >= limitxbeg && x <= limitxend && y >= limitybeg && y < limityend && (grid[x].charAt(y) == '.' || grid[x].charAt(y) == 'B')&& !closedList[x][y];
    }

    /**
     *
     * @param current：当前节点
     * @return list：尚未被close邻居节点列表，即可探测的节点
     */
    private List<Node> findNeighbors(Node current) {
        List<Node> neighbors = new ArrayList<>();
        int[] dx = {0, 1, 0, -1};
        int[] dy = {1, 0, -1, 0}; // 四个基本方向

        for (int i = 0; i < 4; i++) {
            int nx = current.x + dx[i], ny = current.y + dy[i];
            if (isValid(nx, ny)) {//如果邻居节点尚未被close
                neighbors.add(new Node(nx, ny, current, 0, 0));
            }
        }

        return neighbors;
    }



//    private List<Node> reconstructPath(Node node) {
//        List<Node> path = new ArrayList<>();
//        while (node != null) {
//            path.add(node);
//            node = node.parent;
//        }
//        Collections.reverse(path);
//        return path;
//    }

    private LinkedList<Integer> reconstructPath(Node node) {
        LinkedList<Integer> path = new LinkedList<>();
        while (node != null && node.parent != null) {
            if(node.x==node.parent.x){ //!
                if((node.parent.y-node.y)==1){
                    path.push(1);//右移
                }else if((node.parent.y-node.y)==-1){
                    path.push(0);//左移
                }
            }else if(node.y==node.parent.y){
                if((node.parent.x-node.x)==1){
                    path.push(2);//上移
                }else if((node.parent.x-node.x)==-1){
                    path.push(3);//下移
                }
            }
            node = node.parent;
        }
        return path;
    }
}

package com.huawei.codecraft;

import java.util.*;


public class AStarPathSearch {
    private String[] grid;
    private int endX, endY;
    private PriorityQueue<Node> openList;
    private boolean[][] closedList;

    private int limitxbeg,limitxend;
    private int limitybeg,limityend;
    public AStarPathSearch(String[] grid,int limitxbeg,int linitybeg,int limitxend,int limityend) {
        this.grid = grid;
        this.limitxbeg=limitxbeg;
        this.limitybeg=linitybeg;
        this.limitxend=limitxend;
        this.limityend=limityend;
        //限制输入地图的大小[limitx,limity]
        this.openList = new PriorityQueue<>();
        this.closedList = new boolean[grid.length][grid[1].length()];
    }

    private int heuristic(int x, int y) {
        return Math.abs(x - endX) + Math.abs(y - endY); // 曼哈顿距离
    }

    private boolean isValid(int x, int y) {
//        System.out.println("x = " + x);
        return x >= limitxbeg && x <= limitxend && y >= limitybeg && y < limityend && (grid[x].charAt(y) == '.' || grid[x].charAt(y) == 'B');
    }

    private List<Node> findNeighbors(Node current) {
        List<Node> neighbors = new ArrayList<>();
        int[] dx = {0, 1, 0, -1};
        int[] dy = {1, 0, -1, 0}; // 四个基本方向

        for (int i = 0; i < 4; i++) {
            int nx = current.x + dx[i], ny = current.y + dy[i];
            if (isValid(nx, ny)) {
                neighbors.add(new Node(nx, ny, current, 0, 0));
            }
        }

        return neighbors;
    }

    public LinkedList<Integer> findPath(int startX, int startY, int endX, int endY) {
        this.endX = endX;
        this.endY = endY;
        Node startNode = new Node(startX, startY, null, 0, heuristic(startX, startY));
        openList.add(startNode);

        while (!openList.isEmpty()) {
            Node current = openList.poll();
            if (current.x == endX && current.y == endY) {
                return reconstructPath(current);//!
            }

            closedList[current.x][current.y] = true;

            for (Node neighbor : findNeighbors(current)) {
                if (closedList[neighbor.x][neighbor.y]) continue; // 忽略已经处理过的节点

                int tentativeG = current.g + 1; // 假设从当前节点到邻居的成本为1

                boolean isBetterPath = false;

                if (!openList.contains(neighbor)) {
                    neighbor.h = heuristic(neighbor.x, neighbor.y); // 更新启发式值
                    isBetterPath = true;
                } else if (tentativeG < neighbor.g) {
                    isBetterPath = true; // 发现了一条更好的路径
                }

                if (isBetterPath) {
                    neighbor.parent = current;
                    neighbor.g = tentativeG;
                    neighbor.f = neighbor.g + neighbor.h; // 通常f = g + h
                    if (openList.contains(neighbor)) {
                        openList.remove(neighbor); // 必须先移除
                    }
                    openList.add(neighbor); // 然后重新添加以更新其在队列中的位置
                }
            }

        }

        return new LinkedList<>(); // 未找到路径
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

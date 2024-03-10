package com.huawei.codecraft.pathsearch;

import java.util.*;


public class AStarPathSearch {
    private String[] grid;
    private int endX, endY;
    private PriorityQueue<Node> openList;
    private boolean[][] closedList;

    public AStarPathSearch(String[] grid) {
        this.grid = grid;
        this.openList = new PriorityQueue<>();
        this.closedList = new boolean[grid.length][grid[0].length()];
    }

    private int heuristic(int x, int y) {
        return Math.abs(x - endX) + Math.abs(y - endY); // 曼哈顿距离
    }

    private boolean isValid(int x, int y) {
        return x >= 0 && x < grid.length && y >= 0 && y < grid[0].length() && grid[x].charAt(y) == '.';
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
                return reconstructPath(current);
            }

            closedList[current.x][current.y] = true;

            for (Node neighbor : findNeighbors(current)) {
                if (closedList[neighbor.x][neighbor.y]) continue;
                neighbor.g = current.g + 1; // 假设每步的成本为1
                neighbor.h = heuristic(neighbor.x, neighbor.y);
                neighbor.parent = current;

                if (!openList.contains(neighbor)) {
                    openList.add(neighbor);
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
        while (node != null) {
            if(node.x==node.parent.x){
                if((node.parent.y-node.y)==1){
                    path.push(0);//右移
                }else if((node.parent.y-node.y)==-1){
                    path.push(1);//左移
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

package com.huawei.codecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;


public class JpsPathSearch {

    private static final int[] dx = {-1, 1, 0, 0, -1, -1, 1, 1};
    private static final int[] dy = {0, 0, -1, 1, -1, 1, -1, 1};

    private String[] grid;
    private int endX, endY;
    private PriorityQueue<Node> openList;
    private Map<Integer, Node> closedList;

    public JpsPathSearch(String[] grid) {
        this.grid = grid;
        this.openList = new PriorityQueue<>();
        this.closedList = new HashMap<>();
    }

    private int heuristic(int x, int y) {
        // 使用曼哈顿距离作为启发式函数
        return Math.abs(x - endX) + Math.abs(y - endY);
    }

    private boolean isValid(int x, int y) {
        return x >= 0 && x < grid.length && y >= 0 && y < grid[0].length() && (grid[x].charAt(y) == '.');
    }

    private List<Node> findNeighbors(Node current) {
        List<Node> neighbors = new ArrayList<>();
        for (int i = 0; i < 8; i++) { // 包括对角线方向
            int nx = current.x + dx[i];
            int ny = current.y + dy[i];
            if (isValid(nx, ny)) {
                neighbors.add(new Node(nx, ny, current, 0, heuristic(nx, ny)));
            }
        }
        return neighbors;
    }
    private Node jump(int x, int y, int px, int py) {
        if (!isValid(x, y)) return null;
        if (x == endX && y == endY) return new Node(x, y, null, 0, 0);

        int dx = x - px, dy = y - py;
        // 检查强迫邻居
        if (dx != 0 && dy != 0) {
            if ((isValid(x - dx, y + dy) && !isValid(x - dx, y)) ||
                    (isValid(x + dx, y - dy) && !isValid(x, y - dy))) {
                return new Node(x, y, null, 0, 0);
            }
        } else {
            if (dx != 0) {
                if ((isValid(x + dx, y + 1) && !isValid(x, y + 1)) ||
                        (isValid(x + dx, y - 1) && !isValid(x, y - 1))) {
                    return new Node(x, y, null, 0, 0);
                }
            } else if (dy != 0) {
                if ((isValid(x + 1, y + dy) && !isValid(x + 1, y)) ||
                        (isValid(x - 1, y + dy) && !isValid(x - 1, y))) {
                    return new Node(x, y, null, 0, 0);
                }
            }
        }

        // 在对角方向上递归跳跃
        if (dx != 0 && dy != 0) {
            if (jump(x + dx, y, x, y) != null || jump(x, y + dy, x, y) != null) {
                return new Node(x, y, null, 0, 0);
            }
        }

        // 沿当前方向继续跳跃
        if (isValid(x + dx, y) || isValid(x, y + dy)) {
            return jump(x + dx, y + dy, x, y);
        }

        return null;
    }

    private List<Node> reconstructPath(Node node) {
        List<Node> path = new ArrayList<>();
        while (node != null) {
            path.add(node);
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }

    public List<Node> findPath(int startX, int startY, int endX, int endY) {
        this.endX = endX;
        this.endY = endY;
        Node startNode = new Node(startX, startY, null, 0, heuristic(startX, startY));
        openList.add(startNode);

        while (!openList.isEmpty()) {
            Node current = openList.poll();
            if (current.x == endX && current.y == endY) {
                return reconstructPath(current);
            }
            closedList.put(current.x * grid[0].length() + current.y, current);

            for (int i = 0; i < 8; i++) {
                int nx = current.x + dx[i];
                int ny = current.y + dy[i];
                Node jumpNode = jump(nx, ny, current.x, current.y);
                if (jumpNode != null && !closedList.containsKey(jumpNode.x * grid[0].length() + jumpNode.y)) {
                    jumpNode.g = current.g + Math.abs(jumpNode.x - current.x) + Math.abs(jumpNode.y - current.y);
                    jumpNode.h = heuristic(jumpNode.x, jumpNode.y);
                    jumpNode.parent = current;
                    openList.add(jumpNode);
                }
            }
        }
        return new ArrayList<>(); // 未找到路径
    }

}

package com.huawei.codecraft;

import com.huawei.codecraft.Nodev1;

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
    private PriorityQueue<Nodev1> openList;
    private Map<Integer, Nodev1> closedList;

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
        return x >= 1 && x <= 200 && y >= 1 && y <= 200 && (grid[x].charAt(y) == '.' || grid[x].charAt(y) == 'B');
    }

    private List<Nodev1> findNeighbors(Nodev1 current) {
        List<Nodev1> neighbors = new ArrayList<>();
        for (int i = 0; i < 8; i++) { // 包括对角线方向
            int nx = current.x + dx[i];
            int ny = current.y + dy[i];
            if (isValid(nx, ny)) {
                neighbors.add(new Nodev1(nx, ny, current, 0, heuristic(nx, ny)));
            }
        }
        return neighbors;
    }
    private Nodev1 jump(int x, int y, int px, int py) {
        if (!isValid(x, y)) return null;
        if (x == endX && y == endY) return new Nodev1(x, y, null, 0, 0);

        int dx = x - px, dy = y - py;
        // 检查强迫邻居
        if (dx != 0 && dy != 0) {
            if ((isValid(x - dx, y + dy) && !isValid(x - dx, y)) ||
                    (isValid(x + dx, y - dy) && !isValid(x, y - dy))) {
                return new Nodev1(x, y, null, 0, 0);
            }
        } else {
            if (dx != 0) {
                if ((isValid(x + dx, y + 1) && !isValid(x, y + 1)) ||
                        (isValid(x + dx, y - 1) && !isValid(x, y - 1))) {
                    return new Nodev1(x, y, null, 0, 0);
                }
            } else if (dy != 0) {
                if ((isValid(x + 1, y + dy) && !isValid(x + 1, y)) ||
                        (isValid(x - 1, y + dy) && !isValid(x - 1, y))) {
                    return new Nodev1(x, y, null, 0, 0);
                }
            }
        }

        // 在对角方向上递归跳跃
        if (dx != 0 && dy != 0) {
            if (jump(x + dx, y, x, y) != null || jump(x, y + dy, x, y) != null) {
                return new Nodev1(x, y, null, 0, 0);
            }
        }

        // 沿当前方向继续跳跃
        if (isValid(x + dx, y) || isValid(x, y + dy)) {
            return jump(x + dx, y + dy, x, y);
        }

        return null;
    }

    private List<Nodev1> reconstructPath(Nodev1 nodev1) {
        List<Nodev1> path = new ArrayList<>();
        while (nodev1 != null) {
            path.add(nodev1);
            nodev1 = nodev1.parent;
        }
        Collections.reverse(path);
        return path;
    }

    public int findPathGValue(int startX, int startY, int endX, int endY) {
        this.endX = endX;
        this.endY = endY;
        Nodev1 startNodev1 = new Nodev1(startX, startY, null, 0, heuristic(startX, startY));
        openList.add(startNodev1);

        while (!openList.isEmpty()) {
            Nodev1 current = openList.poll();
            if (current.x == endX && current.y == endY) {
                // 找到终点，返回终点的 g 值
                return current.g;
            }
            closedList.put(current.x * grid[1].length() + current.y, current);

            for (int i = 0; i < 8; i++) {
                int nx = current.x + dx[i];
                int ny = current.y + dy[i];
                Nodev1 jumpNodev1 = jump(nx, ny, current.x, current.y);
                if (jumpNodev1 != null && !closedList.containsKey(jumpNodev1.x * grid[1].length() + jumpNodev1.y)) {
                    jumpNodev1.g = current.g + Math.abs(jumpNodev1.x - current.x) + Math.abs(jumpNodev1.y - current.y);
                    jumpNodev1.h = heuristic(jumpNodev1.x, jumpNodev1.y);
                    jumpNodev1.parent = current;
                    openList.add(jumpNodev1);
                }
            }
        }
        return 0; // 未找到路径时返回一个特殊值，比如 0
    }

    public List<Nodev1> findPath(int startX, int startY, int endX, int endY) {
        this.endX = endX;
        this.endY = endY;
        Nodev1 startNodev1 = new Nodev1(startX, startY, null, 0, heuristic(startX, startY));
        openList.add(startNodev1);

        while (!openList.isEmpty()) {
            Nodev1 current = openList.poll();
            if (current.x == endX && current.y == endY) {
                return reconstructPath(current);
            }
            closedList.put(current.x * grid[1].length() + current.y, current);

            for (int i = 0; i < 8; i++) {
                int nx = current.x + dx[i];
                int ny = current.y + dy[i];
                Nodev1 jumpNodev1 = jump(nx, ny, current.x, current.y);
                if (jumpNodev1 != null && !closedList.containsKey(jumpNodev1.x * grid[1].length() + jumpNodev1.y)) {
                    jumpNodev1.g = current.g + Math.abs(jumpNodev1.x - current.x) + Math.abs(jumpNodev1.y - current.y);
                    jumpNodev1.h = heuristic(jumpNodev1.x, jumpNodev1.y);
                    jumpNodev1.parent = current;
                    openList.add(jumpNodev1);
                }
            }
        }
        return new ArrayList<>(); // 未找到路径
    }

}

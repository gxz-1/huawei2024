package com.huawei.codecraft;

import java.util.*;

public class BFSPathSearch {

    private static final int[] dx = {0, 0, 1, -1};
    private static final int[] dy = {1, -1, 0, 0};

    public static Point findMaxValueTarget(String[] ch, int[][][] gds, int begx, int begy,int now_zhen) {
        boolean[][] visited = new boolean[201][201]; // 记录已访问的节点
        Queue<Point> queue = new LinkedList<>();
        List<Point> foundTargets = new ArrayList<>();

        queue.offer(new Point(begx, begy, 0)); // 初始点的gds值设为0
        visited[begx][begy] = true;

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            int x = current.x;
            int y = current.y;

            // 检查当前点是否是目标点
            if (gds[x][y][0] > 0 && gds[x][y][1]+1000>now_zhen) {
                current.gdsValue = gds[x][y][0]; // 记录gds值
                foundTargets.add(current);
                if (foundTargets.size() == 10) {
                    break;
                }
            }

            // 遍历所有可移动的邻居节点
            for (int i = 0; i < 4; i++) {
                int nx = x + dx[i];
                int ny = y + dy[i];

                if (nx >= 1 && nx < 201 && ny >= 1 && ny < 201 && !visited[nx][ny] && (ch[nx].charAt(ny) == 'B' || ch[nx].charAt(ny) == '.')) {
                    queue.offer(new Point(nx, ny, 0)); // 新点的gds值暂时设为0
                    visited[nx][ny] = true;
                }
            }
        }

        // 在找到的目标点中查找gds值最大的那个
        Optional<Point> maxPoint = foundTargets.stream().max(Comparator.comparingInt(p -> p.gdsValue));

        return maxPoint.orElse(null); // 如果没有找到目标点，则返回null
    }

    public static class Point {
        int x, y;
        int gdsValue; // 目标点的gds值

        Point(int x, int y, int gdsValue) {
            this.x = x;
            this.y = y;
            this.gdsValue = gdsValue;
        }
    }

}
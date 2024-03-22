package com.huawei.codecraft;

public class Segment {
    public static void main(String[] args) {
        // 假设有10个关键点的坐标，每个点是一个长度为2的数组，格式为[x, y]
        int[][] points = {
                {20, 30}, {50, 150}, {170, 180}, {160, 40}, {100, 90},
                {80, 10}, {120, 130}, {30, 170}, {190, 60}, {70, 200}
        };

        // 创建分块图数组
        int[][] grid = new int[200][200];

        // 初始化分块图
        for (int i = 0; i < 200; i++) {
            for (int j = 0; j < 200; j++) {
                grid[i][j] = findClosestPointIndex(i, j, points);
            }
        }

        // 这里可以对grid进行进一步处理或输出
        // 示例输出前10行的前10列
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }
    }

    // 计算并返回给定网格点最近的关键点的索引
    private static int findClosestPointIndex(int x, int y, int[][] points) {
        int closestPointIndex = -1;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < points.length; i++) {
            double distance = Math.sqrt(Math.pow(x - points[i][0], 2) + Math.pow(y - points[i][1], 2));
            if (distance < minDistance) {
                minDistance = distance;
                closestPointIndex = i;
            }
        }

        return closestPointIndex;
    }
}

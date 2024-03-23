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
                if (foundTargets.size() == 4) {
                    break;
                }
            }

            // 遍历所有可移动的邻居节点
            for (int i = 0; i < 4; i++) {
                int nx = x + dx[i];
                int ny = y + dy[i];

                System.out.println("ny = " + ny+" ny = " + ny);
                if (nx >= 1 && nx < 201 && ny >= 1 && ny < 200 && !visited[nx][ny] && (ch[nx].charAt(ny) == 'B' || ch[nx].charAt(ny) == '.')) {
                    queue.offer(new Point(nx, ny, 0)); // 新点的gds值暂时设为0
                    visited[nx][ny] = true;
                }
            }
        }

        // 在找到的目标点中查找gds值最大的那个
        Optional<Point> maxPoint = foundTargets.stream().max(Comparator.comparingInt(p -> p.gdsValue));

        return maxPoint.orElse(null); // 如果没有找到目标点，则返回null
    }

    public static Point findMinBerth(String[] ch, int begx, int begy) {
        boolean[][] visited = new boolean[201][201]; // 记录已访问的节点
        Queue<Point> queue = new LinkedList<>();

        queue.offer(new Point(begx, begy, 0)); // 初始点的gds值设为0
        visited[begx][begy] = true;

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            int x = current.x;
            int y = current.y;

            // 检查当前点是否是目标点
            if (ch[x].charAt(y) == 'B') {
                return new Point(x,y,0);
            }

            // 遍历所有可移动的邻居节点
            for (int i = 0; i < 4; i++) {
                int nx = x + dx[i];
                int ny = y + dy[i];

                if (nx >= 1 && nx < 201 && ny >= 1 && ny < 200 && !visited[nx][ny] && (ch[nx].charAt(ny) == 'B' || ch[nx].charAt(ny) == '.')) {
                    queue.offer(new Point(nx, ny, 0)); // 新点的gds值暂时设为0
                    visited[nx][ny] = true;
                }
            }
        }
        return null; // 如果没有找到泊位，则返回null
    }

    /**
     * 修改传入的ch2berth数组
     * @param ch
     * @param ch2berth
     * @param berth
     */
    public static void createch2berth(String[] ch, berthInfo[][] ch2berth, Main.Berth[] berth) {
        // 使用嵌套循环为每个元素分配实例
        for (int i = 1; i < 201; i++) {
            for (int j = 0; j < 201; j++) {
                ch2berth[i][j] = new berthInfo(); // 根据实际情况传递参数
            }
        }
        //遍历10个泊位
        for(int i=0;i<10;++i){
            //BFS
            boolean[][] visited = new boolean[201][201];
            Queue<Point> queue = new LinkedList<>();
            queue.offer(new Point(berth[i].x+1, berth[i].y, 0));
            visited[berth[i].x+1][berth[i].y] = true;
            int distance=1;
            while (!queue.isEmpty()) {
                for (int j=0;j<queue.size();++j){
                    Point current = queue.poll();
                    int x = current.x;
                    int y = current.y;
                    // 检查当前点是否可以更新标记
                    if (ch[x].charAt(y) == '.'){
                        if(ch2berth[x][y].distance==-1 || distance<ch2berth[x][y].distance){//更新
                            ch2berth[x][y].distance=distance;
                            ch2berth[x][y].berth_id=berth[i].berth_id;
                        }
//                        else {//剪枝
//                            continue;
//                        }
                    }

                    // 遍历所有可移动的邻居节点
                    for (int k = 0; k < 4; k++) {
                        int nx = x + dx[k];
                        int ny = y + dy[k];

                        if (nx >= 1 && nx < 201 && ny >= 1 && ny < 200 && !visited[nx][ny] && (ch[nx].charAt(ny) == 'B' || ch[nx].charAt(ny) == '.')) {
                            queue.offer(new Point(nx, ny, 0)); // 新点的gds值暂时设为0
                            visited[nx][ny] = true;
                        }
                    }
                }
                distance++;
            }
        }
    }


    public static class Point {
        int x, y;
        int gdsValue; // 目标点的gds值

        Point(int x, int y, int gdsValue) {
            this.x = x;
            this.y = y;
            this.gdsValue = gdsValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true; // 检查是否为同一对象引用
            if (o == null || getClass() != o.getClass()) return false; // 检查是否为同一类型
            Point point = (Point) o; // 类型转换
            return x == point.x && y == point.y; // 根据x和y的值判断两个Point对象是否相等
        }

        @Override
        public int hashCode() {
            return 31 * x + y; // 生成基于x和y值的哈希码
        }
    }

    public static class berthInfo{
        int berth_id;
        int distance;
        berthInfo(){
            berth_id=-1;
            distance=-1;
        }
    }


}
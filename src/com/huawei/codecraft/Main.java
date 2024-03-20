/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.huawei.codecraft;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
/**
 * Main
 *
 * @since 2024-02-05
 */
public class Main {
    // 200行的地图数据
    private static final int n = 200;
    // 机器人个数
    private static final int robot_num = 10;
    // 泊位个数
    private static final int berth_num = 10;
    //用来初始化地图ch和gds变量的大小的吧，可能是为了方便后面加大地图用的
    private static final int N = 200;

    // 钱、初始化时最后一行的船的容量大小、帧数
    private int money, boat_capacity, id;
    // 应该只是用来吃掉地图
    private String[] ch = new String[N+1];
    // 这个没用上，估计是用来存储地图上的商品信息
    private int[][] gds = new int[N][N];

    // 机器人列表
    private Robot[] robots = new Robot[robot_num];
    // 泊位列表
    private Berth[] berths = new Berth[berth_num];
    // 船列表
    private Boat[] boats = new Boat[10];

    // 这里开始是自己给的属性-------------------------------------------------------------------------
    // 存地图，为了后面寻路
    private char[][] map = new char[N][N];
    // 存一个商品列表
    private Comparator<Good> valueComparator = (s1, s2) -> Integer.compare(s2.value, s1.value);
    private PriorityQueue<Good> goods = new PriorityQueue<>(valueComparator); // 创建列表存储新货物

    // 存储每个陆地坐标到最近泊位的最短距离
    private int[][] landToBerthDist;

    private int totalTrueValue;

    // 空闲船舶队列
    Queue<Integer> freeBoats = new PriorityQueue<>((a, b) -> (boats[a].capacity - boats[a].currentUse) - (boats[b].capacity - boats[b].currentUse));
    // 有货物泊位队列
    Queue<Integer> loadedBerths = new PriorityQueue<>((a, b) -> berths[b].goodNum - berths[a].goodNum);
    // ----------------------------------------这里开始是自己给的方法-----------------------------------------------------------------------
    // 遍历列表，清除已经没有时间的商品
    private void updateGoods() {
        Iterator<Good> iterator = goods.iterator();
        while (iterator.hasNext()) {
            Good good = iterator.next();
            good.decreaseLifeTime();
            if (!good.isStillAlive()) {
                iterator.remove();
            }
        }
    }

    // claude直接生成的，不知道对不对，需要测试

    private int[][] constructPath(Point dest) {
        int length = 0;
        Point curr = dest;
        while (curr != null) {
            length++;
            curr = curr.parent;
        }

        int[][] path = new int[length][2];
        int index = length - 1;
        curr = dest;
        while (curr != null) {
            path[index--] = new int[]{curr.x, curr.y};
            curr = curr.parent;
        }

        return path;
    }
    private int[][] getShortestPath(int sx, int sy, int tx, int ty) {
        int[][] DIRECTIONS = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        boolean[][] visited = new boolean[n][n];
        Queue<Point> queue = new LinkedList<>();
        Point robot = new Point(sx, sy, null);
        Point good = new Point(tx, ty, null);

        queue.offer(robot);
        visited[sx][sy] = true;

        while (!queue.isEmpty()) {
            Point curr = queue.poll();

            if (curr.x == good.x && curr.y == good.y) {
                // 找到目标点,返回路径
                return constructPath(curr);
            }

            // 遍历相邻四个方向
            for (int[] dir : DIRECTIONS) {
                int newX = curr.x + dir[0];
                int newY = curr.y + dir[1];

                // 越界或遇到障碍物,跳过
                if (newX < 0 || newX >= n || newY < 0 || newY >= n || map[newX][newY] == '*' || map[newX][newY] == '#') {
                    continue;
                }

                // 未访问过,加入队列
                if (!visited[newX][newY]) {
                    visited[newX][newY] = true;
                    queue.offer(new Point(newX, newY, curr));
                }
            }
        }

        // 无法到达目标点
        return null;
    }

    // 在与其他机器人路线不冲突的情况下生成最短路 BFS
    private int[][] getShrotestPathWithoutConflict(int sx,int sy,int tx,int ty){
        int[][] DIRECTIONS = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        int[][] RANGES = {{-1, -1}, {-1, 0}, {-1, 1},{0, -1},{0,0},{0,1},{1,-1},{1,0},{1,1}};
        boolean[][] stopBots = new boolean[n][n];
        boolean[][] curMap = new boolean[n][n];

        // 路径避开当前停止的机器人位置
        for(Robot robot:robots){
            int rx = robot.x;
            int ry = robot.y;
            stopBots[rx][ry] = true;
        }
//        stopBots[sx][sy] = false;

        // BFS
        Queue<Point> queue = new LinkedList<>();
        Point robot = new Point(sx, sy, null);
        Point good = new Point(tx, ty, null);
        queue.offer(robot);

        //相对时间
        int time = 0;
        while (!queue.isEmpty()) {
            //当前时间的状态数量
            int size = queue.size();

            if(time > n*n)
                break;

            // 当前时间其他Bot位置
            for(Robot robot1:robots){
                if(robot1.path != null && robot1.path.length>0){
                    if(robot1.path.length > time)
                        curMap[robot1.path[time][0]][robot1.path[time][1]] = true;
                    if(robot1.path.length > time+1)
                        curMap[robot1.path[time+1][0]][robot1.path[time+1][1]] = true;
                    if(robot1.path.length > time+2)
                        curMap[robot1.path[time+2][0]][robot1.path[time+2][1]] = true;
                }
            }

            while(size > 0) {
                size -= 1;
                Point curr = queue.poll();

                if (curr.x == good.x && curr.y == good.y) {
                    // 找到目标点,返回路径
                    return constructPath(curr);
                }

                // 遍历相邻四个方向
                for (int[] dir : DIRECTIONS) {
                    int newX = curr.x + dir[0];
                    int newY = curr.y + dir[1];

                    // 越界或遇到障碍物,跳过
                    if (newX < 0 || newX >= n || newY < 0 || newY >= n || map[newX][newY] == '*' || map[newX][newY] == '#') {
                        continue;
                    }

                    // 与其他机器人路线交叉，跳过
                    if(curMap[newX][newY] == true){
                        continue;
                    }

                    // 碰到停止的机器人，跳过
                    if(stopBots[newX][newY] == true){
                        continue;
                    }

                    // 未访问过,加入队列
                    if (!stopBots[newX][newY]) {
                        stopBots[newX][newY] = true;
                        queue.offer(new Point(newX, newY, curr));
                    }
                }
            }
            // 重置其他Bot位置
            for(Robot robot1:robots){
                if(robot1.path != null && robot1.path.length>0){
                    if(robot1.path.length > time)
                        curMap[robot1.path[time][0]][robot1.path[time][1]] = true;
                    if(robot1.path.length > time+1)
                        curMap[robot1.path[time+1][0]][robot1.path[time+1][1]] = true;
                    if(robot1.path.length > time+2)
                        curMap[robot1.path[time+2][0]][robot1.path[time+2][1]] = true;
                }
            }
            time += 1;
        }

        // 无法到达目标点
        return null;

    }

    private Good getBestGood(Robot robot) {
        Good bestGood = null;
        double maxScore = Double.NEGATIVE_INFINITY;
        int[][] bestPath = null;
        int sizee = goods.size()*2/3;
        Good[] tempGoods = new Good[sizee];
        int bestGoodIndex = -1; // 记录最优货物在 tempGoods 数组中的下标

        // 计算 timeWeight 参数,在整个任务的前中期较大,后期较小,并引入指数衰减
        double timeWeight = Math.pow(0.9, this.id / 1000.0);

        for (int i=0;i<sizee;i++) {
            Good good = goods.poll();
            tempGoods[i] = good;
            // 判断机器人到达货物位置是否可达
            int[][] pathToGood = getShrotestPathWithoutConflict(robot.x, robot.y, good.x, good.y);
            if (pathToGood == null) {
                // 货物不可达,跳过
                continue;
            }

            // 判断机器人到达时货物是否还存活
            int pathLength = pathToGood.length;
            if (good.lifeTime <= pathLength) {
                // 货物在机器人到达前就会过期,跳过
                continue;
            }


            // 计算每一帧可获得的价值
            int distToGood = pathLength;
            int distToBerth = landToBerthDist[good.x][good.y]; // 从货物位置到最近泊位的距离
            double frameValue = good.value / (distToGood + distToBerth + 1);

            // 计算剩余生存时间得分
            int timeLeft = good.lifeTime - distToGood; // 货物剩余生存时间
            double timeScore = timeLeft * timeWeight; // 时间越多,得分越高,并根据任务进度动态调整权重

            // 计算综合得分
            double totalScore = frameValue * 10 + timeScore;

            // 更新最高分数的货物和路径
            if (totalScore > maxScore) {
                maxScore = totalScore;
                bestGood = good;
                bestPath = pathToGood;
//                bestGoodIndex = i; // 记录最优货物在 tempGoods 数组中的下标
            }
        }

        for(int i=0;i<sizee;i++) {
            if(tempGoods[i] != null && i != bestGoodIndex) {
                goods.offer(tempGoods[i]);
            }
        }

        // 将最佳路径存储在机器人的 path 属性中
        if (bestPath != null) {
            robot.path = bestPath;
        }

        return bestGood;
    }


    private Berth getBestBerth(Robot robot) {
        Berth bestBerth = null;
        double maxScore = Double.NEGATIVE_INFINITY;
        int[][] bestPath = null;

        for (Berth berth : berths) {
            int[][] pathToBerth = getShrotestPathWithoutConflict(robot.x, robot.y, berth.x, berth.y);

            if (pathToBerth == null) {
                // 货物不可达,跳过
                continue;
            }
            // 计算距离得分,距离越近得分越高
            double distScore = 1.0 / (pathToBerth.length + 1);

            // 计算装载速度得分,速度越快得分越高
//            double speedScore = berth.loading_speed / 5.0;
//
//            // 计算运输时间得分,时间越短得分越高
//            double timeScore = 2000.0 / berth.transport_time;

            // 计算综合得分
//            double totalScore = distScore * 0.4 + speedScore * 0.3 + timeScore * 0.3;
            double totalScore = distScore;
            // 更新最高分数的泊位和路径
            if (totalScore > maxScore) {
                maxScore = totalScore;
                bestBerth = berth;
                bestPath = pathToBerth;
            }
        }

        // 将最佳路径存储在机器人的 path 属性中
        if (bestPath != null) {
            robot.path = bestPath;
        }

        return bestBerth;
    }


    // 给出机器人的移动指令
    private void robotGo() {
        // 遍历每个机器人
        for (int i = 0; i < robot_num; i++) {
            Robot robot = robots[i];

            // 如果机器人处于恢复状态,则跳过
            if (robot.status == 0) {
                continue;
            }

            // 判断一下，如果机器人手上没有货物，也没有目标商品   表示现在在空闲，需要一个目标,在找目标的时候已经计算了路径，直接给到robot.path中去了
            if (robot.goods==0 && robot.targetGood == null){
                robot.targetGood = getBestGood(robot);
                if(robot.path != null) {
                    robot.path = Arrays.copyOfRange(robot.path, 1, robot.path.length);
                }
                continue;
            }

            // 判断一下，如果机器人手上有货物，但是没有目标泊位表示现在不知道送到哪去
            if (robot.goods==1 && robot.targetBerth == null){
                robot.targetBerth = getBestBerth(robot);
                if(robot.path != null) {
                    robot.path = Arrays.copyOfRange(robot.path, 1, robot.path.length);
                }
                continue;
            }

//            System.out.println(robot.x + " "+robot.y+" "+robot.targetGood.x+" "+robot.targetGood.y+" "+robot.targetGood.lifeTime +" "+Arrays.deepToString(robot.path));

            // 如果机器人有路径,则获取下一步需要去的点
            if (robot.path != null && robot.path.length > 0) {
                int[] nextPoint = robot.path[0]; // 获取路径中的第一个点
                int nextX = nextPoint[0];
                int nextY = nextPoint[1];

                // 根据机器人当前位置和目标点的相对位置,确定移动方向
                int moveDir;
                int dx = nextX - robot.x;
                int dy = nextY - robot.y;

                if (dx > 0) {
                    moveDir = 3; // 下移
                } else if (dx < 0) {
                    moveDir = 2; // 上移
                } else if (dy > 0) {
                    moveDir = 0; // 右移
                } else {
                    moveDir = 1; // 左移
                }

                System.out.printf("move %d %d%n", i, moveDir);
//                // 从路径中移除已经到达的点-------这部分放在input里面更新操作
//                robot.path = Arrays.copyOfRange(robot.path, 1, robot.path.length);
            }


            // 如果机器人位于货物处,且未携带货物,则取货
            if (robot.isOnGood() && robot.goods == 0) {
                System.out.printf("get %d%n", i);
                totalTrueValue = totalTrueValue+robot.targetGood.value;
                robot.goods = 1;
                goods.remove(robot.targetGood);
                robot.targetGood = null; // 清空目标货物
                robot.path = null; // 清空路径
                continue;
            }

            // 如果机器人位于泊位处,且携带货物,则卸货
            if (robot.isOnBerth() && robot.goods == 1) {
                System.out.printf("pull %d%n", i);
                robot.goods = 0;
                robot.targetBerth.addGood();
                robot.targetBerth = null; // 清空目标泊位
                robot.path = null; // 清空路径
            }
        }
//
//        System.out.println("OK");
    }


    private void boatGo() {
        for (int i = 0; i < 5; i++) {
            Boat boat = boats[i];

            if (boat.status == 0) {
                continue;
            }

            if (boat.pos >= 0 && boat.pos < berth_num && boat.currentUse == boat.capacity) {
                berths[boat.pos].hasBoat = 0;
                boat.currentUse = 0;
                System.out.printf("go %d%n", i);
                continue;
            }

            if (boat.pos >= 0 && boat.pos < berth_num && 14990 - id <= berths[boat.pos].transport_time) {
                System.out.printf("go %d%n", i);
                berths[boat.pos].hasBoat = 0;
                boat.currentUse = 0;
                continue;
            }
        }
    }

    /*
    *
    *
    * */
    private void precomputeLandToBerthDist() {
        landToBerthDist = new int[N][N];
        for (int i = 0; i < N; i++) {
            Arrays.fill(landToBerthDist[i], Integer.MAX_VALUE); // 初始化为无法到达
        }

        for (Berth berth : berths) {
            if (berth != null) {
                int[][] dist = new int[N][N]; // 从该泊位出发到达各个位置的距离
                for (int i = 0; i < N; i++) {
                    Arrays.fill(dist[i], Integer.MAX_VALUE);
                }
                bfs(dist, berth.x, berth.y);
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        if (map[i][j] != '#' && map[i][j] != '*') {
                            landToBerthDist[i][j] = Math.min(landToBerthDist[i][j], dist[i][j]);
                        }
                    }
                }
            }
        }
    }


    private boolean inArea(int x, int y) {
        return x >= 0 && x < N && y >= 0 && y < N;
    }

    private void bfs(int[][] dist, int sx, int sy) {
        int[][] DIRS = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}}; // 上下左右四个方向
        Queue<int[]> queue = new LinkedList<>();
        boolean[][] visited = new boolean[N][N]; // 记录已访问过的位置
        queue.offer(new int[]{sx, sy});
        visited[sx][sy] = true;
        dist[sx][sy] = 0; // 泊位到自身距离为0

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int x = cur[0], y = cur[1];
            for (int[] dir : DIRS) {
                int nx = x + dir[0], ny = y + dir[1];
                if (inArea(nx, ny) && !visited[nx][ny] && map[nx][ny] != '#' && map[nx][ny] != '*') {
                    visited[nx][ny] = true;
                    dist[nx][ny] = dist[x][y] + 1; // 更新距离
                    queue.offer(new int[]{nx, ny});
                }
            }
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------


    private void init() {
        Scanner scanf = new Scanner(System.in);
        for (int i = 1; i <= n; i++) {
            String line = scanf.nextLine();
            ch[i] = line;
            map[i - 1] = line.toCharArray();
        }
        for (int i = 0; i < berth_num; i++) {
            int id = scanf.nextInt();
            berths[id] = new Berth();
            berths[id].x = scanf.nextInt();
            berths[id].y = scanf.nextInt();
            berths[id].transport_time = scanf.nextInt();
            berths[id].loading_speed = scanf.nextInt();
//
//            // 获取泊位的陆地部分
//            List<int[]> roadPart = new ArrayList<>();
//            for (int x = berths[id].x; x < berths[id].x + 4; x++) {
//                for (int y = berths[id].y; y < berths[id].y + 4; y++) {
//                    roadPart.add(new int[]{x, y});
//                }
//            }
//            berths[id].roadPart = roadPart.toArray(new int[roadPart.size()][2]);
        }
        this.boat_capacity = scanf.nextInt();
        String okk = scanf.nextLine();

        // 初始化机器人对象
        for (int i = 0; i < robot_num; i++) {
            robots[i] = new Robot(0, 0);
        }

        // 初始化船舶对象
        for (int i = 0; i < 5; i++) {
            boats[i] = new Boat(i, boat_capacity);
        }

        System.out.println("OK");
        System.out.flush();
        precomputeLandToBerthDist();
        totalTrueValue = 0;
    }

    private int input() {
        updateGoods();
        Scanner scanf = new Scanner(System.in);
        this.id = scanf.nextInt();
        this.money = scanf.nextInt();
        int num = scanf.nextInt();
        for (int i = 1; i <= num; i++) {
            int x = scanf.nextInt();
            int y = scanf.nextInt();
            int val = scanf.nextInt();
            goods.add(new Good(x, y, val)); // 将新货物对象添加到goods列表中
        }
        for (int i = 0; i < robot_num; i++) {
            robots[i].goods = scanf.nextInt();
            robots[i].x = scanf.nextInt();
            robots[i].y = scanf.nextInt();
            int sts = scanf.nextInt();
            robots[i].status = sts;

            // 判断一下机器人有没有到目标位置，到了就删掉，配合后面robotGo()做的操作
            if (robots[i].path != null &&robots[i].path.length > 0){
                int[] nextPoint = robots[i].path[0]; // 获取路径中的第一个点
                int nextX = nextPoint[0];
                int nextY = nextPoint[1];

                if (robots[i].x==nextX && robots[i].y==nextY){
                    robots[i].path = Arrays.copyOfRange(robots[i].path, 1, robots[i].path.length);
                }
            }

        }
//        freeBoats.clear();
        for (int i = 0; i < 5; i++) {
            boats[i].status = scanf.nextInt();
            boats[i].pos = scanf.nextInt();
            // 考虑到掉帧的问题，将泊位内容的更新放入到前面保证可以运行
            if (boats[i].pos >= 0 && boats[i].pos < berth_num && boats[i].status == 1&& boats[i].currentUse < boats[i].capacity) {
                Berth berth = berths[boats[i].pos];
                int loadAmount = Math.min(berth.loading_speed, berth.goodNum);
                loadAmount = Math.min(loadAmount, boats[i].capacity - boats[i].currentUse);
                boats[i].currentUse += loadAmount;
                berth.removeGoods(loadAmount);
            }
//            // 将已完成运输或所在泊位无货物的船舶加入空闲队列
//            if (boats[i].status == 1 && boats[i].pos == -1) {
//                freeBoats.offer(i);
//            }
//            if (boats[i].pos >= 0 && boats[i].pos < berth_num && boats[i].status == 1 && berths[boats[i].pos].goodNum < 3){
//                freeBoats.offer(i);
//                try {
//                    // 创建FileWriter对象
//                    FileWriter fileWriter = new FileWriter("debug.txt");
//
//                    // 创建BufferedWriter对象
//                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
//
//                    // 写入totalTrueValue的值
//                    bufferedWriter.write(freeBoats.toString());
//                    bufferedWriter.write('\n');
//                    for (int x = 0; x < berth_num; x++) {
//                        bufferedWriter.write('\n');
//                        bufferedWriter.write(berths[x].toString());
//                    }
//
//                    // 关闭BufferedWriter和FileWriter
//                    bufferedWriter.close();
//                    fileWriter.close();
////                    System.exit(0);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        // 更新有货物泊位队列
//        loadedBerths.clear();
//        for (int i = 0; i < berth_num; i++) {
//            if (berths[i].goodNum >= 0 && berths[i].hasBoat == 0) {
//                loadedBerths.offer(i);
//            }
        }
        String okk = scanf.nextLine();
        return id;
    }

    public static void main(String[] args) {
        Main mainInstance = new Main();
        mainInstance.init();
        for (int zhen = 1; zhen <= 15000; zhen++) {
            int id = mainInstance.input();
            mainInstance.robotGo();
            mainInstance.boatGo();
            System.out.println("OK");
            System.out.flush();
            if (zhen>13000){
                try {
                    // 创建FileWriter对象
                    FileWriter fileWriter = new FileWriter("totalTrueValue.txt");

                    // 创建BufferedWriter对象
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                    // 写入totalTrueValue的值
                    bufferedWriter.write("totalTrueValue: " + mainInstance.totalTrueValue);

                    // 关闭BufferedWriter和FileWriter
                    bufferedWriter.close();
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    class Robot {
        int x, y, goods;
        int status;
        int mbx, mby;
        Good targetGood = null; // 机器人要去拿的目标货物
        Berth targetBerth = null; // 机器人要去卸货的目标泊位
        int[][] path; // 机器人要走的路径,由getBestPath()函数返回的二维数组

        public Robot() {
        }

        public Robot(int startX, int startY) {
            this.x = startX;
            this.y = startY;
        }

        // 判断机器人是否位于货物上
        public boolean isOnGood() {
            if (targetGood != null && x == targetGood.x && y == targetGood.y) {
                return true;
            }
            return false;
        }

        // 判断机器人是否位于泊位上
        public boolean isOnBerth() {
            if (targetBerth != null && x >= targetBerth.x && x < targetBerth.x + 4 && y >= targetBerth.y && y < targetBerth.y + 4) {
                return true;
            }
            return false;
        }
    }

    class Berth {
        int x;
        int y;
        int transport_time;
        int loading_speed;

        int goodNum;
        int hasBoat;
//        int[][] roadPart;

        public Berth() {
        }

        public Berth(int x, int y, int transport_time, int loading_speed) {
            this.x = x;
            this.y = y;
            this.transport_time = transport_time;
            this.loading_speed = loading_speed;
            goodNum = 0;
            hasBoat = 0;
        }

        public void addGood() {
            goodNum++;
        }

        public void removeGoods(int x) {
            if (x>goodNum|| x>loading_speed){
                return;
            }
            goodNum = goodNum - x;
        }

        @Override
        public String toString() {
            return "Berth{" +
                    "x=" + x +
                    ", y=" + y +
                    ", transport_time=" + transport_time +
                    ", loading_speed=" + loading_speed +
                    ", goodNum=" + goodNum +
                    ", hasBoat=" + hasBoat +
                    '}';
        }
    }

    class Boat {
        int num;
        int pos; // 目标点，虚拟点是-1，泊位是泊位在数组中的编号
        int status; // 状态分为0-1-2

        int capacity;// 初始化的时候给的船的容量
        int currentUse; // 表示当时已经使用的船舱空间大小

        public Boat() {
        }

        public Boat(int num, int capacity) {
            this.num = num;
            this.capacity = capacity;
        }
    }

    class Good {
        int x, y;
        int value;
        int lifeTime; // 货物的生存时间,初始为1000
        boolean isAlive;

        public Good(int x, int y, int value) {
            this.x = x;
            this.y = y;
            this.value = value;
            this.lifeTime = 1000; // 初始生存时间为1000帧
            this.isAlive = true;
        }

        // 减少生存时间,如果减少后生存时间为0,则将isAlive设置为false
        public void decreaseLifeTime() {
            lifeTime--;
            if (lifeTime == 0) {
                isAlive = false;
            }
        }

        // 返回商品是否还存在
        public boolean isStillAlive() {
            return isAlive;
        }
    }

    class Point {
        int x, y;
        Point parent;

        Point(int x, int y, Point parent) {
            this.x = x;
            this.y = y;
            this.parent = parent;
        }
    }
}

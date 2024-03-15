/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.huawei.codecraft;

//import com.huawei.codecraft.backup.AStarPathSearchv1;

import java.util.*;

/**
 * Main: the agents receiving environment input and sending actions of robots and ships
 *
 * @since 2024-02-05
 */
public class Main {
    //prepare containers for input
    private static final int n = 200; //map has 200 lines
    private static final int robot_num = 10;
    private static final int berth_num = 10;
    private static final int N = 210;//this N means : the container size

    private int money, boat_capacity, id;
    private String[] ch = new String[N];//this ch means : the map

    private int[][] blockArray;// the blocks in map
    private int[][] gds = new int[N][N];// this gds means : the goods?

    private Robot[] robot = new Robot[robot_num + 10];//why +10?
    private Berth[] berth = new Berth[berth_num + 10];
    private Boat[] boat = new Boat[10];

    //after we read the first zhen, gds, robot, berth, boat will be available,
    //they represents the current state of the environment

    /**
     *Main(agents) load the map data from pipe of System.in , within 5s
     */
    private void init() {
        Scanner scanf = new Scanner(System.in);
        //输入地图
        for(int i = 1; i <= n; i++) {
            ch[i] = scanf.nextLine();
        }
        //获取地图上的障碍物信息
        ArrayList<int[]> blockList = new ArrayList<>();
        for(int i = 1; i <= n; i++) {//ch第0行是null
            for(int j = 0; j < n; j++) {
                if(ch[i].charAt(j) == '#' || ch[i].charAt(j) == '*') {
                    blockList.add(new int[]{i-1, j});
                }
            }
        }
        blockArray = blockList.toArray(new int[0][0]);

        for(int i = 0; i < robot_num; i++) {
            robot[i] = new Robot();
        }
        //10行泊位数据
        for (int i = 0; i < berth_num; i++) {
            int id = scanf.nextInt();
            berth[id] = new Berth();
            berth[id].x = scanf.nextInt();
            berth[id].y = scanf.nextInt();//泊位左上坐标，4*4大小
            berth[id].transport_time = scanf.nextInt();//运输到虚拟点所需时间
            berth[id].loading_speed = scanf.nextInt();//每帧可以装载的物品数量
        }//now we have the Berth objects in berth array
        this.boat_capacity = scanf.nextInt();//船的容积
        String okk = scanf.nextLine();//priliminaryJudge.exe input "OK" means input finished.
        System.out.println("OK");
        System.out.flush();

        for(int i = 0; i < 5; i ++) {
            boat[i] = new Boat();
            boat[i].flag=0;
        }
    }

    /**
     * Main(agents) agents receive the input data of every envirment zhen through pipe of System.in
     * @return id
     */
    private int input() {
        Scanner scanf = new Scanner(System.in);
        this.id = scanf.nextInt();//zhen serial number
        this.money = scanf.nextInt();// current total rewards of the agents
        int num = scanf.nextInt();// number of new goods in map
        for (int i = 1; i <= num; i++) {
            int x = scanf.nextInt();//info of new goods
            int y = scanf.nextInt();
            int val = scanf.nextInt();//but these data was wasted here, we should use gds to save these data in nextline
            gds[x][y] = val;
        }
        for(int i = 0; i < robot_num; i++) {
            robot[i].goods = scanf.nextInt();//Is robot i wether carrying a good?
            robot[i].x = scanf.nextInt();//robot i's position
            robot[i].y = scanf.nextInt();
            int sts = scanf.nextInt();//Is robot i workable?
            if(sts==0){
                robot[i].status=-1;//异常
            }
        }
        for(int i = 0; i < 5; i ++) {
            boat[i].status = scanf.nextInt();//0:moving 1:workable 2:wating outside berth
            boat[i].pos = scanf.nextInt();//berth_id or -1(in moving)
        }
        String okk = scanf.nextLine();
        return id;// zhen id
    }

    /**
     * Interacting Process of Main(agents) and Judge.exe
     * @param args
     */
    public static void main(String[] args) {
        Main mainInstance = new Main();//create a agents(Main)
        mainInstance.init();//load map data
        //初始化寻路算法

//        AStarPathSearchv1 ps = new AStarPathSearchv1(mainInstance.ch, 1, 1, 200, 200);
        Random random = new Random();

        //与判题器交互
        for(int zhen = 1; zhen <= 15000; zhen ++) { // read zhen1~15000 data from judge.exe
            int id = mainInstance.input();


            /*
             -----------------机器人操作-----------------
             */
            for(int i = 0; i < robot_num; i ++){
                Robot r=mainInstance.robot[i];
                assert r!=null:"robot is null";
                if(r.status==-1){//异常状态:返回泊位点右下角位置 TODO 改成重新寻路到随机泊位
                    LinkedList<Integer> mvPath = AStar.findPath(r.x, r.y, mainInstance.berth[i].x + 3, mainInstance.berth[i].y + 3,mainInstance.blockArray);
//                    LinkedList<Integer> mvPath = ps.findPath(r.x, r.y, mainInstance.berth[i].x + 3, mainInstance.berth[i].y + 3);
                    if(mvPath.size()==0){//如果根本到不了泊位 或 迭代次数超时,则就地找货物机器人
                        r.status=0;
                    }else {
                        System.out.printf("move %d %d" + System.lineSeparator(), i, mvPath.poll());
                        r.mvPath=mvPath;
                        r.status=2;
                    }
                }else if(r.status==0){
                    //拿到range*2范围内的所有货物gds
                    int[][] goodsMap=mainInstance.gds;
                    int range=100;
                    long MaxWeight=-1;//权重定义为货物价值/距离机器人的距离
                    LinkedList<Integer> BestPath=null;
                    int[] BestGood=null;
                    //搜索range*2范围内的所有货物gds
                    for(int j=r.x-range;j<r.x+range;++j){
                        for(int k=r.y-range;k<r.y+range;++k){
                            if(j>=0 && j<goodsMap.length && k>=0 && k<goodsMap[0].length && goodsMap[j][k]!=0){//是货物
                                int[] Good = new int[]{j,k};
                                int distance = Math.abs(r.x - Good[0]) + Math.abs(r.y - Good[1]);
                                if(distance!=0  &&  (goodsMap[j][k]/distance)>MaxWeight){//更新权重最大的物品
                                    MaxWeight=goodsMap[j][k]/distance;
                                    BestGood=Good;
                                }
                            }
                        }

                    }
                    if(BestGood!=null){//才能试图寻找BestPath
                        BestPath = AStar.findPath(r.x, r.y, BestGood[0], BestGood[1],mainInstance.blockArray);
                        if (!BestPath.isEmpty()) {//只有当找到路径时才能保存到BestPath
                            r.mvPath=BestPath;
                            r.status=1;
                            //机器人已锁定此货物，别的机器人不能再取
                            mainInstance.gds[BestGood[0]][BestGood[1]]=-1;
                            System.out.printf("move %d %d" + System.lineSeparator(), i, r.mvPath.poll());

                        }
                    }

                    if(BestGood==null||BestPath==null){//范围内都没有物品，让机器人随机游走
                        //范围内都没有物品，让机器人随机游走
                        System.out.printf("move %d %d" + System.lineSeparator(), i,random.nextInt(4)%4);
                        r.status=0;
                    }


                }else if (r.status==1) {//机器人前往货物处
                    if(r.mvPath.size()>0){// still on the way
                        System.out.printf("move %d %d" + System.lineSeparator(), i, r.mvPath.poll());
                    }else{// already arrived at good position
                        if (mainInstance.gds[r.x][r.y] > 0 || mainInstance.gds[r.x][r.y] == -1) {//货物仍在，就取货
                            mainInstance.gds[r.x][r.y]=0;//将这个物品标记为消失
                            System.out.printf("get %d" + System.lineSeparator(), i);
                            r.status=2;
                            r.mvPath= AStar.findPath(r.x, r.y, mainInstance.berth[i].x + 2, mainInstance.berth[i].y + 2,mainInstance.blockArray);
//                            r.mvPath=ps.findPath(r.x, r.y, mainInstance.berth[i * 2].x + 3, mainInstance.berth[i * 2].y + 3);
                        } else {
                            //if good has been taken by other robot
                            r.status=0;
                        }
                    }
                }else if (r.status==2) {//机器人前往泊位
                    if(r.mvPath.size()>0){// still on the way
                        System.out.printf("move %d %d" + System.lineSeparator(), i, r.mvPath.poll());
                    }else{// already arrived at berth
                        System.out.printf("pull %d" + System.lineSeparator(), i);
                        mainInstance.berth[i].goods_num+=r.goods;//机器人对应的泊位货物数量+1
                        r.status=0;
                    }
                }
            }
            /*
             -----------------船只操作-----------------
             */
            for (int i=0;i<5;++i){
                Main.Boat boat=mainInstance.boat[i];//每一帧每艘船有一套相同的操作逻辑
                if (id==1){//第一帧,每一艘船都在虚拟点，前往泊位
                    System.out.printf("ship %d %d" + System.lineSeparator(), i,i*2);
                }else if(boat.status==0 || boat.status==2){//非第一帧，船处于0:船移动中 2:泊位外等待
                    //不执行操作
                }else if(boat.status==1){ //非第一帧，船处于等待指令状态
                    if(boat.pos!=-1){//船只已经到达泊位
                        mainInstance.berth[boat.pos].ship=true;//泊位占用信号


                        //每一帧试图装货
                        //装货量=最小值(船仓余量，泊位货物数量，装货速度)
                        int boatAllowance = mainInstance.boat_capacity - boat.loadedGoods;
                        int loadingGoods = Math.min(boatAllowance, Math.min( mainInstance.berth[boat.pos].goods_num, mainInstance.berth[boat.pos].loading_speed));
                        boat.loadedGoods+=loadingGoods;
                        mainInstance.berth[boat.pos].goods_num-=boat.loadedGoods;

                        //装完后发现装满了，就离开
                        if (boat.loadedGoods >= mainInstance.boat_capacity || mainInstance.berth[boat.pos].goods_num<=0){
                            System.out.printf("go %d" + System.lineSeparator(), i);
                            boat.flag=(boat.flag==1)?0:1;
                            mainInstance.berth[boat.pos].ship=false;

                        }


                    } else {//船只已经到达虚拟点
                        //卸下货物
                        if (boat.loadedGoods>0)boat.loadedGoods=0;
                        //返航
                        System.out.printf("ship %d %d" + System.lineSeparator(), i,i*2+boat.flag);

                    }

                }
            }


            System.out.println("OK");
            System.out.flush();
        }
    }
    //TODO
    /**
     * 机器人避障寻路(mainstance 的实例方法)
     */
    public LinkedList<Integer> findPathAvoidingRobots(int startX, int startY, int endX, int endY, int currentRobotIndex) {
        Robot[] robots = this.robot;
        int[][] blocksArray = this.blockArray;

        //mvPath 和 节点差值 转换字典
        int[][] nodeMoveDict = new int[][]{{0, 1}, {0, -1}, {-1, 0}, {1, 0}};//TODO 检查方向是否正确

        // 更新障碍物数组以包含其他机器人的位置作为临时障碍
        int[][] updatedBlocksArray = Arrays.copyOf(blocksArray, blocksArray.length);
        for (int i = 0; i < robots.length; i++) {
            if (i != currentRobotIndex) { // 排除当前计算路径的机器人
                Robot otherRobot = robots[i];
                int robotX = otherRobot.x;
                int robotY = otherRobot.y;
                // 假设你有方法来将机器人位置转换为blocksArray中的索引

                for (Integer integer : otherRobot.mvPath) {//把别的机器人的可能路径也标记为障碍
                    int[] blockIndex = new int[]{robotX, robotY};
                    updatedBlocksArray[blockIndex[0]][blockIndex[1]] = 1; // 标记为障碍
                    int[] move = nodeMoveDict[integer];
                    robotX += move[0];
                    robotY += move[1];
                }


            }
        }
        // 使用更新的障碍物数组计算路径
        return AStar.findPath(startX, startY, endX, endY, updatedBlocksArray);
    }


    /**
     * Entity: Robot
     */
    class Robot {
        int x, y, goods;
        int status; //-1异常状态 0 空闲 1前往物品中 2拿到物品前往泊位中
        int mbx, mby;

        public Robot() {
            status=-1;
        }

        public Robot(int startX, int startY) {
            this.x = startX;
            this.y = startY;
            status=-1;
        }

        LinkedList<Integer> mvPath;
    }

    /**
     * Entity: Berth
     */
    class Berth {
        int x;
        int y;
        int transport_time;
        int loading_speed;

        int goods_num=0;
        public boolean ship=false;

        public Berth(){}
        public Berth(int x, int y, int transport_time, int loading_speed) {
            this.x = x;
            this.y = y;
            this.transport_time = transport_time;
            this.loading_speed = loading_speed;
        }
    }

    /**
     * Entity: Boat
     */
    class Boat {
        int num;
        int pos;
        int status;

        int loadedGoods=0;

        int flag;
    }
}
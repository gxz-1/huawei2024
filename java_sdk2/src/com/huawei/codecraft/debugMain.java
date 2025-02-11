/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.huawei.codecraft;

//import com.huawei.codecraft.backup.AStarPathSearchv1;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main: the agents receiving environment input and sending actions of robots and ships
 *
 * @since 2024-02-05
 */
public class debugMain {
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
    private void init(Scanner scanf) {

        //输入地图
        for(int i = 1; i <= n; i++) {
            ch[i] = scanf.nextLine();
        }
        //获取地图上的障碍物信息
        ArrayList<int[]> blockList = new ArrayList<>();
        for(int i = 1; i <= n; i++) {//ch第0行是null
            for(int j = 0; j < n; j++) {
                if(ch[i].charAt(j) == '#' || ch[i].charAt(j) == 'A'|| ch[i].charAt(j) == '*') {
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
            berth[id].ship= false;
        }//now we have the Berth objects in berth array
        this.boat_capacity = scanf.nextInt();//船的容积
        String okk = scanf.nextLine();//priliminaryJudge.exe input "OK" means input finished.
        scanf.nextLine();
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
    private int input(Scanner scanf) {
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
        scanf.nextLine();
        String okk = scanf.nextLine();
        return id;// zhen id
    }

    /**
     * Interacting Process of Main(agents) and Judge.exe
     * @param args
     */
    public static void main(String[] args) {

        Scanner scanf = null;
        try {
            File file = new File("maps\\debuginput1.txt");
            scanf = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        debugMain mainInstance = new debugMain();//create a agents(Main)
        mainInstance.init(scanf);//load map data
        //初始化寻路算法

//        AStarPathSearchv1 ps = new AStarPathSearchv1(mainInstance.ch, 1, 1, 200, 200);
        Random random = new Random();

        //与判题器交互
        for(int zhen = 1; zhen <= 15000; zhen ++) { // read zhen1~15000 data from judge.exe
            int id = mainInstance.input(scanf);
            if(zhen>=3000){
                System.out.println("zhen=4000");
            }
            /*
            --------------移动机器人------------------
             */
            for(int i = 0; i < robot_num; i ++){
                Robot r=mainInstance.robot[i];
                assert r!=null:"robot is null";
                if(r.status==-1){//异常状态:返回泊位点右下角位置
                    //TODO check
                    LinkedList<Integer> mvPath = mainInstance.findPathAvoidingRobots(r.x, r.y, mainInstance.berth[i].x + 3, mainInstance.berth[i].y + 3, i);
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
                    int range=25;
                    long MaxWeight=-1;//权重定义为货物价值/距离机器人的距离
                    LinkedList<Integer> BestPath=null;
                    //搜索range*2范围内的所有货物gds
                    for(int j=r.x-range;j<r.x+range;++j){
                        for(int k=r.y-range;k<r.y+range;++k){
                            if(j>=0 && j<goodsMap.length && k>=0 && k<goodsMap[0].length && goodsMap[j][k]!=0){//是货物
                                LinkedList<Integer> path = AStar.findPath(r.x, r.y, j, k,mainInstance.blockArray);
//                                LinkedList<Integer> path = ps.findPath(r.x, r.y, j, k);//切换成AStarv2寻路
                                if(path.size()!=0 &&  (goodsMap[j][k]/path.size())>MaxWeight){//更新权重最大的物品
                                    MaxWeight=goodsMap[j][k]/path.size();
                                    BestPath=path;
                                }
                            }
                        }

                    }
                    if(BestPath==null){
                        //范围内都没有物品，让机器人随机游走
                        System.out.printf("move %d %d" + System.lineSeparator(), i,random.nextInt(4)%4);
                        r.status=0;
                    }else {
                        //有物品
                        System.out.printf("move %d %d" + System.lineSeparator(), i, BestPath.poll());
                        r.mvPath=BestPath;
                        r.status=1;
                    }
                }else if (r.status==1) {//robot is moving to target good and will get it
                    if(r.mvPath.size()>0){// still on the way
                        System.out.printf("move %d %d" + System.lineSeparator(), i, r.mvPath.poll());
                    }else{// already arrived at good position
                        //if good still exists
                        if (mainInstance.gds[r.x][r.y] > 0) {
                            mainInstance.gds[r.x][r.y]=0;//将这个物品标记为消失
                            System.out.printf("get %d" + System.lineSeparator(), i);
                            r.status=2;
                            r.mvPath= AStar.findPath(r.x, r.y, mainInstance.berth[i].x + 3, mainInstance.berth[i].y + 3,mainInstance.blockArray);
//                            r.mvPath=ps.findPath(r.x, r.y, mainInstance.berth[i * 2].x + 3, mainInstance.berth[i * 2].y + 3);
                        } else {
                            //if good has been taken by other robot
                            r.status=0;
                        }
                    }
                }else if (r.status==2) {//robot is moving to berth
                    if(r.mvPath.size()>0){// still on the way
                        System.out.printf("move %d %d" + System.lineSeparator(), i, r.mvPath.poll());
                    }else{// already arrived at berth
                        System.out.printf("pull %d" + System.lineSeparator(), i);
                        r.status=0;
                    }
                }
            }
            /*
             -----------------船只操作-----------------
             */
//
            for (int i=0;i<5;++i){
                Boat boat=mainInstance.boat[i];
                if(boat.status==0 || boat.status==2){//0:船移动中 2:泊位外等待
                    //不执行操作
                }else if(boat.status==1){ //1:正常运行状态(即装货状态或运输完成状态)
                    if(zhen==1 || boat.pos==-1){//船在虚拟点
                        System.out.printf("ship %d %d" + System.lineSeparator(), i,i*2+boat.flag);
                    }else{//从3000帧开始不停的运
                        //移动到虚拟点
                        System.out.printf("go %d" + System.lineSeparator(), i);
                        boat.flag=(boat.flag==1)?0:1;
                    }

                }
            }
            System.out.println(zhen+"OK");
            System.out.flush();
        }
    }
    //TODO: Implement a method to calculate the our path
    public LinkedList<Integer> findPathAvoidingRobots(int startX, int startY, int endX, int endY, int currentRobotIndex) {
        Robot[] robots = this.robot;
        int[][] updatedBlocksArray = Arrays.copyOf(this.blockArray, blockArray.length);
        for (int i = 0; i < updatedBlocksArray.length; i++) {
            for (int j = 0; j < 2; j++) {

            }
        }

        //mvPath 和 节点差值 转换字典
        int[][] nodeMoveDict = new int[][]{{0, 1}, {0, -1}, {-1, 0}, {1, 0}};//TODO 检查方向是否正确

        // 更新障碍物数组以包含其他机器人的位置作为临时障碍
        List<int[]> blocksList = Arrays.stream(updatedBlocksArray).collect(Collectors.toList());
        for (int i = 0; i < 10; i++) {
            if (i != currentRobotIndex && robots[i].mvPath!=null) { // 排除当前计算路径的机器人 和没有mvPath的机器人
                Robot otherRobot = robots[i];
                int robotX = otherRobot.x;
                int robotY = otherRobot.y;
                // 假设你有方法来将机器人位置转换为blocksArray中的索引

                //虚拟行走预演
                for (int index=0;i<otherRobot.mvPath.size();index++) {//把别的机器人的可能路径也标记为障碍
                    int moveCommand = otherRobot.mvPath.poll();
                    int[] move = nodeMoveDict[moveCommand];
                    robotX += move[0];
                    robotY += move[1];
                    int[] blockIndex = new int[]{robotX, robotY};
                    blocksList.add(blockIndex);


                }




            }
        }
        updatedBlocksArray = blocksList.toArray(new int[0][]);

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
        LinkedList<Integer> mvPath=null;//TODO check


        public Robot() {
            status=-1;
            mvPath=null;
        }

        public Robot(int startX, int startY) {
            this.x = startX;
            this.y = startY;
            status=-1;
        }

    }

    /**
     * Entity: Berth
     */
    class Berth {
        int x;
        int y;
        int transport_time;
        int loading_speed;
        boolean ship;//用于判断泊位内是否有船停靠，进而为 船只停泊 和 机器人装货 提供指导-----------------------------------------------

        public Berth(){

        }
        public Berth(int x, int y, int transport_time, int loading_speed) {
            this.x = x;
            this.y = y;
            this.transport_time = transport_time;
            this.loading_speed = loading_speed;
            this.ship=false;

        }
    }

    /**
     * Entity: Boat
     */
    class Boat {
        int num;
        int pos;//泊位id
        int status;//0:船移动中 1:等待指令状态(即装货状态或运输完成状态) 2:泊位外等待
        int flag;
    }
}

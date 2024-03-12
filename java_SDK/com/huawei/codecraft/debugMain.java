/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.huawei.codecraft;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

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
        okk=scanf.nextLine();
        System.out.println(okk);
        System.out.flush();
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
            robot[i] = new Robot();
            robot[i].goods = scanf.nextInt();//Is robot i wether carrying a good?
            robot[i].x = scanf.nextInt();//robot i's position
            robot[i].y = scanf.nextInt();
            int sts = scanf.nextInt();//Is robot i workable?
            if(sts==0){
                robot[i].status=-1;//异常
            }
        }
        for(int i = 0; i < 5; i ++) {
            boat[i] = new Boat();
            boat[i].status = scanf.nextInt();//0:moving 1:workable 2:wating outside berth
            boat[i].pos = scanf.nextInt();//berth_id or -1(in moving)
        }
        String okk = scanf.nextLine();
        okk=scanf.nextLine();
        System.out.println(okk);
        return id;// zhen id
    }

    /**
     * Interacting Process of Main(agents) and Judge.exe
     * @param args
     */
    public static void main(String[] args) {

        Scanner scanf = null;
        try {//TODO 读取输入文件
            File file = new File("maps/debuginput1.txt");
            scanf = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        debugMain mainInstance = new debugMain();//create a agents(Main)
        mainInstance.init(scanf);//load map data
        //TODO 初始化寻路算法
        AStarPathSearch ps = new AStarPathSearch(mainInstance.ch);
        for(int zhen = 1; zhen <= 15000; zhen ++) { // read zhen1~15000 data from judge.exe
            int id = mainInstance.input(scanf);
            //TODO 编写移动逻辑
            //移动机器人
            Random random = new Random();
            for(int i = 0; i < robot_num; i ++){
                Robot r=mainInstance.robot[i];
                if(r.status==-1){//异常状态:返回泊位点右下角位置
                    LinkedList<Integer> mvPath = ps.findPath(r.x, r.y, mainInstance.berth[i * 2].x + 2, mainInstance.berth[i * 2].y + 2);
                    System.out.printf("move %d %d" + System.lineSeparator(), i, mvPath.poll());
                    r.mvPath=mvPath;
                    r.status=2;
                }else if(r.status==0){
                    //拿到range*2范围内的所有货物gds
                    int[][] goodsMap=mainInstance.gds;
                    int range=25;
                    long MaxWeight=-1;//权重定义为货物价值/距离机器人的距离
                    LinkedList<Integer> BestPath=null;
                    for(int j=r.x-range;j<r.x+range;++j){
                        for(int k=r.y-range;k<r.y+range;++k){
                            if(j>=0 && j<goodsMap.length && k>=0 && k<goodsMap[0].length && goodsMap[j][k]!=0){//是货物
                                LinkedList<Integer> path = ps.findPath(r.x, r.y, j, k);
                                if(path.size()!=0 &&  (goodsMap[j][k]/path.size())>MaxWeight){//更新权重最大的物品
                                    MaxWeight=goodsMap[j][k]/path.size();
                                    BestPath=path;
                                }
                            }
                        }
                        if(BestPath==null){
                            //TODO range范围内都没有物品，让机器人随机游走
                            System.out.printf("move %d %d" + System.lineSeparator(), i,random.nextInt(4)%4);
                            r.status=0;
                        }else {
                            System.out.printf("move %d %d" + System.lineSeparator(), i, BestPath.poll());
                            r.mvPath=BestPath;
                            r.status=1;
                        }
                    }
                }else if (r.status==1) {//robot is moving to target good and will get it
                    if(r.mvPath.size()>0){// still on the way
                        System.out.printf("move %d %d" + System.lineSeparator(), i, r.mvPath.poll());
                    }else{// already arrived at good position
                        //if good still exists

                        if (mainInstance.gds[r.x][r.y] > 0) {
                            System.out.printf("get %d" + System.lineSeparator(), i);
                            r.status=2;
                            r.mvPath=ps.findPath(r.x, r.y, mainInstance.berth[i * 2].x + 2, mainInstance.berth[i * 2].y + 2);
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
            //移动船
            if(zhen==1){
                for (int i=0;i<5;++i){
                    System.out.printf("ship %d %d" + System.lineSeparator(), i,i*2);
                }
            }
            System.out.println("OK"+zhen);
            System.out.flush();
        }
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
    }
}

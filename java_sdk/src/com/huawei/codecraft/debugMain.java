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
 * Main
 *
 * @since 2024-02-05
 */
public class debugMain {

    private static final int n = 200;
    private static final int robot_num = 10;
    private static final int berth_num = 10;
    private static final int N = 210;

    private int money, boat_capacity, id;
    private String[] ch = new String[N];
    private int[][] gds = new int[N][N];

    private Robot[] robot = new Robot[robot_num + 10];
    private Berth[] berth = new Berth[berth_num + 10];
    private Boat[] boat = new Boat[10];

    private void init(Scanner scanf) {
        for(int i = 1; i <= n; i++) {
            ch[i] = scanf.nextLine();
        }
        for(int i = 0; i < robot_num; i++) {
            robot[i] = new Robot();
        }
        for (int i = 0; i < berth_num; i++) {
            int id = scanf.nextInt();
            berth[id] = new Berth();
            berth[id].x = scanf.nextInt();
            berth[id].y = scanf.nextInt();
            berth[id].transport_time = scanf.nextInt();
            berth[id].loading_speed = scanf.nextInt();
        }
        this.boat_capacity = scanf.nextInt();
        String okk = scanf.nextLine();
        scanf.nextLine();
        System.out.println("OK");
        System.out.flush();

        for(int i = 0; i < 5; i ++) {
            boat[i] = new Boat();
        }
    }

    private int input(Scanner scanf)  {
        this.id = scanf.nextInt();
        this.money = scanf.nextInt();
        int num = scanf.nextInt();
        for (int i = 1; i <= num; i++) {
            int x = scanf.nextInt();
            int y = scanf.nextInt();
            int val = scanf.nextInt();
            gds[x][y] = val;
        }
        for(int i = 0; i < robot_num; i++) {
            robot[i].goods = scanf.nextInt();
            robot[i].x = scanf.nextInt();
            robot[i].y = scanf.nextInt();
            int sts = scanf.nextInt();
            if(sts==0){
                robot[i].status=-1;//异常
            }
        }
        for(int i = 0; i < 5; i ++) {

            boat[i].status = scanf.nextInt();
            boat[i].pos = scanf.nextInt();
        }

        String okk = scanf.nextLine();
        return id;
    }

    public static void main(String[] args) {

        Scanner scanf = null;
        try {
            File file = new File("C:\\Users\\21232\\Desktop\\HuaweiCup\\HUAWEICup\\maps\\debuginput1.txt");
            scanf = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        debugMain mainInstance = new debugMain();
        mainInstance.init(scanf);//load map data
        //TODO 初始化寻路算法
        AStarPathSearch ps = new AStarPathSearch(mainInstance.ch, 1, 1, 200, 200);
        for(int zhen = 1; zhen <= 15000; zhen ++) { // read zhen1~15000 data from judge.exe
            int id = mainInstance.input(scanf);
            //TODO 编写移动逻辑
            //移动机器人
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
                    for(int j=r.x-25;j<r.x+25;++j){
                        for(int k=r.y-25;k<r.y+25;++k){
                            if(j>=0 && j<goodsMap.length && k>=0 && k<goodsMap[0].length && goodsMap[j][k]!=0){//是货物
                                LinkedList<Integer> path = ps.findPath(r.x, r.y, j, k);
                                if(path.size()!=0 &&  (goodsMap[j][k]/path.size())>MaxWeight){//更新权重最大的物品
                                    MaxWeight=goodsMap[j][k]/path.size();
                                    BestPath=path;
                                }
                            }

                        }
                    }
                    if(BestPath==null){
                        //TODO range范围内都没有物品，让机器人随机游走
                        Random random = new Random();
                        System.out.printf("move %d %d" + System.lineSeparator(), i,random.nextInt(4)%4);
                        r.status=0;
                        continue;//移动下一个机器人
                    }
                    System.out.printf("move %d %d" + System.lineSeparator(), i, BestPath.poll());
                    r.mvPath=BestPath;
                    r.status=1;
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

                } else if (r.status==2) {//robot is moving to berth

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
            String OK = scanf.nextLine();
            System.out.println(OK);
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

        public Robot() {status=-1;}

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

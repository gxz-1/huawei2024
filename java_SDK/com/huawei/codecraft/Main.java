/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.huawei.codecraft;

import java.util.Random;
import java.util.Scanner;

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
        for(int i = 1; i <= n; i++) {
            ch[i] = scanf.nextLine();
        }//now we have the map data in ch
        for (int i = 0; i < berth_num; i++) {
            int id = scanf.nextInt();
            berth[id] = new Berth();
            berth[id].x = scanf.nextInt();
            berth[id].y = scanf.nextInt();
            berth[id].transport_time = scanf.nextInt();
            berth[id].loading_speed = scanf.nextInt();
        }//now we have the Berth objects in berth array
        this.boat_capacity = scanf.nextInt();//boat_capacity is a constant integer
        String okk = scanf.nextLine();//priliminaryJudge.exe input "OK" means input finished.
        System.out.println("OK");
        System.out.flush();
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
            int val = scanf.nextInt();//but these data was wasted here, we should use gds to save these data

        }
        for(int i = 0; i < robot_num; i++) {
            robot[i] = new Robot();
            robot[i].goods = scanf.nextInt();//Is robot i wether carrying a good?
            robot[i].x = scanf.nextInt();//robot i's position
            robot[i].y = scanf.nextInt();
            int sts = scanf.nextInt();//Is robot i workable?
        }
        for(int i = 0; i < 5; i ++) {
            boat[i] = new Boat();
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
        for(int zhen = 1; zhen <= 15000; zhen ++) { // read zhen1~15000 data from judge.exe
            int id = mainInstance.input();

            //output commands to judge.exe
            //  agents' action
            Random rand = new Random();
            for(int i = 0; i < robot_num; i ++)
                System.out.printf("move %d %d" + System.lineSeparator(), i, rand.nextInt(4) % 4);
            System.out.println("OK");
            System.out.flush();
        }
    }


    /**
     * Entity: Robot
     */
    class Robot {
        int x, y, goods;
        int status;
        int mbx, mby;

        public Robot() {}

        public Robot(int startX, int startY) {
            this.x = startX;
            this.y = startY;
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

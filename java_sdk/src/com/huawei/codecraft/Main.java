/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.huawei.codecraft;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

//import static jdk.nashorn.internal.objects.NativeRegExp.test;

/**
 * Main: the agents receiving environment input and sending actions of robots and ships
 *
 * @since 2024-02-05
 */
public class Main {
    //prepare containers for input
    private static final int n = 200;
    private static final int robot_num = 10;
    private static final int berth_num = 10;
    private static final int N = 210;
    private static final boolean logOn=false;//是否开启船装载日志

    private static final boolean testOn=false;

    private int money, boat_capacity, zhen_id;
    private String[] ch = new String[N];
    private Berth[] berth = new Berth[berth_num + 10];

    private int[][][] gds = new int[N][N][2];//额外记录生成时间，用于判断物品是否消失
    private Robot[] robots = new Robot[robot_num + 10];
    private Boat[] boats = new Boat[10];

    private LinkedList<int[]> accidentPoints = new LinkedList<>();//记录发生碰撞的点

    private PriorityQueue<int[]> availableBerths = new PriorityQueue<>(new Comparator<int[]>() {
        @Override
        public int compare(int[] o1, int[] o2) {
            return o1[1] - o2[1];
        }//按照泊位的开销从小到大排序，int[0]是泊位id，int[1]是泊位的开销
    });




    private int[][] initBlocksMap;//新版A星的地图信息，在init里初始化

    private FastAStar fastAStar;//新版A星寻路器实例

    private Node[][] searchArea;


    private  int[][] robotAdjacency = new int[robot_num][robot_num];//第0行表示机器人0和1、2、3、4、5、6、7、8、9是否相邻
    //第1行表示机器人1和2、3、4、5、6、7、8、9是否相邻，也就是说只有右上方的三角矩阵是有意义的，只记录机器人i和之后的机器人是否相邻
    private static final int goodsList_capacity =32;//物品数组的最大容量
    private CircularBuffer goodsList;
    private Random random;
    private FileOutputStream fos;
    private BFSPathSearch.berthInfo[][] ch2berth=new BFSPathSearch.berthInfo[210][210];//全局变量，ch2berth 中的每个点分配一个泊位信息，其中包括最近泊位的标识（berth_id）和该点到最近泊位的距离（distance）。

    /**
     *Main(agents) load the map data from pipe of System.in , within 5s
     */
    private void init(Scanner scanf) {
        //1.系统输入
        for(int i = 1; i <= n; i++) {  //输入地图
            ch[i] = scanf.nextLine();
        }
        for (int i = 0; i < berth_num; i++){ //10行泊位数据
            int id = scanf.nextInt();
            berth[id] = new Berth();
            berth[id].x = scanf.nextInt();
            berth[id].y = scanf.nextInt();//泊位左上坐标，4*4大小
            berth[id].transport_time = scanf.nextInt();//运输到虚拟点所需时间
            berth[id].loading_speed = scanf.nextInt();//每帧可以装载的物品数量
            //自定义变量
            berth[id].berth_id =id;
            berth[id].ship=false;
            berth[id].goods_num=0;
            berth[id].goods_value=0;
        }
        boat_capacity = scanf.nextInt();//船的容积
        scanf.nextLine();//整数后要吸取换行符



        //2.自定义信息
        if(logOn){
            try {//TODO 船的日志信息
                fos = new FileOutputStream("out/myoutput"+new Date().getTime()+".txt");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        random = new Random();
        goodsList=new CircularBuffer(goodsList_capacity);
        ArrayList<int[]> blockList = new ArrayList<>(); //获取地图上的障碍物信息
        for(int i = 1; i <= n; i++) {//ch第0行是null
            for(int j = 0; j < n; j++) {
                if(ch[i].charAt(j) == '#' || ch[i].charAt(j) == '*' ||ch[i].charAt(j)=='A') {
                    blockList.add(new int[]{i-1, j});
                }
            }
        }

        //3.地图数据存入寻路器
            //新版
        int[][] initBlocksMap = new int[n][n];//每行每列的障碍物是否存在的信息

        for(int i = 1; i <= n; i++) {//ch第0行是null
            for(int j = 0; j < n; j++) {
                if(ch[i].charAt(j) == '#' || ch[i].charAt(j) == '*' ) {
                    initBlocksMap[i-1][j] = 1;
                }
                else {
                    initBlocksMap[i-1][j] = 0;
                }
            }
        }

        this.fastAStar= new FastAStar(initBlocksMap);
        this.searchArea= fastAStar.getSearchArea();

        for(int i = 0; i < robot_num; i++) {//机器人初始化
            robots[i]=new Robot();
            robots[i].robot_id = i;
            robots[i].status=0;
            robots[i].sts=0;
            robots[i].goods=0;
        }

        for(int i = 0; i < 5; i ++) {//船舶初始化
            boats[i]=new Boat();
            boats[i].boat_id = i;
            boats[i].flag=0;
            boats[i].loadedGoods=0;
            boats[i].wait_zhen=15000;
        }

        BFSPathSearch.createch2berth(ch,ch2berth,berth);


        String OK = scanf.nextLine();//scanf.nextLine();
        System.out.println("OK");
        System.out.flush();
    }


    /**
     * Main(agents) agents receive the input data of every envirment zhen through pipe of System.in
     * @return id
     */
    private int input(Scanner scanf) {
        zhen_id = scanf.nextInt();//帧序号
        money = scanf.nextInt();//当前金钱数
        int num = scanf.nextInt();//新增货物的数量
        for (int i = 1; i <= num; i++) {
            int x = scanf.nextInt();//该货物的坐标
            int y = scanf.nextInt();
            int val = scanf.nextInt();//该货物的金额
            gds[x][y][0] = val;
            gds[x][y][1] = zhen_id;
            goodsList.add(new Agood(x,y,val));
        }
        scanf.nextLine();//吸取换行符
        for(int i = 0; i < robot_num; i++) {
            robots[i].goods = scanf.nextInt();//0 表示未携带物品 1 表示携带物品
            robots[i].x = scanf.nextInt();//机器人的坐标
            robots[i].y = scanf.nextInt();
            robots[i].sts = scanf.nextInt();//0 表示恢复状态 1 表示正常运行状态
            if(robots[i].sts==0){//如果发生碰撞，需要冻结状态并等待



                if (robots[i].waitTime==-1){
                    //第一次碰撞，要在全图标记碰撞点
                    accidentPoints.add(new int[]{robots[i].x,robots[i].y});

                    robots[i].FronzenStatus = robots[i].status;
                    robots[i].status=-1;
                    robots[i].waitTime = 20+random.nextInt(5);//random.nextInt( + 5);
                }
                else if (robots[i].waitTime>0) {
                robots[i].waitTime--;}
                else if (robots[i].waitTime==0){//碰撞时间内等待时间结束，这逻辑上是不可能的
                    robots[i].status = robots[i].FronzenStatus;
                    robots[i].waitTime=-1;
                }

            } else if (robots[i].sts==1) {


                //如果有冻结的状态，就解冻，如果没有，就不管

                if (robots[i].FronzenStatus!=-7){
                    robots[i].status = robots[i].FronzenStatus;
                    robots[i].waitTime=-1;
                    robots[i].FronzenStatus=-7;
                    this.robotAdjacency=getRobotAdjacency();
                    findPathBypassRobots(i,robots[i].destinationX,robots[i].destinationY);

                }
            }
        }
        this.robotAdjacency=getRobotAdjacency();


        for(int i = 0; i < 5; i ++) {
            boats[i].status = scanf.nextInt();//0:moving 1:workable 2:wating outside berth
            boats[i].pos = scanf.nextInt();//berth_id or -1(虚拟点)
        }
        scanf.nextLine();//吸取换行符
        String Ok= scanf.nextLine();
        return zhen_id;
    }

    /**
     * Interacting Process of Main(agents) and Judge.exe
     * @param args
     */
    public static void main(String[] args) {

        if(testOn)test();//测试用新建方法

        Main mainInstance = new Main();
        Scanner scanf = new Scanner(System.in);
//        //文件调试的代码
//        Scanner scanf = null;
//        try {
//            scanf = new Scanner(new File("maps\\debuginput1.txt"));
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
        mainInstance.init(scanf);
        //与判题器交互
        for(int zhen = 1; zhen <= 15000; zhen ++) { // read zhen1~15000 data from judge.exe
            scanf =new Scanner(System.in);
            int zhen_id = mainInstance.input(scanf);
            for(int i = 0; i < robot_num; i ++){
                mainInstance.robotMove(i);
            }
            for (int i=0;i<5;++i){
                //每一帧每艘船有一套相同的操作逻辑
//                mainInstance.boatMove3(i);
                mainInstance.boatMove2(i);
            }

//            mainInstance.robotAdjacency=null;
            System.out.println("OK");
            System.out.flush();
        }
    }

    private static void test() {
        try {

            Main mainInstance = new Main();
            Scanner scanf = new Scanner(new File("out/out.txt"));
            mainInstance.init(scanf);



            //与判题器交互

            for(int zhen = 1; zhen <= 15000; zhen ++) { // read zhen1~15000 data from judge.exe
                int zhen_id = mainInstance.input(scanf);
                /*
                -----------------------测试区-------------------------
                 */

//                mainInstance.robots[1].x=mainInstance.robots[0].x+1;
//                mainInstance.robots[1].y=mainInstance.robots[0].y;

//                mainInstance.getRobotAdjacency();

//                mainInstance.robots[0].status=1;
                for(int i = 0; i < robot_num; i ++){
//                    mainInstance.robots[i].wait(10);
                    mainInstance.robotMove(i);
                }
                for (int i=0;i<5;++i){
                    //每一帧每艘船有一套相同的操作逻辑
//                    mainInstance.boatMove(i);
//                    mainInstance.boatMove2(i);
//                    mainInstance.boatMove3(i);//智能来回
                    mainInstance.boatMove2(i);//2泊位来回
                }

                /*
                -----------------------测试区-------------------------
                 */
                System.out.println("OK");
                System.out.flush();
            }



        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

//        //文件调试的代码
//        Scanner scanf = null;
//        try {
//            scanf = new Scanner(new File("maps\\debuginput1.txt"));
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }

    }


    private void robotMove(int i){
        Robot r= robots[i];
//        goFirst(i);//i 大叫，我要先走了,相邻的先等一等


        if(r.status==-1){//碰撞状态:这部分处理放在input中了。
//            r.afterCollision0();
//            r.afterCollision2();
//            r.afterCollision1();

        }else if(r.status==0){//空闲状态
//            r.searchGds0();
            r.searchGds3();
//            r.searchGds2();
            if(r.mvPath!=null && !r.mvPath.isEmpty()){
                System.out.printf("move %d %d" + System.lineSeparator(), i, r.mvPath.poll());
            }


        }else if(r.status==-2){//避让状态
            r.wait(3);//随机等待1~10帧，或保持等待
            return;
        }
        //若机器人正在前往泊位的路上
        else if (r.status==2) {
            if(r.goods==1){
                if( ch[r.x+1].charAt(r.y) == 'B' || r.mvPath.isEmpty()) { //到达泊位
                    System.out.printf("pull %d" + System.lineSeparator(), i);
                    berth[i].goods_num += r.goods;//机器人对应的泊位货物数量+1
                    berth[i].goods_value+=r.val;
                    r.destinationX= -1;
                    r.destinationY=-1;
                    r.status = 0;
                }else {//还在路上
                    System.out.printf("move %d %d" + System.lineSeparator(), i, r.mvPath.poll());
                    //r.mvPath!=null && r.mvPath.size()>0
                }
            }else {
                r.status=0;
            }
        }else if(r.status==1){//前往货物的路上
            if (r.mvPath!=null && !r.mvPath.isEmpty()) {//还在路上
                System.out.printf("move %d %d" + System.lineSeparator(), i, r.mvPath.poll());
            } else { //到达货物？
                if (gds[r.x][r.y][0] != 0) {//货物仍在，就取货
                    gds[r.x][r.y][0]=0;//将这个物品标记为消失
                    System.out.printf("get %d" + System.lineSeparator(), i);
//                    r.searchBerth3();
                    r.searchBerth0();
                } else {
                    r.status=0;//if good has been taken by other robot
                }
            }

        }
    }

    /**
     *mainInstance use .goFirst(robot i) to let robot i go first and set i's adjacent followers' status avoid it.
     * @param i Robot id 0~9
     */
    public void goFirst(int i){
        int hasNeighbor=0;
        for (int j = i + 1; j < robot_num; ++j) {//邻居避让
            if(robotAdjacency[i][j]==1){
                robots[j].wait(3);//短避让状态
                hasNeighbor=1;
            }
        }
        if(hasNeighbor==1){//有邻居，如果在路上，那么就重新避人寻路
            if(robots[i].mvPath!=null&&!robots[i].mvPath.isEmpty()) findPathBypassRobots(i,robots[i].destinationX,robots[i].destinationY);

        }

    }
//
//    /**
//     * mainInstance use findPathBypassRobots(robot i) to find a path bypassing "robots after i" for i,and set it's mvPath
//     * bypass: 避开后续机器人的坐标以及周围1格内，避免双向碰撞
//     * @param i
//     */
//    public void findPathBypassRobots(int i, int endX , int endY) {
//        int[][] updatedBlocksArray = Arrays.copyOf(blockArray, blockArray.length+9*robot_num);
//        //初始化后半部分
//        for (int i1 = blockArray.length; i1 < updatedBlocksArray.length; i1++) {
//            updatedBlocksArray[i1]=new int[]{0,0};
//        }
//
//        for (int j = i + 1; j < robot_num; ++j) {
//            if (robotAdjacency[i][j] == 1) {
//                Robot otherRobot = robots[j];
//                int robotX = otherRobot.x;
//                int robotY = otherRobot.y;
//                for (int k = 0; k < 9; k++) {
//                    updatedBlocksArray[blockArray.length+ j+k*robot_num]= new int[]{robotX-1+k/3, robotY-1+k%3};
//                    //k/3 =0,0,0,1,1,1,2,2,2
//                    //k%3 =0,1,2,0,1,2,0,1,2
//                }
//
//            }
//        }
//        robots[i].mvPath = AStar.findPath(robots[i].x, robots[i].y, endX, endY, updatedBlocksArray);
//
//    }


    /**
     * 尝试重新寻路
     * 避开后续机器人的坐标以及周围1格内，避免双向碰撞
     * @param id :当前机器人id
     */
    public boolean findPathBypassRobots(int id, int endX , int endY) {
        if(this.robotAdjacency==null) this.robotAdjacency=getRobotAdjacency();

        LinkedList<int[]> addingBlocksList = new LinkedList<>();
//        int[][] addingBlocks = new int[9*robot_num][2];//一个机器人要占用9格，十个机器人就是90格

        //获取机器人id的后续相邻机器人的占用位置
        for (int j = id + 1; j < robot_num; ++j) {
            if (robotAdjacency[id][j] == 1) {//j是i的邻居机器人
                Robot otherRobot = robots[j];
                int consideringX = -1;
                int consideringY =-1;

                consideringX = otherRobot.x - 1;
                consideringY = otherRobot.y;
                if (consideringX>=0&&!searchArea[consideringX][consideringY].isBlock()) {//如果不是障碍
                    addingBlocksList.add(new int[]{consideringX, consideringY});
                }

                consideringX = otherRobot.x;
                consideringY = otherRobot.y-1;
                if (consideringY>=0 && !searchArea[consideringX][consideringY].isBlock()) {//如果不是障碍
                    addingBlocksList.add(new int[]{consideringX, consideringY});
                }

                consideringX = otherRobot.x+1;
                consideringY = otherRobot.y;
                if (consideringX<200 && !searchArea[consideringX][consideringY].isBlock()) {//如果不是障碍
                    addingBlocksList.add(new int[]{consideringX, consideringY});
                }

                consideringX = otherRobot.x;
                consideringY = otherRobot.y+1;
                if (consideringY <200 && !searchArea[consideringX][consideringY].isBlock()) {//如果不是障碍
                    addingBlocksList.add(new int[]{consideringX, consideringY});
                }


//                for (int k = 0; k < 9; k++) {//把九宫格内的非障碍节点添加到假设障碍数组中
//
//                    consideringX=otherRobot.x-1+k/3;//k/3 =0,0,0,1,1,1,2,2,2
//                    consideringY=otherRobot.y-1+k%3;//k%3 =0,1,2,0,1,2,0,1,2
//
//                    if (!searchArea[consideringX][consideringY].isBlock()) {//如果不是障碍
//                        addingBlocksList.add(new int[]{consideringX, consideringY});
//                    }
//
//                }

            }
        }
        //更新障碍物数组
        int[][] addingBlocksArray = addingBlocksList.toArray(new int[addingBlocksList.size()][2]);
        fastAStar.addBlocks(addingBlocksArray);
        fastAStar.deleteBlocks(new int[][]{{robots[id].x,robots[id].y}});//删除机器人自身的障碍
        //尝试避人寻路
        LinkedList<Integer> tryPath = fastAStar.findPathCommands(robots[id].x, robots[id].y, endX, endY);
        if (tryPath!=null&& !tryPath.isEmpty()){
            robots[id].mvPath = tryPath;
            //清除机器人占用的障碍物
            fastAStar.deleteBlocks(addingBlocksArray);
            return true;
        }else {
            //清除机器人占用的障碍物
            fastAStar.deleteBlocks(addingBlocksArray);
            return false;
        }



    }


    private void boatMove(int i){
        Boat boat=boats[i];
        if (zhen_id ==1){//第一帧,每一艘船都在虚拟点，前往泊位
            System.out.printf("ship %d %d" + System.lineSeparator(), i,i*2);
        }else if(boat.status==0 || boat.status==2){//非第一帧，船处于0:船移动中 2:泊位外等待
            //不执行操作
        }else if(boat.status==1){ //非第一帧，船处于等待指令状态
            if(boat.pos!=-1){//船只已经到达泊位
//                boat.loadGoods0();
                boat.loadGoods1();
            } else {//船只已经到达虚拟点
                boat.loadedGoods=0;//卸下货物
                System.out.printf("ship %d %d" + System.lineSeparator(), i,i*2+boat.flag);//返航
            }

        }
    }
    private void boatMove2(int i){
        Boat boat=boats[i];
        if (zhen_id ==1){//第一帧,每一艘船都在虚拟点，前往泊位
            System.out.printf("ship %d %d" + System.lineSeparator(), i,i*2);
        }else if(boat.status==0 || boat.status==2){//非第一帧，船处于0:船移动中 2:泊位外等待
            //不执行操作
        }else if(boat.status==1){ //非第一帧，船处于等待指令状态
            if(boat.pos==-1){//船只已经到达虚拟点
                System.out.printf("ship %d %d" + System.lineSeparator(), i,i*2+boat.flag);//返航
                boat.loadedGoods=0;
            } else {//船只已经到达泊位
                boat.loadGoods2();
            }
        }
    }

    /**
     * 智能运船：
     *
     *      * 每一帧，当有船位于虚拟点，为所有ship=flase的泊位计算优先度，并插入可用泊位。
     *      *      优先度计算：去某泊位的本趟货物价值/预估运输耗时=berth.goodValue/（transportTime*2+loadingTime），
     *      * loadTime=berth.goodNum/uploadSpeed
     *      * 依次取出最高优先度的第一个泊位，分配给位于虚拟点的船来ship，并设置对应泊位的ship信号为true
     *      * 当有船装好货物准备go时，设置泊位的ship信号为false表示泊位可以停靠。
     *      每一帧结束，清空availableBerths队列
     * @param i
     */
    private void boatMove3(int i){
        Boat boat=boats[i];

        if(boat.status==1){//船等待指令
            if(boat.pos==-1){//船只已经到达虚拟点
                //每帧队列会清空。如果队列非空，说明本帧有船到达虚拟点并且已经生成了泊位队列，后续船只直接poll就行，否则需要生成泊位队列
                if(availableBerths.isEmpty()){
                    generateAvailableBerths();
                }

                //依次取出最高优先度的第一个泊位，分配给位于虚拟点的船来ship，并设置对应泊位的ship信号为true
                if (!availableBerths.isEmpty()) {
                    int bestBerth = availableBerths.poll()[0];
                    berth[bestBerth].ship=true;
                    System.out.printf("ship %d %d" + System.lineSeparator(),i, bestBerth);
                } else {
                    System.out.printf("ship %d %d" + System.lineSeparator(), i,i*2);//生成泊位出问题了
                }
            } else {//船只已经到达泊位,应该装货，离开，然后设置泊位的ship信号为false表示泊位可以停靠。
                boat.loadGoods4();
//                    System.out.printf("go %d" + System.lineSeparator(), boat.boat_id);
//                    berth[boat.pos].ship=false;


            }

        }else {//船移动中或泊位外等待
            //不执行操作

        }
        if(i==4&&!availableBerths.isEmpty()) availableBerths.clear();//每一帧结束，清空availableBerths队列

    }

    /**
     * 生成泊位优先队列并且赋值给availableBerths
     */
    private void generateAvailableBerths(){
        for (int j = 0; j < berth_num; j++) {
            if(!berth[j].ship){//如果泊位没有船
                int loadingTime=berth[j].goods_num/berth[j].loading_speed;
                int cost = (2*berth[j].transport_time+loadingTime)/(berth[j].goods_value+1);
                availableBerths.add(new int[]{j,cost});
            }
        }
    }




    /**
     * use getRobotAdjacency() to get the robotAdjacency Matrix
     * @return int[][] indicates adjacency of robot i and its followers
     */
    public int[][] getRobotAdjacency() {
        for (int i = 0; i < robot_num; ++i) { //for robot i
            for (int j = i + 1; j < robot_num; ++j) { // check adjacency with i's followers
                int x_distance = Math.abs(robots[i].x - robots[j].x);
                int y_distance = Math.abs(robots[i].y - robots[j].y);
                if ((x_distance+y_distance)<=2) {
                    robotAdjacency[i][j] = 1;
                }
            }
        }
        return robotAdjacency;
    }



    /**
     * Entity: Robot
     */
    class Robot {
        int robot_id;
        int x, y, goods,val;
        int status; //-2避让状态 -1异常状态 0 空闲 1前往物品中 2拿到物品前往泊位中
        int sts;

        int waitTime=-1;

        int FronzenStatus = -7;
        LinkedList<Integer> mvPath;

        private int destinationX =-1;
        private int destinationY = -1;


        /**
         * 机器人随机等待一会
         * @param waitTimeRange
         */
        public void wait(int waitTimeRange){

                if(this.waitTime==-1) {//开始等待,冻结状态
                    this.FronzenStatus = this.status;
                    this.status=-2;//避让状态
                    this.waitTime = random.nextInt(1 + waitTimeRange);
                    this.waitTime--;

                }else if(this.waitTime>0) {
                    this.waitTime--;
                }else if(this.waitTime==0){
                    this.status = this.FronzenStatus;
                    this.FronzenStatus=-7;
                    this.waitTime=-1;
                }

        }

        //碰撞后处理只用改这里

        /**
         * 随机泊位
         */
        public void afterCollision0(){
            if(sts==0){
                return;
            }
            if(goods==0){
                status=0;
            }else{//机器人携带货物，则重新随机寻路一个泊位
                //随机一个泊位编号
                int berthId = random.nextInt(10);
//                mvPath=AStar.findPath(x, y, berth[berthId].x + 2, berth[berthId].y + 2,blockArray);
                //如果能找到路径，则前往泊位，否则跳过此帧
                if(mvPath.size()>0){
                    status=2;
                }
            }
        }

        public void afterCollision1(){
            if(goods==0){
                status=0;
            }else{//机器人携带货物，则寻找最近的泊位
                searchBerth1();
                //如果能找到路径，则前往泊位，否则跳过此帧
                if(mvPath.size()>0){
                    status=2;
                }
            }
        }

        /**
         * 回退随机步数
         */
        public void  afterCollision2(){
            if(this.status==-1||this.sts==0){//确认碰撞了
                //等待一个随机时间(20+1~10)，然后继续之前的行为
                this.wait(5);//先等一等，没好就再等一等


            }

        }

        //使用goodsList查找货物
        public void searchGds1(){
            long MaxWeight=-1;//权重定义为货物价值/距离机器人的距离
            LinkedList<Integer> BestPath=null;
            Agood BestGood=null;
            Agood NextBestGood=null;
            //搜索goodsList中最佳的货物
            for(int i=0;i<goodsList.size();++i){
                Agood temp_goods=goodsList.get(i);
                if(gds[temp_goods.x][temp_goods.y][0]<=0){//货物不存在或已被其他机器人锁定,则跳过
                    continue;
                }
                int distance = Math.abs(x - temp_goods.x) + Math.abs(y - temp_goods.y);
                if(distance!=0  &&  (temp_goods.val/distance)>MaxWeight){//更新权重最大的物品
                    MaxWeight=temp_goods.val/distance;
                    NextBestGood=BestGood;
                    BestGood=temp_goods;
                }
            }
            if(BestGood!=null){//才能试图寻找BestPath
                this.destinationX = BestGood.x;
                this.destinationY = BestGood.y;
//                BestPath = AStar.findPath(x, y, this.destinationX, this.destinationY,blockArray);
                if(BestPath.size() == 0 && NextBestGood != null){//TODO 找次好的货物
//                    BestPath = AStar.findPath(x, y, NextBestGood.x, NextBestGood.y,blockArray);
                    this.destinationX = NextBestGood.x;
                    this.destinationY = NextBestGood.y;
                }
                if (!BestPath.isEmpty()) {//只有当找到路径时才能保存到BestPath
                    //机器人已锁定此货物，别的机器人不能再取
                    gds[destinationX][destinationY][0]=-1;
                    val=BestGood.val;
                    mvPath=BestPath;
                    status=1;
                }
            }
        }



        //基于高级避障的查找货物
        public void searchGds2(){
            long MaxWeight=-1;//权重定义为货物价值/距离机器人的距离
            LinkedList<Integer> BestPath=null;
            Agood BestGood=null;
            Agood NextBestGood=null;
            //搜索goodsList中最佳的货物
            for(int i=0;i<goodsList.size();++i){
                Agood temp_goods=goodsList.get(i);
                if(gds[temp_goods.x][temp_goods.y][0]<=0){//货物不存在或已被其他机器人锁定,则跳过
                    continue;
                }
                int distance = Math.abs(x - temp_goods.x) + Math.abs(y - temp_goods.y);
                if(distance!=0  &&  (temp_goods.val/distance)>MaxWeight){//更新权重最大的物品
                    MaxWeight=temp_goods.val/distance;
                    NextBestGood=BestGood;
                    BestGood=temp_goods;
                }
            }
            if(BestGood!=null){//才能试图寻找BestPath
//                BestPath = AStar.findPath(x, y, BestGood.x, BestGood.y,blockArray);
//                BestPath = findPathAvoidingRobots(x, y, BestGood.x, BestGood.y, robot_id, blockArray,robots);
                if(BestPath.size() == 0 && NextBestGood != null){//TODO 找次好的货物
//                    BestPath = AStar.findPath(x, y, NextBestGood.x, NextBestGood.y,blockArray);
                    BestGood=NextBestGood;
                }
                if (!BestPath.isEmpty()) {//只有当找到路径时才能保存到BestPath
                    //机器人已锁定此货物，别的机器人不能再取
                    val=BestGood.val;
                    gds[BestGood.x][BestGood.y][0]=-1;
                    mvPath=BestPath;
                    status=1;
                }
            }
        }

        /**
         * robot use searchGds3() to call BFS.findMaxValueTarget() to find the best good's x and y,
         * to use this point to try to get a path commands , to set in robot's mvpath.
         */
        public void searchGds3(){
            BFSPathSearch.Point point = BFSPathSearch.findMaxValueTarget(ch, gds, x, y,zhen_id);
            if(point!=null){
//                LinkedList<Integer> path = AStar.findPath(x, y, point.x, point.y, blockArray);
                LinkedList<Integer> path = fastAStar.findPathCommands(x, y, point.x, point.y);
                if(!path.isEmpty()){
                    this.destinationX = point.x;
                    this.destinationY = point.y;
                    val=point.gdsValue;
                    gds[point.x][point.y][0]=-1;
                    gds[point.x][point.y][1]=-1000;
                    mvPath=path;
                    status=1;
                }
            }
        }


        public void searchBerth0(){
            status=2;
            this.destinationX = berth[robot_id].x + 2;
            this.destinationY = berth[robot_id].y + 2;
            LinkedList<Integer> path = fastAStar.findPathCommands(x, y, destinationX, destinationY);
            if (!path.isEmpty()) {
                mvPath = path;
            }
        }

        public void searchBerth1(){//曼哈顿距离找最近的泊位
            status=2;
            int MinDistance=10000;
            Berth MinBerth=null;
            for(int i=0;i<berth_num;++i){
                int distance = Math.abs(x - berth[i].x) + Math.abs(y - berth[i].y);
                if(distance<MinDistance){
                    MinDistance=distance;
                    MinBerth=berth[i];
                }
            }
            if (MinBerth != null) {
                this.destinationX = MinBerth.x + 2;
                this.destinationY = MinBerth.y + 2;
                mvPath = fastAStar.findPathCommands(x, y, MinBerth.x + 2, MinBerth.y + 2);
//                mvPath= AStar.findPath(x, y, MinBerth.x, MinBerth.y,blockArray);
            }
        }


        public void searchBerth2(){//BFS找最近的泊位
            status=2;

            BFSPathSearch.Point point = BFSPathSearch.findMinBerth(ch, x, y);
            if (point != null) {
                this.destinationX= point.x;
                this.destinationY = point.y;
                mvPath = fastAStar.findPathCommands(x, y, point.x, point.y);
//                mvPath= AStar.findPath(x, y, point.x, point.y,blockArray);
            }
        }

        /**
         *根据ch2berth直接取出当前位置最近的泊位中心点，然后前往中心点。
         */
        public void searchBerth3(){//BFS找最近的泊位
            status=2;
            BFSPathSearch.berthInfo berthInfo = ch2berth[x][y];
            if (berthInfo.berth_id != -1) {
                this.destinationX= berth[berthInfo.berth_id].x+2;
                this.destinationY= berth[berthInfo.berth_id].y+2;
//                mvPath= AStar.findPath(x, y, berth[berthInfo.berth_id].x+2, berth[berthInfo.berth_id].y+2,blockArray);
                mvPath= fastAStar.findPathCommands(x, y, this.destinationX, this.destinationY);
            }
        }

    }



    /**
     * Entity: Berth
     */
    class Berth {
        int berth_id;
        int x;
        int y;
        int transport_time;
        int loading_speed;

        int goods_num;
        int goods_value;
        public boolean ship;
    }

    /**
     * Entity: Boat
     */
    class Boat {
        int boat_id;
        int pos;
        int status;
        int loadedGoods;

        int loadedValue=0;
        int flag;
        double wait_zhen;

        public void loadGoods0(){
                berth[pos].ship=true;//泊位占用信号
                //每一帧试图装货:装货量=最小值(船仓余量，泊位货物数量，装货速度)
                int boatAllowance = boat_capacity - loadedGoods;
                int loadingGoods = Math.min(boatAllowance, Math.min( berth[pos].goods_num, berth[pos].loading_speed));
                loadedGoods+=loadingGoods;
                berth[pos].goods_num-=loadedGoods;
                //装完后发现装满了或者没货物了，就离开
                if (loadedGoods >= boat_capacity || berth[pos].goods_num<=0){
                    System.out.printf("go %d" + System.lineSeparator(), boat_id);
                    flag^=1;
                    berth[pos].ship=false;
                }
        }

        public void loadGoods1(){
            if(wait_zhen==15000){//约定wait_zhen==15000为开始装货物的信号
                berth[pos].ship=true;//泊位占用信号
                //预估装货时间
                float MaxLoadNum=Math.min(berth[pos].goods_num,boat_capacity);//最大可装载量
                wait_zhen = zhen_id+Math.ceil(MaxLoadNum/berth[pos].loading_speed);
                berth[pos].goods_num-=MaxLoadNum;
            }
            if(wait_zhen<zhen_id){//该运货了
                System.out.printf("go %d" + System.lineSeparator(), boat_id);
                flag^=1;
                berth[pos].ship=false;
                loadedGoods=0;
                wait_zhen=15000;//到下次可以装货的时候执行上面的代码

            }
        }

        public void loadGoods2(){
            if(wait_zhen==15000){//约定wait_zhen==15000为开始装货物的信号
                berth[pos].ship=true;//泊位占用信号
                //预估装货时间
                float MaxLoadNum=Math.min(berth[pos].goods_num,boat_capacity);//最大可装载量
                wait_zhen = zhen_id+Math.ceil(MaxLoadNum/berth[pos].loading_speed);
                berth[pos].goods_num-=MaxLoadNum;
                loadedGoods+= (int) MaxLoadNum;
            }
            if(wait_zhen<zhen_id){//该运货了
                if(flag==0){
                    flag^=1;
                    System.out.printf("ship %d %d" + System.lineSeparator(), boat_id,boat_id*2+flag);
                }else{
                    flag^=1;
                    System.out.printf("go %d" + System.lineSeparator(), boat_id);

                    if(logOn){
                        // 要写入文件的内容
                        String content=zhen_id+"\n";
                        content +=String.format("berthid：%d loadspeed：%d 剩余货物数量：%d 累积货物价值：%d \n",
                                pos,berth[pos].loading_speed,berth[pos].goods_num,berth[pos].goods_value);
                        content +=String.format("boatid：%d transport_time：%d boat_capacity：%d 运输量 %d\n",
                                boat_id,berth[pos].transport_time,boat_capacity,loadedGoods);
                        int sumnum = 0,sumval = 0;
                        for(int i=0;i<berth_num;++i){
                            sumnum+=berth[i].goods_num;
                            sumval+=berth[i].goods_value;
                        }
                        content+="总计剩余货物:"+sumnum+" 总计价值:"+sumval+"\n";
                        try{
                            // 将字符串转换为字节数组
                            byte[] bytesArray = content.getBytes();
                            // 写入内容到文件
                            fos.write(bytesArray);
                            fos.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
                berth[pos].ship=false;
                wait_zhen=15000;//到下次可以装货的时候执行上面的代码

            }
        }

        public void loadGoods3(){
            if(wait_zhen==15000){//约定wait_zhen==15000为开始装货物的信号
                berth[pos].ship=true;//泊位占用信号
                //预估装货时间
                float MaxLoadNum=Math.min(berth[pos].goods_num,boat_capacity);//最大可装载量
                wait_zhen = zhen_id+Math.ceil(MaxLoadNum/berth[pos].loading_speed);
                berth[pos].goods_num-=MaxLoadNum;
                loadedGoods+= (int) MaxLoadNum;
            }
            if( wait_zhen<zhen_id){//该运货了
                if(loadedGoods>=boat_capacity){ //容量满了就运
                    System.out.printf("go %d" + System.lineSeparator(), boat_id);
                }else {
                    flag^=1;
                    System.out.printf("ship %d %d" + System.lineSeparator(), boat_id,boat_id*2+flag);
                }
                berth[pos].ship=false;
                wait_zhen=15000;//到下次可以装货的时候执行上面的代码
            }
        }

        /**
         * 传入装载货船的id，返回是否不能再装货，应该离开
         * 装货，离开，ship=flase
         */
        public boolean loadGoods4(){
            if(wait_zhen==15000){//约定wait_zhen==15000为开始装货物的信号
                berth[pos].ship=true;//泊位占用信号
                //预估装货时间
                float MaxLoadNum=Math.min(berth[pos].goods_num,boat_capacity);//最大可装载量
                wait_zhen = zhen_id+Math.ceil(MaxLoadNum/berth[pos].loading_speed);
                berth[pos].goods_num-=MaxLoadNum;
                //预估装载的货物价值
                this.loadedValue= berth[pos].goods_value;
                berth[pos].goods_value=0;

                loadedGoods+= (int) MaxLoadNum;

                return true;


            }
            else if (wait_zhen<zhen_id){//该运货了
                wait_zhen=15000;//到下次可以装货的时候执行上面的代码


                if(loadedGoods>=boat_capacity){ //容量满了就运
                    System.out.printf("go %d" + System.lineSeparator(), boat_id);
                }else {//去另一个泊位
                    generateAvailableBerths();
                    if(!availableBerths.isEmpty()) {
                        System.out.printf("ship %d %d" + System.lineSeparator(), boat_id, availableBerths.poll()[0]);
                    }else {
                        System.out.printf("ship %d %d" + System.lineSeparator(), boat_id,boat_id*2+flag);
                        flag^=1;
                    }
                }

                berth[pos].ship=false;
                return false;

            }
            else return false;

        }
    }

}
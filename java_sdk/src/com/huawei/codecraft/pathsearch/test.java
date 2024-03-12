package com.huawei.codecraft.pathsearch;


import com.huawei.codecraft.AStarPathSearch;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

public class test {

    @Test
    public void jpstest() {
        // 设置起点和终点
        int startX = 89, startY = 55;
        int endX = 4, endY = 162;

        Scanner scanf = null;
        try {
            File file = new File("C:\\Users\\21232\\Desktop\\HuaweiCup\\HUAWEICup\\maps\\map1.txt");
            scanf = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        String[] ch=init(scanf);


        long startTime = System.currentTimeMillis();
        //TODO 创建JPS实例并寻找路径
//        JpsPathSearch ps = new JpsPathSearch(grid);
        //使用A*
        AStarPathSearch ps = new AStarPathSearch(ch,1,1,200,200);
        //使用dfs
//        dfsPathSearch ps = new dfsPathSearch(grid);

        LinkedList<Integer> path = ps.findPath(startX, startY, endX, endY);
        path = ps.findPath(startX, startY, endX, endY);
        long endTime = System.currentTimeMillis();
        // 打印路径
        System.out.println("执行时间："+(endTime-startTime)+"ms");
        System.out.println("距离为："+path.size());
        System.out.println("Path from (" + startX + ", " + startY + ") to (" + endX + ", " + endY + "):");
        if (path.isEmpty()) {
            System.out.println("No path found.");
        } else {
            for (Integer p : path) {
                System.out.println("p = " + p);
            }
        }
    }

    private String[] init(Scanner scanf) {
        String[] ch=new String[210];
        for(int i = 1; i <= 200; i++) {
            ch[i] = scanf.nextLine();
        }
        System.out.println("OK");
        System.out.flush();
        return ch;
    }
}

package com.huawei.codecraft;

/**
 * Nodev2 is map unit of a possible path

 */
public class Nodev1 implements Comparable<Nodev1>{
    int x, y;
    Nodev1 parent;
    int g, h,f; // g: 起点到当前节点的成本, h: 当前节点到终点的估算成本

    public Nodev1(int x, int y, Nodev1 parent, int g, int h) {
        this.x = x;
        this.y = y;
        this.parent = parent;
        this.g = g;
        this.h = h;
    }

    public Nodev1(int x, int y, Nodev1 parent) {
        this.x = x;
        this.y = y;
        this.parent = parent;

    }


    @Override
    public int compareTo(Nodev1 o) {
        return Integer.compare(this.g + this.h, o.g + o.h);
    }
    //node在优先队列中的大小比较是通过这行代码实现的
}

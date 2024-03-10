package com.huawei.codecraft.pathsearch;

public class Node implements Comparable<Node>{
    int x, y;
    Node parent;
    int g, h; // g: 起点到当前节点的成本, h: 当前节点到终点的估算成本

    public Node(int x, int y, Node parent, int g, int h) {
        this.x = x;
        this.y = y;
        this.parent = parent;
        this.g = g;
        this.h = h;
    }

    public Node(int x, int y, Node parent) {
        this.x = x;
        this.y = y;
        this.parent = parent;
        this.g = g;
        this.h = h;
    }


    @Override
    public int compareTo(Node o) {
        return Integer.compare(this.g + this.h, o.g + o.h);
    }
}

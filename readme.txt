开发提示：
    调试时，可以开启logOn 和testOn。跑分前必须关闭logOn和testOn。
    容易拥堵的图适合固定泊位，不拥堵的图适合地图绑定泊位。

问题日志：
3.23测试
    map3.12 -我的机器上
        机器人固定泊位，船智能来回25w7k
        机器人BFS择近，船智能来回24w4k
        机器人固定泊位，船2泊位来回25w7k
        机器人BFS择近，船2泊位来回24w8k
        ？检查船2智能来回是否正常。

3.22
    ？泊位堆积货物提升了，船运力有点跟不上了。
        ！船在虚拟点时取出最佳可用泊位前往。最佳可用泊位=max本趟货物价值/预估运输耗时=berth.goodValue/（transportTime*2+loadingTime）
        loadTime=berth.goodNum/uploadSpeed *map-3.7
            x 测试：boatMove3()分巨低，还在研究问题出在哪里。
            x 805帧，0船到达泊位，装货两次，806帧结束时便已经go虚拟点。823帧，1船到达泊位3，装货两次，825帧离开
            1606，0船到达虚拟点，

        ！船装完一个泊位的货物，load返回false时，可以考虑前往另一个泊位而非虚拟点，在此时计算泊位成本，选择最小的去。

   
 /**
     * 智能运船：
     * 维护一个全局变量优先队列：可用泊位，用优先度排序
     * 每一帧，当有船位于虚拟点，重置全局变量-泊位队列，为所有ship=flase的泊位计算优先度，并插入可用泊位。
     *      优先度计算：去某泊位的本趟货物价值/预估运输耗时=berth.goodValue/（transportTime*2+loadingTime），
     * loadTime=berth.goodNum/uploadSpeed
     * 依次取出最高优先度的第一个泊位，分配给位于虚拟点的船来ship，并设置对应泊位的ship信号为true
     * 当有船装好货物准备go时，设置泊位的ship信号为false表示泊位可以停靠。
     *
     */

3.21 zxr
*OnlineMap -14w，515帧开始卡得要死，卡顿往往伴随“碰撞，get货，pull货”   *map-3.13 -18w，无明显问题
    x 巨卡，卡顿往往伴随“碰撞，get货，pull货”，可能是由于searchGoods3不再是遍历goodList，而是广度优先。
        x 换用新版A星:寻路货物和泊位时改用新版A星，避人寻路没用 *mapOnline - 15w *map-3.13 - 18w
        x 避人时也用fastA星* *15w,有时会连环堵塞，不知道是啥原因，线上版本是7万分
            x fastA星避人方法有问题，会导致连环碰撞。为什么？244 4 撞，已解决
        ! getRobotAdjacency()位置优化
        ! 历史bestGood统计，能否加快搜索？

    x map-3.10, 440帧，在狭窄路段，机器人0和3发生碰撞并且无法解开。
        x 容易拥堵的图适合固定泊位，不拥堵的图适合地图绑定泊位。











3.20 zxr
x 机器人到泊位放下货物时会卡一下-导致20%~50%的移速减少：优化寻物计算量
    x 修改A*算法的blockmap逻辑，1.增加findpath方法不需要传入blockarr，2.新增setblockarr设置地图
    x 修改船运输算法

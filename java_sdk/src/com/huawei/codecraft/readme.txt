开发提示：
    Main.test()内部搭建好了测试框架，可以在main首行取消注释.test()来运行

问题日志：
    机器人到泊位放下货物时会卡一下-导致20%~50%的移速减少：优化寻物计算量
    修改A*算法的blockmap逻辑，1.增加findpath方法不需要传入blockarr，2.新增setblockarr设置地图
    修改船运输算法

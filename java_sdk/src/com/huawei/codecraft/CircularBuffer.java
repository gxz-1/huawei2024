package com.huawei.codecraft;

//环形数组,用于存储物品信息
public class CircularBuffer {
    private Agood[] buffer;
    private int head = 0;
    private int tail = 0;
    private int size = 0;
    private int capacity;

    public CircularBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = new Agood[capacity];
    }

    public void add(Agood element) {
        if (size == capacity) {
            head = (head + 1) % capacity; // 移除最早的元素
        } else {
            size++;
        }
        buffer[tail] = new Agood(element.x,element.y,element.val);
        tail = (tail + 1) % capacity;
    }

    public Agood get(int index) {
        return buffer[(head + index) % capacity];
    }

    public int size() {
        return size;
    }
}

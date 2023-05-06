package com.homo.core.utils.lang;

import java.util.BitSet;

public class BitMap {
    private byte[] bytes;
    private int initSize;

    public BitMap(int size) {
        if (size <= 0) {
            return;
        }
        initSize = size / 8 + 1;
        bytes = new byte[initSize];
    }

    public void set(int number) {
        //相当于对一个数字进行右移动3位，相当于除以8
        int index = number >> 3;
        //相当于 number % 8 获取到byte[index]的位置
        int position = number & 0x07;
        //进行|或运算  参加运算的两个对象只要有一个为1，其值为1。 1byte = 8bit
        bytes[index] |= 1 << position;
    }


    public boolean contain(int number) {
        int index = number >> 3;
        int position = number & 0x07;
        return (bytes[index] & (1 << position)) != 0;
    }

    public static void main(String[] args) {
        BitMap myBitMap = new BitMap(32);
        myBitMap.set(30);
        myBitMap.set(13);
        myBitMap.set(24);
        System.out.println(myBitMap.contain(30));
        System.out.println(myBitMap.contain(13));
        System.out.println(myBitMap.contain(24));
        System.out.println(myBitMap.contain(2));
        BitSet bitSet = new BitSet(32);
        bitSet.set(30);
        bitSet.set(13);
        bitSet.set(24);
        System.out.println(bitSet.get(30));
        System.out.println(bitSet.get(13));
        System.out.println(bitSet.get(24));
        System.out.println(bitSet.get(2));
    }
}

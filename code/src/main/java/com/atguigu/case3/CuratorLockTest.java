package com.atguigu.case3;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMultiLock;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.Collections;

public class CuratorLockTest {

    public static void main(String[] args) {

        //创建分布式锁1
        //（客户端，上锁路径）
        final InterProcessMultiLock locks1 = new InterProcessMultiLock(getCuratorFramework(), Collections.singletonList("/locks"));

        //创建分布式锁2
        final InterProcessMultiLock locks2 = new InterProcessMultiLock(getCuratorFramework(), Collections.singletonList("/locks"));

        new Thread(new Runnable() {
            public void run() {
                try {
                    //如果获取到锁
                    locks1.acquire();
                    System.out.println("线程1 获取到锁");

                    //第二次获取
                    locks1.acquire();
                    System.out.println("线程1 再次获得到锁");

                    Thread.sleep(5*1000);

                    //释放锁
                    locks1.release();
                    System.out.println("线程1 释放锁");

                    locks1.release();
                    System.out.println("线程1 再次释放锁");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                try {
                    //如果获取到锁
                    locks2.acquire();
                    System.out.println("线程2 获取到锁");

                    //第二次获取
                    locks2.acquire();
                    System.out.println("线程2 再次获得到锁");

                    Thread.sleep(5*1000);

                    //释放锁
                    locks2.release();
                    System.out.println("线程2 释放锁");

                    locks2.release();
                    System.out.println("线程2 再次释放锁");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static CuratorFramework getCuratorFramework() {

        //连接失败时多少秒重新连接和尝试连接几次
        ExponentialBackoffRetry policy = new ExponentialBackoffRetry(3000, 3);

        //通过自带的工厂类连接到主机,连接超时时间，操作超时时间，连接失败重连操作
        CuratorFramework client = CuratorFrameworkFactory.builder().connectString("192.168.28.138:2181").connectionTimeoutMs(2000).sessionTimeoutMs(2000).retryPolicy(policy).build();

        client.start();
        System.out.println("zookeeper启动成功");

        return client;
    }
}

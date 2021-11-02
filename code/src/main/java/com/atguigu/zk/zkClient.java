package com.atguigu.zk;

import org.apache.log4j.Logger;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class zkClient {

    //服务器地址，如果没有配置映射则使用ip地址，多个ip使用逗号分开不能有空格
    private String connectString = "192.168.28.138:2181";
    //服务超时时间
    private int sessionTimeout = 10000;

    //全局变量
    private ZooKeeper zkClient;


    //初始化连接,每次操作都会执行
    @Before
    public void init() throws IOException {
        //服务地址，超时时间，监听器
       zkClient = new ZooKeeper(connectString,sessionTimeout, new Watcher() {
            public void process(WatchedEvent watchedEvent) {
                //可以自己new一个监听器，如果为true则使用创建时的匿名监听器
                System.out.println("-------------------------------");
                List<String> children = null;
                try {
                    children = zkClient.getChildren("/", true);
                    for (String child: children) {
                        System.out.println(child);
                    }
                    System.out.println("1");
                    System.out.println("----------------------------------");
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //创建节点
    @Test
    public void create() throws KeeperException, InterruptedException {
        //create中的参数为（节点地址,节点内容值字节型,访问权限，节点类型）
        String nodeCreated = zkClient.create("/atguigu", "ss.avi".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    //监控点
    @Test
    public void getChildren() throws KeeperException, InterruptedException {
/*        List<String> children = zkClient.getChildren("/", true);

        for (String chlid: children) {
            System.out.println(chlid);
        }
        System.out.println("2");*/
        //延时
        Thread.sleep(Long.MAX_VALUE);
    }

    //判断某个节点是否存在
    @Test
    public void exist() throws KeeperException, InterruptedException {
        Stat stat = zkClient.exists("/atguigu", false);

        System.out.println(stat==null?"not exist":"exist");
    }
}

package com.atguigu.case1;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DistributeClient {

    private String connectString = "192.168.28.138:2181";

    private int sessionTimeout = 10000;

    private ZooKeeper zk;

    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {

        DistributeClient client = new DistributeClient();
        //1.获取zk连接
        client.getConnect();
        //2.监听sever下面子节点的增加和删除
        client.getServerList();
        //3.业务逻辑
        client.business();
    }

    private void business() throws InterruptedException {
        Thread.sleep(Long.MAX_VALUE);
    }

    //注册
    private void getServerList() throws KeeperException, InterruptedException {
        List<String> children = zk.getChildren("/servers", true);

        //遍历每一个子节点带上名字放入数组
        ArrayList<String> servers = new ArrayList<String>();
        for (String child :children) {
            byte[] data = zk.getData("/servers/" + child, false, null);
            servers.add(new String(data));
        }

        //打印
        System.out.println(servers);
    }

    //连接
    private void getConnect() throws IOException {
        zk = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
            public void process(WatchedEvent watchedEvent) {
                try {
                    getServerList();
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

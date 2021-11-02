package com.atguigu.case2;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class DistributedLock {

    private final String connectString = "192.168.28.138:2181";
    private final int sessionTimeout = 1000000;
    private final ZooKeeper zk;

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private CountDownLatch waitLatch = new CountDownLatch(1);

    private String waitPat;

    private String currentMode;

    public DistributedLock() throws IOException, InterruptedException, KeeperException {

        //获取链接
        zk = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
            public void process(WatchedEvent watchedEvent) {
                //connectLatch 如果连接zk 可以释放
                if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                    //countDowLatch开始-1
                    countDownLatch.countDown();
                }
                //waitLatch 需要释放
                if (watchedEvent.getType() == Event.EventType.NodeDeleted && watchedEvent.getPath().equals(waitPat));
                waitLatch.countDown();
            }
        });
        //等待zk正常连接,往下走程序
        countDownLatch.await();

        //判断根节点是否存在
        Stat stat = zk.exists("/locks", false);

        if (stat == null ){
            //创建一个根节点
            zk.create("/locks","locks".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
    }

    //对zk加锁
    public void zklock() {
        //创建对应的零时带序号节点
        try {
            currentMode = zk.create("/locks/" + "seq-", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

            //判断创建节点是否是最小节点，如果是获取到锁，如果不是，监听序号上一个节点
            List<String> children = zk.getChildren("/locks", false);

            //如果children 只有一个值，那就直接获取锁，如果有多个节点，需要判断谁小
            if (children.size() == 1) {
                return;
            }else {
                //重新排序
                Collections.sort(children);

                // 获取节点名称 seq-000000000
                String thisNode = currentMode.substring("/locks/".length());
                //通过seq-000000获取该节点在children集合的位置
                int index = children.indexOf(thisNode);

                //判断
                if (index == -1) {
                    System.out.println("数据异常");
                }else if (index == 0) {
                    //只存在一个节点，可以使用了
                    return;
                }else  {
                    //需要监听，他前一个节点的变化
                    waitPat = "/locks/"+children.get(index-1);
                    zk.getData(waitPat,true,null);

                    //等待监听
                    waitLatch.await();

                    return;
                }
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    //解锁
    public void unZKLock() throws KeeperException, InterruptedException {

        //删除节点
        zk.delete(currentMode,-1);
    }
}

package com.linj.javabased.demo;

import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @Author: LinJ
 * @Description: 锁的种类与使用
 * @Date: Created in 11:26 2019/10/15
 */
public class MyLockTest implements Runnable{

//    public synchronized void get() {
//        System.out.println("2 enter thread name-->" + Thread.currentThread().getName());
//        //reentrantLock.lock();
//        System.out.println("3 get thread name-->" + Thread.currentThread().getName());
//        set();
//        //reentrantLock.unlock();
//        System.out.println("5 leave run thread name-->" + Thread.currentThread().getName());
//    }
//
//    public synchronized void set() {
//        //reentrantLock.lock();
//        System.out.println("4 set thread name-->" + Thread.currentThread().getName());
//        //reentrantLock.unlock();
//    }

    // get()方法中顺利进入了set()方法，说明synchronized的确是可重入锁。
    // thread-0先进入get方法体，这个时候thread-1、thread-2、thread-3等待进入，但当
    // thread-0离开时，thread-2却先进入了方法体，没有按照thread-1、thread-2、thread-3
    // 的顺序进入get方法体，说明sychronized的确是非公平锁。
    // 而且在一个线程进入get方法体后，其他线程只能等待，无法同时进入，
    // 验证了synchronized是独占锁。

    // 非公平锁
//    private ReentrantLock reentrantLock = new ReentrantLock();
    // 公平锁
    private ReentrantLock reentrantLock = new ReentrantLock(true);

    public void get() {
        System.out.println("2 enter thread name-->" + Thread.currentThread().getName());
        reentrantLock.lock();
        System.out.println("3 get thread name-->" + Thread.currentThread().getName());
        set();
        reentrantLock.unlock();
        System.out.println("5 leave run thread name-->" + Thread.currentThread().getName());
    }

    public void set() {
        reentrantLock.lock();
        System.out.println("4 set thread name-->" + Thread.currentThread().getName());
        reentrantLock.unlock();
    }

    // 的确如其名，可重入锁，当然默认的确是非公平锁。thread-0持有锁期间，thread-1等待拥有锁，
    // 当thread-0释放锁时thread-3先获取到锁，并非按照先后顺序获取锁的。
    // 将其构造为公平锁，看看运行结果是否符合预期。查看源码构造公平锁很简单，
    // 只要在构造器传入boolean值true即可。



    @Override
    public void run() {
        System.out.println("1 run thread name-->" + Thread.currentThread().getName());
        get();
    }

//    public static void main(String[] args) {
//        MyLockTest test = new MyLockTest();
//        for (int i = 0; i < 10; i++) {
//            new Thread(test, "thread-" + i).start();
//        }
//    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Cache.put("key", new String(Thread.currentThread().getName() + " joke"));
                }
            }, "threadW-" + i).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println(Cache.get("key"));
                }
            }, "threadR-" + i).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Cache.clear();
                }
            }, "threadC-" + i).start();
        }
    }
    // 读写锁的性能都会比排他锁要好，因为大多数场景读是多于写的。
    // 在读多于写的情况下，读写锁能够提供比排它锁更好的并发性和吞吐量。
    // Java并发包提供读写锁的实现是ReentrantReadWriteLock。
    //
    // 特性	      说明
    // 公平性选择	  支持非公平(默认)和公平的锁获取方式，吞吐量还是非公平优于公平
    // 重进入	  该锁支持重进入，以读写线程为例：读线程在获取了读锁之后，能够再次获取读锁。
    //            而写线程在获取了写锁之后能够再次获取写锁，同时也可以获取读锁
    // 锁降级	  遵循获取写锁、获取读锁再释放写锁的次序，写锁能够降级成为读锁
}

class Cache {
    static Map<String, Object> map = new HashMap<String, Object>();
    static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    static Lock r = rwl.readLock();
    static Lock w = rwl.writeLock();

    // 获取一个key对应的value
    public static final Object get(String key) {
        r.lock();
        try {
            System.out.println("get " + Thread.currentThread().getName());
            return map.get(key);
        } finally {
            r.unlock();
        }
    }

    // 设置key对应的value，并返回旧有的value
    public static final Object put(String key, Object value) {
        w.lock();
        try {
            System.out.println("put " + Thread.currentThread().getName());
            return map.put(key, value);
        } finally {
            w.unlock();
        }
    }

    // 清空所有的内容
    public static final void clear() {
        w.lock();
        try {
            System.out.println("clear " + Thread.currentThread().getName());
            map.clear();
        } finally {
            w.unlock();
        }
    }


}

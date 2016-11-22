package com.moilioncircle.redis.replicator;

import com.moilioncircle.redis.replicator.rdb.RdbFilter;
import com.moilioncircle.redis.replicator.rdb.RdbListener;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyStringValueString;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;

/**
 * Created by leon on 11/21/16.
 */
public class RedisRdbReplicatorTest {
    @Test
    public void testChecksumV7() throws IOException, InterruptedException {
        Replicator redisReplicator = new RedisReplicator(
                RedisSocketReplicatorTest.class.getClassLoader().getResourceAsStream("dumpV7.rdb"),
                Configuration.defaultSetting());
        final AtomicInteger acc = new AtomicInteger(0);
        final AtomicLong atomicChecksum = new AtomicLong(0);
        redisReplicator.addRdbListener(new RdbListener.Adaptor() {
            @Override
            public void handle(Replicator replicator, KeyValuePair<?> kv) {
                acc.incrementAndGet();
            }

            @Override
            public void postFullSync(Replicator replicator, long checksum) {
                super.postFullSync(replicator, checksum);
                atomicChecksum.compareAndSet(0, checksum);
                assertEquals(19, acc.get());
                assertEquals(6576517133597126869L, atomicChecksum.get());
            }
        });
        redisReplicator.open();
    }

    @Test
    public void testChecksumV6() throws IOException, InterruptedException {
        Replicator redisReplicator = new RedisReplicator(
                RedisSocketReplicatorTest.class.getClassLoader().getResourceAsStream("dumpV6.rdb"),
                Configuration.defaultSetting());
        final AtomicInteger acc = new AtomicInteger(0);
        final AtomicLong atomicChecksum = new AtomicLong(0);
        redisReplicator.addRdbListener(new RdbListener.Adaptor() {
            @Override
            public void handle(Replicator replicator, KeyValuePair<?> kv) {
                acc.incrementAndGet();
            }

            @Override
            public void postFullSync(Replicator replicator, long checksum) {
                super.postFullSync(replicator, checksum);
                atomicChecksum.compareAndSet(0, checksum);
                assertEquals(132, acc.get());
                assertEquals(-3409494954737929802L, atomicChecksum.get());
            }
        });
        redisReplicator.open();

    }

    @Test
    public void testCloseListener1() throws IOException, InterruptedException {
        final AtomicInteger acc = new AtomicInteger(0);
        Replicator replicator = new RedisReplicator(
                RedisSocketReplicatorTest.class.getClassLoader().getResourceAsStream("dumpV6.rdb"),
                Configuration.defaultSetting());
        replicator.addCloseListener(new CloseListener() {
            @Override
            public void handle(Replicator replicator) {
                acc.incrementAndGet();
                assertEquals(1, acc.get());
            }
        });
        replicator.open();
    }

    @Test
    public void testFileV7() throws IOException, InterruptedException {
        Replicator redisReplicator = new RedisReplicator(
                RedisSocketReplicatorTest.class.getClassLoader().getResourceAsStream("dumpV7.rdb"),
                Configuration.defaultSetting());
        final AtomicInteger acc = new AtomicInteger(0);
        redisReplicator.addRdbListener(new RdbListener.Adaptor() {
            @Override
            public void handle(Replicator replicator, KeyValuePair<?> kv) {
                acc.incrementAndGet();
                if (kv.getKey().equals("abcd")) {
                    KeyStringValueString ksvs = (KeyStringValueString) kv;
                    assertEquals("abcd", ksvs.getValue());
                }
                if (kv.getKey().equals("foo")) {
                    KeyStringValueString ksvs = (KeyStringValueString) kv;
                    assertEquals("bar", ksvs.getValue());
                }
                if (kv.getKey().equals("aaa")) {
                    KeyStringValueString ksvs = (KeyStringValueString) kv;
                    assertEquals("bbb", ksvs.getValue());
                }
            }

            @Override
            public void postFullSync(Replicator replicator, long checksum) {
                super.postFullSync(replicator, checksum);
                assertEquals(19, acc.get());
            }
        });
        redisReplicator.open();

    }

    @Test
    public void testFilter() throws IOException, InterruptedException {
        Replicator redisReplicator = new RedisReplicator(
                RedisSocketReplicatorTest.class.getClassLoader().getResourceAsStream("dumpV7.rdb"),
                Configuration.defaultSetting());
        final AtomicInteger acc = new AtomicInteger(0);
        redisReplicator.addRdbFilter(new RdbFilter() {
            @Override
            public boolean accept(KeyValuePair<?> kv) {
                return kv.getValueRdbType() == 0;
            }
        });
        redisReplicator.addRdbListener(new RdbListener.Adaptor() {
            @Override
            public void preFullSync(Replicator replicator) {
                super.preFullSync(replicator);
                assertEquals(0, acc.get());
            }

            @Override
            public void handle(Replicator replicator, KeyValuePair<?> kv) {
                acc.incrementAndGet();
            }

            @Override
            public void postFullSync(Replicator replicator, long checksum) {
                super.postFullSync(replicator, checksum);
                assertEquals(13, acc.get());
            }
        });
        redisReplicator.open();
    }

    @Test
    public void testFileV6() throws IOException, InterruptedException {
        Replicator redisReplicator = new RedisReplicator(
                RedisSocketReplicatorTest.class.getClassLoader().getResourceAsStream("dumpV6.rdb"),
                Configuration.defaultSetting());
        final AtomicInteger acc = new AtomicInteger(0);
        redisReplicator.addRdbListener(new RdbListener.Adaptor() {

            @Override
            public void handle(Replicator replicator, KeyValuePair<?> kv) {
                acc.incrementAndGet();
            }

            @Override
            public void postFullSync(Replicator replicator, long checksum) {
                super.postFullSync(replicator, checksum);
                assertEquals(132, acc.get());
            }
        });
        redisReplicator.open();
    }

}
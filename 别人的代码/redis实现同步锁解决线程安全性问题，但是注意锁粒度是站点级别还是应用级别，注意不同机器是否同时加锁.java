package com.ioe.stored.energy.util;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.ioe.api.context.OpenApiContextHolder;

import redis.clients.jedis.JedisCluster;

/**
 * @author ZhengLou Zhong
 * @since 1.4
 */
public class LockRequestIdUtil {
    //设定将值在redis中保存30s
    public static boolean lock(JedisCluster jedisCluster) {
        return lock(jedisCluster, 30, TimeUnit.SECONDS);
    }

    
    public static boolean lock(JedisCluster jedisCluster, long time, TimeUnit unit) {
        //强制要求使用lock方法时设定时间不能少于1s
        if (time < 1) {
            throw new IllegalArgumentException("time < 1 异常");
        }
        //获取请求id，该id可以唯一标志一个请求
        final String requestId = getRequestId();
        final long millis = unit.toMillis(time);
        //如果requestId已存在，设置失败
        final String existKey = jedisCluster.set(requestId, "1", "NX", "PX", millis);
        //如果设置成功，代表这个请求调用的方法是第一次被调用
        return "OK".equalsIgnoreCase(existKey);
    }

    public static void unlock(JedisCluster jedisCluster) {
//        return
        jedisCluster.del(getRequestId());
    }

    public static void unlockQuietly(JedisCluster jedisCluster) {
        try {
            unlock(jedisCluster);
        } catch (Exception e) {
            // ignore
        }
    }

    private static String getRequestId() {
        final String requestId = OpenApiContextHolder.getContext().getRequestId();
        if (StringUtils.isEmpty(requestId)) {
            throw new IllegalStateException("无效的 requestId");
        }
        return requestId;
    }
}

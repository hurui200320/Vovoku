package info.skyblond.vovoku.commons.redis;

import redis.clients.jedis.Jedis;

import java.time.Duration;
import java.util.UUID;

/**
 * Redis distributed lock implementation. This is thread safe.
 * Reference: com.github.jedis.lock.info.skyblond.vovoku.commons.redis.JedisLock, by Alois Belaska <alois.belaska@gmail.com>
 *
 * @author Rui Hu <hurui200320@skyblond.info>
 */
public class JedisLock implements AutoCloseable {
    /**
     * Represent null lock
     */
    private static final Lock NULL_LOCK = new Lock(new UUID(0L, 0L), 0L);

    /**
     * Jedis client, not thread safe. Thus all function using jedis should be synced
     */
    private final Jedis jedis;

    /**
     * Lock key
     */
    private final String lockKey;

    /**
     * Duration of this lock
     */
    private final Duration lockExpiryDuration;

    /**
     * UUID identifier for this lock
     */
    private final UUID lockUUID;

    /**
     * Internal representation
     */
    private Lock internalLock = null;

    @Override
    public void close() throws Exception {
        jedis.close();
    }

    /**
     * The internal lock structure
     */
    protected static class Lock {
        private final UUID uuid;
        private final long expiryTimestampInMillis;

        protected Lock(UUID uuid, long expiryTimestampInMillis) {
            this.uuid = uuid;
            this.expiryTimestampInMillis = expiryTimestampInMillis;
        }

        protected static Lock fromString(String text) {
            try {
                String[] parts = text.split(":");
                UUID theUUID = UUID.fromString(parts[0]);
                long theTime = Long.parseLong(parts[1]);
                return new Lock(theUUID, theTime);
            } catch (Exception any) {
                return NULL_LOCK;
            }
        }

        @Override
        public String toString() {
            return this.uuid.toString() + ":" + this.expiryTimestampInMillis;
        }

        boolean isExpired() {
            return this.expiryTimestampInMillis < System.currentTimeMillis();
        }

        boolean isExpiredOrMine(UUID otherUUID) {
            return this.isExpired() || this.uuid.equals(otherUUID);
        }

        public UUID getUuid() {
            return this.uuid;
        }

        public long getExpiryTimestampInMillis() {
            return this.expiryTimestampInMillis;
        }
    }

    /**
     * Detailed constructor with generated UUID
     *
     * @param jedis              {@link Jedis} instance to talk with Redis
     * @param lockKey            lock key, same as key in redis
     * @param lockExpiryDuration lock expiration in {@link Duration}
     */
    public JedisLock(Jedis jedis, String lockKey, Duration lockExpiryDuration) {
        this(jedis, lockKey, lockExpiryDuration, UUID.randomUUID());
    }

    /**
     * Detailed constructor.
     *
     * @param jedis              {@link Jedis} instance to talk with Redis
     * @param lockKey            lock key, same as key in redis
     * @param lockExpiryDuration lock expiration in {@link Duration}
     * @param uuid               unique identification of this lock
     */
    public JedisLock(Jedis jedis, String lockKey, Duration lockExpiryDuration, UUID uuid) {
        this.jedis = jedis;
        this.lockKey = lockKey;
        this.lockExpiryDuration = lockExpiryDuration;
        this.lockUUID = uuid;
    }

    /**
     * Acquire lock. Public interface.
     *
     * @return true if lock is acquired, false acquire failed
     */
    public synchronized boolean acquire() {
        try {
            return this.acquire(this.jedis);
        } catch (InterruptedException any) {
            return false;
        }
    }

    /**
     * Acquire lock. Private implementation.
     *
     * @param jedis {@link Jedis} instance to talk with Redis
     * @return true if lock is acquired, false acquire failed
     * @throws InterruptedException in case of thread interruption
     */
    protected synchronized boolean acquire(Jedis jedis) throws InterruptedException {
        final Lock newLock = new Lock(
                this.lockUUID,
                System.currentTimeMillis() + this.lockExpiryDuration.toMillis()
        );
        // lock by SETNX
        if (jedis.setnx(this.lockKey, newLock.toString()) == 1) {
            this.internalLock = newLock;
            return true;
        }
        // if lock key exists, check current lock
        final String currentValueStr = jedis.get(this.lockKey);
        final Lock currentLock = Lock.fromString(currentValueStr);
        // is expired lock or last lock from this instance
        if (currentLock.isExpiredOrMine(this.lockUUID)) {
            // replace with the new key, guaranteed by atomic getSet
            String oldValueStr = jedis.getSet(this.lockKey, newLock.toString());
            // if old value is not changed, then lock is acquired
            // otherwise another program acquire the lock
            if (oldValueStr != null && oldValueStr.equals(currentValueStr)) {
                this.internalLock = newLock;
                return true;
            }
        }
        return false;
    }

    /**
     * Renew lock.
     *
     * @return true if lock is renewed, false otherwise
     * @throws InterruptedException in case of thread interruption
     */
    public boolean renew() throws InterruptedException {
        // get current lock in redis
        final Lock lock = Lock.fromString(this.jedis.get(this.lockKey));
        // if not from this instance or not expired, then return false
        if (!lock.isExpiredOrMine(this.lockUUID)) {
            return false;
        }
        // otherwise acquire a new lock
        return this.acquire(this.jedis);
    }

    /**
     * Acquired lock release.
     */
    public synchronized void release() {
        this.release(this.jedis);
    }

    /**
     * Release current lock.
     *
     * @param jedis {@link Jedis} instance to talk with Redis
     */
    protected synchronized void release(Jedis jedis) {
        if (this.isLocked()) {
            jedis.del(this.lockKey);
            this.internalLock = null;
        }
    }

    /**
     * Check if owns the lock: locked and not expired
     *
     * @return true if lock owned
     */
    public synchronized boolean isLocked() {
        return this.internalLock != null && !this.internalLock.isExpired();
    }

    public String getLockKey() {
        return this.lockKey;
    }

    public Duration getLockExpiryDuration() {
        return this.lockExpiryDuration;
    }

    public UUID getLockUUID() {
        return this.lockUUID;
    }
}
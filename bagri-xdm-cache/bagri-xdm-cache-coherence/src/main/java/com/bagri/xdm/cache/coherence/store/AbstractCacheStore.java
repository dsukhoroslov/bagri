package com.bagri.xdm.cache.coherence.store;

import com.tangosol.net.cache.CacheStore;
//import oracle.sql.ARRAY;
//import oracle.sql.ArrayDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Abstract Cache Store class
 * User: dsukhoroslov
 * Date: 24.07.12 15:45
 *
 * @param <K> Key
 * @param <V> Value
 */
public abstract class AbstractCacheStore<K, V> implements CacheStore {

    private final Logger logger;

    /**
     * @return JDBC template
     */
    public JdbcTemplate getTemplate() {
        return template;
    }

    private JdbcTemplate template;

    /**
     * Class constructor
     */
    public AbstractCacheStore() {
        this.logger = LoggerFactory.getLogger(getClass());
    }

    public Logger getLogger() {
        return logger;
    }

    /**
     * @return Data keys
     */
    public abstract List<K> getDataKeys();

    /**
     * @return true if {@code loadAll(keys)} can load data in batch, else false
     */
    public boolean isSupportBatchLoading() {
        return true;
    }

    /**
     * @param template JDBC template
     */
    public void setJdbcTemplate(JdbcTemplate template) {
        this.template = template;
    }

    /**
     * Load one key
     *
     * @param key Key
     * @return Loaded key
     */
    @Override
    public Object load(Object key) {
        logger.trace("load. key: {}", key);
        return null;
    }

    /**
     * Load all keys
     *
     * @param keys Key collection
     * @return Loaded keys
     */
    @Override
    public Map loadAll(Collection keys) {
        logger.trace("loadAll. keys: {}", keys);
        return Collections.EMPTY_MAP;
    }

    /**
     * Delete one key
     *
     * @param key Key
     */
    @Override
    public void erase(Object key) {
        logger.trace("erase. key: {}", key);
    }

    /**
     * Delete all keys
     *
     * @param keys Key collection
     */
    @Override
    public void eraseAll(Collection keys) {
        logger.trace("eraseAll. keys: {}", keys);
    }

    @Override
    public void store(Object key, Object value) {
        logger.trace("store. key: {}, value: {}", key, value);
    }

    /**
     * @param entities Entity map
     */
    @Override
    public void storeAll(Map entities) {
        logger.trace("storeAll. entities: {}", entities.size());
        //storeAll(entities.values(), getStoreAllSql());
    }

    /**
     * String IDs Setter class
     */
//    static class StringIdsSetter implements PreparedStatementSetter {
//
//        private Collection<String> ids;
//
//        StringIdsSetter(Collection<String> ids) {
//            this.ids = ids;
//        }
//
//        @Override
//        public void setValues(PreparedStatement ps) throws SQLException {
//            ArrayDescriptor aDesc = ArrayDescriptor.createDescriptor("SYS.ODCIVARCHAR2LIST", ps.getConnection());
//            String[] keys = ids.toArray(new String[ids.size()]);
//            //logger.trace("converted ids: {}", Arrays.toString(keys));
//            ARRAY dArray = new ARRAY(aDesc, ps.getConnection(), keys);
//            ps.setArray(1, dArray);
//        }
//    }

    /**
     * @param value Boolean value
     * @return Converted boolean value to database format
     */
    public static String toDatabaseBoolean(boolean value) {
        return value ? "Y" : "N";
    }

    /**
     * @param value Database boolean value
     * @return Java boolean value
     */
    public static boolean fromDatabaseBoolean(String value) {
        return "Y".equals(value);
    }

    /**
     * @param arr    Array
     * @param values Values
     * @param <T>    Type class
     * @return Typed array
     */
    public static <T> T[] add(T[] arr, T... values) {
        T[] res = Arrays.copyOf(arr, arr.length + values.length);
        System.arraycopy(values, 0, res, arr.length, values.length);
        return res;
    }
}

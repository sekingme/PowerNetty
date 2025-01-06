 /* power by sekingme */

package org.infraRpcExample.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.infraRpcExample.dao.entity.ServerName;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

import static org.infraRpcExample.utils.Constants.CACHE_KEY_VALUE_ALL;


/**
 * server name mapper interface
 *
 * @author sekingme
 */
@Deprecated
@CacheConfig(cacheNames = "serverName", keyGenerator = "cacheKeyGenerator")
public interface ServerMapper extends BaseMapper<ServerName> {

    /**
     * query all server name
     *
     * @return server name list
     */
    @Cacheable(sync = true, key = CACHE_KEY_VALUE_ALL)
    List<ServerName> queryAllWorkerGroup();

    @CacheEvict(key = CACHE_KEY_VALUE_ALL)
    int deleteById(Integer id);

    @Override
    @CacheEvict(key = CACHE_KEY_VALUE_ALL)
    int insert(ServerName entity);

    @Override
    @CacheEvict(key = CACHE_KEY_VALUE_ALL)
    int updateById(@Param("et") ServerName entity);

    /**
     * query server by name
     *
     * @param name name
     * @return server list
     */
    List<ServerName> queryServerByName(@Param("name") String name);

}

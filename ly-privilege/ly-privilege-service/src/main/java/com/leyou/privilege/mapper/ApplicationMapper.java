package com.leyou.privilege.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.privilege.entity.ApplicationInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationMapper extends BaseMapper<ApplicationInfo> {

    int insertApplicationPrivilege(@Param("serviceId") Long serviceId,@Param("idList") List<Long> idList);

    List<Long> queryTargetIdList(@Param("serviceId") Long serviceId);
}

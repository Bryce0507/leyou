package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.entity.SpecGroup;
import com.leyou.item.entity.SpecParam;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SpecService {
    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper paramMapper;


    public List<SpecGroupDTO> queryGroupByCategory(Long id) {
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(id);
        List<SpecGroup> list = specGroupMapper.select(specGroup);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }
        //对象转换
        return BeanHelper.copyWithCollection(list, SpecGroupDTO.class);
    }


    /**
     * 根据gid 或者cd 查询参数列表
     * @param gid
     * @param cid
     * @param searching
     * @return
     */
    public List<SpecParamDTO> querySpecParams(Long gid, Long cid, Boolean searching) {
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setSearching(searching);

        List<SpecParam> list = paramMapper.select(specParam);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }
        //对象转换
        return BeanHelper.copyWithCollection(list, SpecParamDTO.class);

    }

    /**
     * 增加分组
     * @param specGroupDTO
     */
    @Transactional
    public void addGroup(SpecGroupDTO specGroupDTO) {
        SpecGroup specGroup = BeanHelper.copyProperties(specGroupDTO, SpecGroup.class);
        specGroup.setId(null);
        int count = specGroupMapper.insertSelective(specGroup);
        if (count != 1) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

    }

    /**
     * 增加参数
     * @param specParamDTO
     */
    @Transactional
    public void addParam(SpecParamDTO specParamDTO) {
        SpecParam specParam = BeanHelper.copyProperties(specParamDTO, SpecParam.class);
        specParam.setId(null);
        int count = paramMapper.insertSelective(specParam);
        if (count != 1) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    /**
     *规格参数分组修改按钮
     * @param specGroupDTO
     */
    @Transactional
    public void updateGroup(SpecGroupDTO specGroupDTO) {
        SpecGroup specGroup = BeanHelper.copyProperties(specGroupDTO, SpecGroup.class);
        int count = specGroupMapper.updateByPrimaryKeySelective(specGroup);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }

    /**
     * 根据id删除参数分组
     * @param id
     */

    @Transactional
    public void deleteGroup(Long id) {
        int count = specGroupMapper.deleteByPrimaryKey(id);
        if (count != 1) {
            throw new LyException(ExceptionEnum.DELETE_OPERATION_FAIL);
        }

    }

    /**
     * 根据cid 查询参数组 和组内的参数
     * @param id
     * @return
     */
    public List<SpecGroupDTO> querySpecsByCid(Long id) {
        //查询规格组
        List<SpecGroupDTO> specGroupDTOS = queryGroupByCategory(id);
        //循环根据specGroup的id 和cid 查询 对应的specParams
   /*     for (SpecGroupDTO specGroupDTO : specGroupDTOS) {
            List<SpecParamDTO> specParamDTOS = querySpecParams(specGroupDTO.getId(), id, null);
            specGroupDTO.setParams(specParamDTOS);
        }*/
        //查询所有cd 的规格参数的value
        List<SpecParamDTO> params = querySpecParams(null, id, null);
        //参数转化为流
        Map<Long, List<SpecParamDTO>> paramMap = params.stream()
                //按照groupingBy后面的分组返回一个map
                .collect(Collectors.groupingBy(SpecParamDTO::getGroupId));
//        for (SpecGroupDTO specGroupDTO : specGroupDTOS) {
//            specGroupDTO.setParams(paramMap.get(specGroupDTO.getId()));
//        }
        specGroupDTOS.forEach(specGroupDTO -> specGroupDTO
                .setParams(paramMap.get(specGroupDTO.getId())));

        return specGroupDTOS;
    }



    /* *//**
     * 根据cid 查询ParamDto
     * @param cid
     * @return
     *//*
    public List<SpecParamDTO> queryParamsByCid(Long cid) {
        SpecParam specParam = new SpecParam();
        specParam.setCid(cid);
        List<SpecParam> list = paramMapper.select(specParam);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }
        //对象转换
        return BeanHelper.copyWithCollection(list, SpecParamDTO.class);
    }*/
}

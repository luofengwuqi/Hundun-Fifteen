package com.createpen.fifteenhundun.module.memo.service.serviceimpl;

import com.createpen.fifteenhundun.common.context.BaseContext;
import com.createpen.fifteenhundun.module.memo.mapper.MemoCommonMapper;
import com.createpen.fifteenhundun.module.memo.pojo.DTO.MemoCommonDTO;
import com.createpen.fifteenhundun.module.memo.pojo.Entity.MemoCommon;
import com.createpen.fifteenhundun.module.memo.service.MemoCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class MemoCommonServiceImpl implements MemoCommonService {
    
    private final MemoCommonMapper memoCommonMapper;
    
    public MemoCommonServiceImpl(MemoCommonMapper memoCommonMapper) {
        this.memoCommonMapper = memoCommonMapper;
    }
    
    @Override
    @Transactional
    public void insertMemoCommon(MemoCommonDTO dto) {
        log.info("创建普通备忘, userId: {}", BaseContext.getCurrentId());
        
        MemoCommon memoCommon = new MemoCommon();
        BeanUtils.copyProperties(dto, memoCommon);
        memoCommon.setUserId(BaseContext.getCurrentId());
        
        memoCommonMapper.insertMemoCommon(memoCommon);
        log.info("普通备忘创建成功, id: {}", memoCommon.getId());
    }
    
    @Override
    @Transactional
    public void deleteMemoCommonById(Long id) {
        log.info("删除普通备忘, id: {}, userId: {}", id, BaseContext.getCurrentId());
        
        Long userId = BaseContext.getCurrentId();
        memoCommonMapper.deleteMemoCommonById(id, userId);
        log.info("普通备忘删除成功, id: {}", id);
    }
    
    @Override
    @Transactional
    public void updateMemoCommon(MemoCommonDTO dto) {
        log.info("更新普通备忘, id: {}, userId: {}", dto.getId(), BaseContext.getCurrentId());
        
        MemoCommon memoCommon = new MemoCommon();
        BeanUtils.copyProperties(dto, memoCommon);
        memoCommon.setUserId(BaseContext.getCurrentId());
        
        memoCommonMapper.updateMemoCommon(memoCommon);
        log.info("普通备忘更新成功, id: {}", dto.getId());
    }
    
    @Override
    public MemoCommon selectMemoCommonById(Long id) {
        log.info("查询普通备忘, id: {}", id);
        return memoCommonMapper.selectMemoCommonById(id);
    }
    
    @Override
    public List<MemoCommon> selectMemoCommonsByUserId(Long userId) {
        log.info("查询用户的普通备忘列表, userId: {}", userId);
        return memoCommonMapper.selectMemoCommonsByUserId(userId);
    }
    
    @Override
    public List<MemoCommon> selectAllMemoCommons() {
        log.info("查询所有普通备忘");
        return memoCommonMapper.selectAllMemoCommons();
    }
}

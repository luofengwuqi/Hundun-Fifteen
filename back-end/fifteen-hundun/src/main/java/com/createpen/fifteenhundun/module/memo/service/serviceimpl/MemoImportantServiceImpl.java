package com.createpen.fifteenhundun.module.memo.service.serviceimpl;

import com.createpen.fifteenhundun.common.context.BaseContext;
import com.createpen.fifteenhundun.module.memo.mapper.MemoImportantMapper;
import com.createpen.fifteenhundun.module.memo.pojo.DTO.MemoImportantDTO;
import com.createpen.fifteenhundun.module.memo.pojo.Entity.MemoImportant;
import com.createpen.fifteenhundun.module.memo.service.MemoImportantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class MemoImportantServiceImpl implements MemoImportantService {
    
    private final MemoImportantMapper memoImportantMapper;
    
    public MemoImportantServiceImpl(MemoImportantMapper memoImportantMapper) {
        this.memoImportantMapper = memoImportantMapper;
    }
    
    @Override
    @Transactional
    public void insertMemoImportant(MemoImportantDTO dto) {
        log.info("创建重要备忘, userId: {}", BaseContext.getCurrentId());
        
        MemoImportant memoImportant = new MemoImportant();
        BeanUtils.copyProperties(dto, memoImportant);
        memoImportant.setUserId(BaseContext.getCurrentId());
        
        memoImportantMapper.insertMemoImportant(memoImportant);
        log.info("重要备忘创建成功, id: {}", memoImportant.getId());
    }
    
    @Override
    @Transactional
    public void deleteMemoImportantById(Long id) {
        log.info("删除重要备忘, id: {}, userId: {}", id, BaseContext.getCurrentId());
        
        Long userId = BaseContext.getCurrentId();
        memoImportantMapper.deleteMemoImportantById(id, userId);
        log.info("重要备忘删除成功, id: {}", id);
    }
    
    @Override
    @Transactional
    public void updateMemoImportant(MemoImportantDTO dto) {
        log.info("更新重要备忘, id: {}, userId: {}", dto.getId(), BaseContext.getCurrentId());
        
        MemoImportant memoImportant = new MemoImportant();
        BeanUtils.copyProperties(dto, memoImportant);
        memoImportant.setUserId(BaseContext.getCurrentId());
        
        memoImportantMapper.updateMemoImportant(memoImportant);
        log.info("重要备忘更新成功, id: {}", dto.getId());
    }
    
    @Override
    public MemoImportant selectMemoImportantById(Long id) {
        log.info("查询重要备忘, id: {}", id);
        return memoImportantMapper.selectMemoImportantById(id);
    }
    
    @Override
    public List<MemoImportant> selectMemoImportantsByUserId(Long userId) {
        log.info("查询用户的重要备忘列表, userId: {}", userId);
        return memoImportantMapper.selectMemoImportantsByUserId(userId);
    }
    
    @Override
    public List<MemoImportant> selectAllMemoImportants() {
        log.info("查询所有重要备忘");
        return memoImportantMapper.selectAllMemoImportants();
    }
}

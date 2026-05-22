package com.createpen.fifteenhundun.module.memo.service.serviceimpl;

import com.createpen.fifteenhundun.common.context.BaseContext;
import com.createpen.fifteenhundun.module.memo.cache.MemoImportantRedisService;
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
    private final MemoImportantRedisService memoImportantRedisService;

    public MemoImportantServiceImpl(MemoImportantMapper memoImportantMapper,
                                    MemoImportantRedisService memoImportantRedisService) {
        this.memoImportantMapper = memoImportantMapper;
        this.memoImportantRedisService = memoImportantRedisService;
    }

    @Override
    @Transactional
    public void insertMemoImportant(MemoImportantDTO dto) {
        log.info("创建重要备忘, userId: {}", BaseContext.getCurrentId());

        MemoImportant memoImportant = new MemoImportant();
        BeanUtils.copyProperties(dto, memoImportant);
        memoImportant.setUserId(BaseContext.getCurrentId());

        memoImportantMapper.insertMemoImportant(memoImportant);
        memoImportantRedisService.evictListCache(memoImportant.getUserId());
        log.info("重要备忘创建成功, id: {}", memoImportant.getId());
    }

    @Override
    @Transactional
    public void deleteMemoImportantById(Long id) {
        log.info("删除重要备忘, id: {}, userId: {}", id, BaseContext.getCurrentId());

        Long userId = BaseContext.getCurrentId();
        memoImportantMapper.deleteMemoImportantById(id, userId);
        memoImportantRedisService.evictAll(userId, id);
        log.info("重要备忘删除成功, id: {}", id);
    }

    @Override
    @Transactional
    public void updateMemoImportant(MemoImportantDTO dto) {
        log.info("更新重要备忘, id: {}, userId: {}", dto.getId(), BaseContext.getCurrentId());

        Long userId = BaseContext.getCurrentId();
        MemoImportant memoImportant = new MemoImportant();
        BeanUtils.copyProperties(dto, memoImportant);
        memoImportant.setUserId(userId);

        memoImportantMapper.updateMemoImportant(memoImportant);
        memoImportantRedisService.evictAll(userId, dto.getId());
        log.info("重要备忘更新成功, id: {}", dto.getId());
    }

    @Override
    public MemoImportant selectMemoImportantById(Long id) {
        log.info("查询重要备忘, id: {}", id);
        Long userId = BaseContext.getCurrentId();

        if (memoImportantRedisService.hasItemKey(userId, id)) {
            log.info("重要备忘单条缓存命中, id: {}", id);
            return memoImportantRedisService.selectById(userId, id);
        }

        MemoImportant fromDb = memoImportantMapper.selectMemoImportantById(id);
        if (fromDb != null && userId.equals(fromDb.getUserId())) {
            memoImportantRedisService.saveById(userId, fromDb);
            return fromDb;
        }
        memoImportantRedisService.saveEmptyById(userId, id);
        return null;
    }

    @Override
    public List<MemoImportant> selectMemoImportantsByUserId(Long userId) {
        log.info("查询用户的重要备忘列表, userId: {}", userId);

        List<MemoImportant> cached = memoImportantRedisService.selectList(userId);
        if (cached != null) {
            log.info("重要备忘列表缓存命中, userId: {}", userId);
            return cached;
        }

        List<MemoImportant> fromDb = memoImportantMapper.selectMemoImportantsByUserId(userId);
        memoImportantRedisService.saveList(userId, fromDb);
        return fromDb;
    }

    @Override
    public List<MemoImportant> selectAllMemoImportants() {
        log.info("查询所有重要备忘");
        return memoImportantMapper.selectAllMemoImportants();
    }
}

package com.createpen.fifteenhundun.module.memo.service.serviceimpl;

import com.createpen.fifteenhundun.common.context.BaseContext;
import com.createpen.fifteenhundun.common.exception.MyException.SQLException;
import com.createpen.fifteenhundun.module.memo.cache.MemoCommonRedisService;
import com.createpen.fifteenhundun.module.memo.mapper.MemoCommonMapper;
import com.createpen.fifteenhundun.module.memo.pojo.DTO.MemoCommonDTO;
import com.createpen.fifteenhundun.module.memo.pojo.Entity.MemoCommon;
import com.createpen.fifteenhundun.module.memo.service.MemoCommonService;
import com.createpen.fifteenhundun.module.user.pojo.Entity.User;
import com.createpen.fifteenhundun.module.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class MemoCommonServiceImpl implements MemoCommonService {

    private final UserService userService;
    private final MemoCommonMapper memoCommonMapper;
    private final MemoCommonRedisService memoCommonRedisService;

    public MemoCommonServiceImpl(MemoCommonMapper memoCommonMapper,
                                 UserService userService,
                                 MemoCommonRedisService memoCommonRedisService) {
        this.memoCommonMapper = memoCommonMapper;
        this.userService = userService;
        this.memoCommonRedisService = memoCommonRedisService;
    }

    @Override
    @Transactional
    public void insertMemoCommon(MemoCommonDTO dto) {
        log.info("创建普通备忘, userId: {}", BaseContext.getCurrentId());
        Long userId = BaseContext.getCurrentId();
        MemoCommon memoCommon = new MemoCommon();
        BeanUtils.copyProperties(dto, memoCommon);
        memoCommon.setUserId(userId);
        if (dto.getStatus() != null && dto.getStatus() == 1) {
            User user = userService.selectUserById(userId);
            memoCommon.setEmail(user.getEmail());
        }

        memoCommonMapper.insertMemoCommon(memoCommon);
        memoCommonRedisService.evictListCaches(userId);
        log.info("普通备忘创建成功, id: {}", memoCommon.getId());
    }

    @Override
    @Transactional
    public void deleteMemoCommonById(Long id) {
        log.info("删除普通备忘, id: {}, userId: {}", id, BaseContext.getCurrentId());
        Long userId = BaseContext.getCurrentId();
        memoCommonMapper.deleteMemoCommonById(id, userId);
        memoCommonRedisService.evictAll(userId, id);
        log.info("普通备忘删除成功, id: {}", id);
    }

    @Override
    @Transactional
    public void updateMemoCommon(MemoCommonDTO dto) {
        log.info("更新普通备忘, id: {}, userId: {}", dto.getId(), BaseContext.getCurrentId());
        Long userId = BaseContext.getCurrentId();
        MemoCommon memoCommon = new MemoCommon();
        BeanUtils.copyProperties(dto, memoCommon);
        memoCommon.setUserId(userId);
        try{
            memoCommonMapper.updateMemoCommon(memoCommon);
        }catch (Exception e){
            throw new SQLException("操作失败,请检查任务是否开启");
        }
        memoCommonRedisService.evictAll(userId, dto.getId());
        log.info("普通备忘更新成功, id: {}", dto.getId());
    }

    @Override
    public MemoCommon selectMemoCommonById(Long id) {
        log.info("查询普通备忘, id: {}", id);
        Long userId = BaseContext.getCurrentId();

        if (memoCommonRedisService.hasItemKey(userId, id)) {
            log.info("普通备忘单条缓存命中, id: {}", id);
            return memoCommonRedisService.selectById(userId, id);
        }

        MemoCommon fromDb = memoCommonMapper.selectMemoCommonById(id);
        if (fromDb != null && userId.equals(fromDb.getUserId())) {
            memoCommonRedisService.saveById(userId, fromDb);
            return fromDb;
        }
        memoCommonRedisService.saveEmptyById(userId, id);
        return null;
    }

    @Override
    public List<MemoCommon> selectMemoCommonsByUserId(Long userId) {
        return selectMemoCommonsByUserId(userId, null);
    }

    @Override
    public List<MemoCommon> selectMemoCommonsByUserId(Long userId, String completion) {
        log.info("查询用户的普通备忘列表, userId: {}, completion: {}", userId, completion);

        List<MemoCommon> cached = memoCommonRedisService.selectList(userId, completion);
        if (cached != null) {
            log.info("普通备忘列表缓存命中, userId: {}, completion: {}", userId, completion);
            return cached;
        }

        List<MemoCommon> fromDb = memoCommonMapper.selectMemoCommonsByUserId(userId, completion);
        memoCommonRedisService.saveList(userId, completion, fromDb);
        return fromDb;
    }

    @Override
    public List<MemoCommon> selectAllMemoCommons() {
        log.info("查询所有普通备忘");
        return memoCommonMapper.selectAllMemoCommons();
    }
}

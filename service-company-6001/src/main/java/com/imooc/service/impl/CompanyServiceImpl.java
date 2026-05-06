package com.imooc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.imooc.base.BaseInfoProperties;
import com.imooc.enums.CompanyReviewStatus;
import com.imooc.enums.YesOrNo;
import com.imooc.exceptions.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.mapper.CompanyMapper;
import com.imooc.mapper.CompanyMapperCustom;
import com.imooc.mapper.CompanyPhotoMapper;
import com.imooc.pojo.Company;
import com.imooc.pojo.CompanyPhoto;
import com.imooc.pojo.Users;
import com.imooc.pojo.bo.CreateCompanyBO;
import com.imooc.pojo.bo.ModifyCompanyInfoBO;
import com.imooc.pojo.bo.QueryCompanyBO;
import com.imooc.pojo.bo.ReviewCompanyBO;
import com.imooc.pojo.vo.CompanyInfoVO;
import com.imooc.service.CompanyService;
import com.imooc.utils.LocalDateUtils;
import com.imooc.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.redisson.RedissonMultiLock;
import org.redisson.api.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>
 * 企业表 服务实现类
 * </p>
 *
 * @author 风间影月
 * @since 2022-09-04
 */
@Service
public class CompanyServiceImpl extends BaseInfoProperties implements CompanyService {

    @Autowired
    private CompanyMapper companyMapper;
    @Autowired
    private CompanyMapperCustom companyMapperCustom;
    @Autowired
    private CompanyPhotoMapper companyPhotoMapper;

    @Override
    public Company getByFullName(String fullName) {

        Company tempCompany = companyMapper.selectOne(
                new QueryWrapper<Company>()
                    .eq("company_name", fullName)
        );

        return tempCompany;
    }

    @Transactional
    @Override
    public String createNewCompany(CreateCompanyBO createCompanyBO) {

        Company newCompany = new Company();

        BeanUtils.copyProperties(createCompanyBO, newCompany);

        newCompany.setIsVip(YesOrNo.NO.type);
        newCompany.setReviewStatus(CompanyReviewStatus.NOTHING.type);
        newCompany.setCreatedTime(LocalDateTime.now());
        newCompany.setUpdatedTime(LocalDateTime.now());

        companyMapper.insert(newCompany);

        return newCompany.getId();
    }

    @Transactional
    @Override
    public String resetNewCompany(CreateCompanyBO createCompanyBO) {

        Company newCompany = new Company();

        BeanUtils.copyProperties(createCompanyBO, newCompany);

        newCompany.setId(createCompanyBO.getCompanyId());
        newCompany.setReviewStatus(CompanyReviewStatus.NOTHING.type);
        newCompany.setUpdatedTime(LocalDateTime.now());

        companyMapper.updateById(newCompany);

        return createCompanyBO.getCompanyId();
    }

    @Override
    public Company getById(String id) {
        return companyMapper.selectById(id);
    }

    @Transactional
    @Override
    public void commitReviewCompanyInfo(ReviewCompanyBO reviewCompanyBO) {

        Company pendingCompany = new Company();
        pendingCompany.setId(reviewCompanyBO.getCompanyId());

        pendingCompany.setReviewStatus(CompanyReviewStatus.REVIEW_ING.type);
        pendingCompany.setReviewReplay(""); // 如果有内容，则重置覆盖之前的审核意见
        pendingCompany.setAuthLetter(reviewCompanyBO.getAuthLetter());

        pendingCompany.setCommitUserId(reviewCompanyBO.getHrUserId());
        pendingCompany.setCommitUserMobile(reviewCompanyBO.getHrMobile());
        pendingCompany.setCommitDate(LocalDate.now());

        pendingCompany.setUpdatedTime(LocalDateTime.now());

        companyMapper.updateById(pendingCompany);
    }

    @Override
    public PagedGridResult queryCompanyListPaged(QueryCompanyBO companyBO,
                                                 Integer page,
                                                 Integer limit) {

        PageHelper.startPage(page, limit);

        Map<String, Object> map = new HashMap<>();
        map.put("companyName", companyBO.getCompanyName());
        map.put("realName", companyBO.getCommitUser());
        map.put("reviewStatus", companyBO.getReviewStatus());
        map.put("commitDateStart", companyBO.getCommitDateStart());
        map.put("commitDateEnd", companyBO.getCommitDateEnd());

        List<CompanyInfoVO> list = companyMapperCustom.queryCompanyList(map);

        return setterPagedGrid(list, page);
    }

    @Override
    public CompanyInfoVO getCompanyInfo(String companyId) {

        Map<String, Object> map = new HashMap<>();
        map.put("companyId", companyId);

        CompanyInfoVO companyInfo = companyMapperCustom.getCompanyInfo(map);
        return companyInfo;
    }

    @Transactional
    @Override
    public void updateReviewInfo(ReviewCompanyBO reviewCompanyBO) {

        Company pendingCompany = new Company();
        pendingCompany.setId(reviewCompanyBO.getCompanyId());

        pendingCompany.setReviewStatus(reviewCompanyBO.getReviewStatus());
        pendingCompany.setReviewReplay(reviewCompanyBO.getReviewReplay());

        pendingCompany.setUpdatedTime(LocalDateTime.now());

        companyMapper.updateById(pendingCompany);
    }

    @Transactional
//    @Override
    public void modifyCompanyInfo2(ModifyCompanyInfoBO companyInfoBO) throws Exception {

        // 1. 获得锁，值随意，只要不为空即可
//        boolean isLockOK = redis.setnx("redis-lock", "123");
        // 1.1 为锁添加过期时间
//        redis.expire("redis-lock", 30);

        // 1. 获得锁的同时增加过期时间，保证原子性
        String selfId = UUID.randomUUID().toString();   // 加锁前生成各自的请求标识（也可以使用sid）
        boolean isLockOK = redis.setnx("redis-lock", selfId, 30);

        if (isLockOK) {
            // 2. 加锁成功，执行业务
//            this.doModify(companyInfoBO);
            // 4. 执行完毕后，释放锁
            String selfIdLock = redis.get("redis-lock");
            if (StringUtils.isNotBlank(selfIdLock) && selfIdLock.equals(selfId)) {
                redis.del("redis-lock");
            }

//            redis.del("redis-lock");
        } else {
            // 3. 加锁失败，重试
            // 不要立马重试，因为锁住的请求可能还没执行完毕，可以sleep 100~300ms
            Thread.sleep(200);

            System.out.println("setnx锁生效中，一会重试~");
            this.modifyCompanyInfo(companyInfoBO, 1);
        }
    }

    private void doModify(ModifyCompanyInfoBO companyInfoBO) {

        String companyId = companyInfoBO.getCompanyId();
        if (StringUtils.isBlank(companyId)) {
            GraceException.display(ResponseStatusEnum.COMPANY_INFO_UPDATED_ERROR);
        }

        Company pendingCompany = new Company();
        pendingCompany.setId(companyInfoBO.getCompanyId());
        pendingCompany.setUpdatedTime(LocalDateTime.now());

        BeanUtils.copyProperties(companyInfoBO, pendingCompany);

        companyMapper.updateById(pendingCompany);

        // 修改以后，删除企业的缓存信息
        redis.del(REDIS_COMPANY_MORE_INFO + ":" + companyId);
        redis.del(REDIS_COMPANY_BASE_INFO + ":" + companyId);
    }

    private ReentrantLock reentrantLock;

    @Autowired
    private RedissonClient redissonClient;


    @Transactional
    @Override
    public void modifyCompanyInfo(ModifyCompanyInfoBO companyInfoBO, Integer num) throws Exception {

        // 使用redissonClient获得名为xxx的锁
        String distLock = "redisson-lock";
//        RLock rLock = redissonClient.getLock(distLock);
        // 使用公平锁
        RLock rLock = redissonClient.getFairLock(distLock);
        // 加锁
        rLock.lock();
//        rLock.lock(10, TimeUnit.SECONDS);

        try {
            System.out.println("获得锁，执行业务~");
            // 加锁成功，执行业务
            Thread.sleep(20 * 1000);
//            this.doModify(companyInfoBO);
//            this.selfLock(rLock);
            System.out.println("num = " + num);
        } finally {
            // 解锁
            rLock.unlock();
        }
    }

    // 联锁
    private void multiLock() throws Exception {
        RLock lock1 = redissonClient.getLock("lock1");
        RLock lock2 = redissonClient.getLock("lock2");
        RLock lock3 = redissonClient.getLock("lock3");

        RedissonMultiLock lock = new RedissonMultiLock(lock1, lock2, lock3);

        lock.lock();

        try {
            System.out.println("业务处理");
        } finally {
            lock.unlock();
        }
    }

    // 测可重入锁
    private void selfLock(RLock rLock) throws Exception {
        rLock.lock();
        Thread.sleep(10 * 1000);
        System.out.println("执行第二个方法的业务...");
        rLock.unlock();
        Thread.sleep(10 * 1000);
    }

    @Override
    public void testReadLock() {
        RReadWriteLock lock = redissonClient.getReadWriteLock("redisson-rw-lock");
        lock.readLock().lock(15, TimeUnit.SECONDS);

        System.out.println("读取业务操作。。。");

//        lock.readLock().unlock();
    }

    @Override
    public void testWriteLock() {
        RReadWriteLock lock = redissonClient.getReadWriteLock("redisson-rw-lock");
        lock.writeLock().lock(15, TimeUnit.SECONDS);

        System.out.println("写入业务操作。。。");

//        lock.writeLock().unlock();
    }

    @Override
    public void testSemaphoreLock(Integer num) throws Exception {

        // 定义信号量，声明资源的数量（停车场的车位数，餐厅的饭桌数）
        RSemaphore semaphore = redissonClient.getSemaphore("eat-sema");
        semaphore.trySetPermits(3);

        semaphore.acquire();    // 获得资源数  --1
        System.out.println(num + "号客人" + "来吃饭了~");

//        boolean res = semaphore.tryAcquire();
//        if (!res) {
//            GraceException.display();
//        }

    }

    @Override
    public void testSemaphoreRelease(Integer num) throws Exception {
        RSemaphore semaphore = redissonClient.getSemaphore("eat-sema");
        semaphore.trySetPermits(3);

        semaphore.release();    // 释放资源    ++1
        System.out.println(num + "号客人" + "吃完走人~");
    }

    @Override
    public void testCountDownLatch() throws Exception {
        RCountDownLatch cdl = redissonClient.getCountDownLatch("H₂SO₄-Car");
        cdl.trySetCount(3);     // 资源数
        cdl.await();            // 等待全部资源数完成
    }

    @Override
    public void testDoneStep() throws Exception {
        RCountDownLatch cdl = redissonClient.getCountDownLatch("H₂SO₄-Car");
        cdl.countDown();        // 待处理的资源数 ++1
    }

    @Transactional
//    @Override
    public void modifyCompanyInfo3(ModifyCompanyInfoBO companyInfoBO, Integer num) throws Exception {

        String distLock = "redis-lock";
        String selfId = UUID.randomUUID().toString();
        Integer expireTimes = 30;

        while (redis.setnx(distLock, selfId, expireTimes)) {
            // 如果加锁失败，则重试循环
            System.out.println("setnx 锁生效中，一会重试~");
            Thread.sleep(50);
        }

        // 一旦获得锁，则开启新的timer执行定期检查，做lock的自动续期
        autoRefreshLockTimes(distLock, selfId, expireTimes);

        try {
            System.out.println("获得锁，执行业务~");
            // 加锁成功，执行业务
            Thread.sleep(40000);
            this.doModify(companyInfoBO);
        } finally {
            // 业务执行完毕，释放锁
//            String selfIdLock = redis.get(distLock);
//            if ( StringUtils.isNotBlank(selfIdLock) && selfIdLock.equals(selfId)) {
//                redis.del(distLock);
//            }

            // 使用LUA脚本执行删除key操作，为了保证原子性
            String lockScript =
                    " if redis.call('get',KEYS[1]) == ARGV[1] "
                            + " then "
                            +   " return redis.call('del',KEYS[1]) "
                            + " else "
                            +   " return 0 "
                            + " end "
                    ;
            long unLockResult = redis.execLuaScript(lockScript, distLock, selfId);
            if (unLockResult == 1) {
                lockTimer.cancel();
                System.out.println("释放锁，并且取消timer~");
            }
        }
    }

    private Timer lockTimer = new Timer();

    // 自动续期
    private void autoRefreshLockTimes(String distLock, String selfId, Integer expireTimes) {

        // if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('expire',KEYS[1],30) else return 0 end

        String refreshScript =
                " if redis.call('get',KEYS[1]) == ARGV[1] "
                        + " then "
                        +   " return redis.call('expire',KEYS[1],30) "
                        + " else "
                        +   " return 0 "
                        + " end "
                ;
        lockTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("自动续期，重置到30秒");
                redis.execLuaScript(refreshScript, distLock, selfId);
            }
        },
        expireTimes/3*1000,
        expireTimes/3*1000);
    }


    @Transactional
//    @Override
    public void modifyCompanyInfo2(ModifyCompanyInfoBO companyInfoBO, Integer num) throws Exception {

        String distLock = "redis-lock";

        // 1. 获得锁，值随意，只要不为空即可
//        boolean isLockOK = redis.setnx(distLock, "123");
        // 1.1 为锁添加过期时间
//        redis.expire(distLock, 30);

        // 1. 获得锁的同时增加一个标识符
        String selfId = UUID.randomUUID().toString();

        // 2. 加锁的同时设定过期时间，如此保证操作的原子性（要么全部成功，要么失败）
        boolean isLockOK = redis.setnx(distLock, selfId, 30);
        System.out.println("isLockOK = " + isLockOK);

        if (isLockOK) {
            // 2. 加锁成功，执行业务
            this.doModify(companyInfoBO);

            if (num!=null && num>1) {
                Thread.sleep(500);
            }

            // 4. 业务执行完毕，释放锁
            String selfIdLock = redis.get(distLock);
            if ( StringUtils.isNotBlank(selfIdLock) && selfIdLock.equals(selfId) ) {
                // 判断自己的标识符，只能有自己当前请求的线程来删除解锁
                redis.del(distLock);
            }
        } else {
            // 3. 加锁失败，重试

            // 不要立马递归或者死循环，因为锁住的请求可能还没有执行完毕，可以sleep
            Thread.sleep(50);

            System.out.println("setnx 锁生效中，一会重试~");
            this.modifyCompanyInfo(companyInfoBO, num);
        }

    }

    @Transactional
    @Override
    public void savePhotos(ModifyCompanyInfoBO companyInfoBO) {

        String companyId = companyInfoBO.getCompanyId();

        CompanyPhoto companyPhoto = new CompanyPhoto();
        companyPhoto.setCompanyId(companyId);
        companyPhoto.setPhotos(companyInfoBO.getPhotos());

        // 判断企业相册是否存在，不存在则插入，存在则修改
        CompanyPhoto tempPhoto = getPhotos(companyId);
        if (tempPhoto == null) {
            companyPhotoMapper.insert(companyPhoto);
        } else {
            companyPhotoMapper.update(companyPhoto,
                    new UpdateWrapper<CompanyPhoto>()
                            .eq("company_id", companyId)
            );
        }
    }

    @Override
    public CompanyPhoto getPhotos(String companyId) {
        return companyPhotoMapper.selectOne(
                new QueryWrapper<CompanyPhoto>()
                    .eq("company_id", companyId)
        );
    }

    @Override
    public List<Company> getByIds(List<String> companyIds) {
        return companyMapper.selectList(
                new QueryWrapper<Company>()
                        .in("id", companyIds)
        );
    }

    @Override
    public boolean getIsVip(String companyId) {

        boolean vipCompany = false;

        // 从redis中查询，如果存在直接返回即可
        String vipStr = redis.get(REDIS_COMPANY_IS_VIP + ":" + companyId);
        if (StringUtils.isNotBlank(vipStr)) {
            vipCompany = Boolean.valueOf(vipStr);
        } else {
            Company company = getById(companyId);
            if (company != null) {
                Integer isVip = company.getIsVip();
                LocalDate vipExpireDate = company.getVipExpireDate();

                if (vipExpireDate != null) {
                    long expireDays = LocalDateUtils.getChronoUnitBetween(LocalDate.now(),
                                                                        vipExpireDate,
                                                                        ChronoUnit.DAYS,
                                                                        false);
                    // isVip == 1 并且 过期时间 >= 当前日期
                    if (isVip == YesOrNo.YES.type && expireDays >= 0) {
                        vipCompany = true;
                    }
                }
            }
        }

        redis.set(REDIS_COMPANY_IS_VIP + ":" + companyId,
                    String.valueOf(vipCompany),
                    12 * 60 * 60);
        return vipCompany;
    }

    @Transactional
    @Override
    public void setCompanyVip(String companyId, LocalDate expireDate) {

        Company companyVip = new Company();
        companyVip.setId(companyId);
        companyVip.setVipExpireDate(expireDate);
        companyVip.setIsVip(YesOrNo.YES.type);
        companyVip.setUpdatedTime(LocalDateTime.now());

        companyMapper.updateById(companyVip);
    }
}

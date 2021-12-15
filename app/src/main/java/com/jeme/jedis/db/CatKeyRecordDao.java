package com.jeme.jedis.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/***
 * @date 3/29/21
 * @author jeme
 * @description 查询可以的记录操作集
 */
@Dao
public interface CatKeyRecordDao {

    /***
     * 新增/修改一条记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addRecord(CatKeyRecordEntity entity);

    /***
     * 根据小说id获取一条记录
     */
    @Query("SELECT * FROM cat_key_record ORDER BY update_time DESC LIMIT 100")
    List<CatKeyRecordEntity> queryAll();

}

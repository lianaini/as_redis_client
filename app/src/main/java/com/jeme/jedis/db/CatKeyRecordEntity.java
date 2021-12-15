package com.jeme.jedis.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/***
 * @date 3/29/21
 * @author jeme
 * @description 查询Key的记录表
 */
@Entity(tableName = "cat_key_record")
public class CatKeyRecordEntity {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private Long id;
    private String key;
    @ColumnInfo(name = "update_time")
    private Long updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }


}

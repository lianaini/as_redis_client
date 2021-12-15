package com.jeme.jedis.db;


import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.jeme.jedis.JApplication;

/***
 * @date 3/29/21
 * @author jeme
 * @description Jredis相关的数据库
 */
@Database(entities = {CatKeyRecordEntity.class}, version = 1, exportSchema = false)
public abstract class JRedisDB extends RoomDatabase {

    /***
     * 获取查询Key记录表的操作集
     */
    public abstract CatKeyRecordDao getCatKeyRecordDao();

    private static class INSTANCE {
        private static final JRedisDB _instance = Room.databaseBuilder(JApplication.app.getBaseContext()
                , JRedisDB.class,
                "j_redis.db")
                .allowMainThreadQueries()
//                .addMigrations(MIGRATION_1_2)
                .build();

    }

    public static JRedisDB getInstance() {
        return INSTANCE._instance;
    }

    static final Migration MIGRATION_1_2 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            //此处对于数据库中的所有更新都需要写下面的代码
//            database.execSQL("ALTER TABLE read_record "
//                    + " ADD COLUMN speaker_id TEXT");
        }
    };
}

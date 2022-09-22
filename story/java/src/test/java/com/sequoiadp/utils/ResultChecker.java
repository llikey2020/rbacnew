package com.sequoiadp.utils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Descreption 结果检查工具类
 * @Author zhongziming
 * @CreateDate 2022/7/7
 */
public class ResultChecker {

    /**
     * @param set
     * @param index
     *            start from 1
     * @param keyWord
     *            待查询关键字
     * @return 返回true表示指定结果集的特定字段包含查询的关键字，反之表示不包含。
     * @throws SQLException
     *             keyisexist
     */
    public static boolean isExistKeyInResult( ResultSet set, int[] index,
            String[] keyWord ) throws SQLException {
        if ( index.length != keyWord.length )
            throw new SQLException(
                    "index.length should equal with keyWord.length" );

        while ( set.next() ) {
            boolean res = true;
            for ( int i = 0; i < index.length; i++ ) {
                res = res
                        & set.getString( index[ i ] ).contains( keyWord[ i ] );
            }
            if ( res )
                return res;
        }
        return false;
    }
}

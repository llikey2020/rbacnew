package com.sequoiadp.rbac;

import org.testng.annotations.Test;

import java.sql.SQLException;

/**
 * @Descreption 如何实现TestBase的示例
 * @Author zhongziming
 * @CreateDate 2022/9/17
 */
public class TestExample_001 extends TestBase {

    @Override
    public void casePrepare() throws SQLException {
        userInfo.put( "sdbadmin", "sdbadmin" );
        userInfo.put( "test", "test" );

        // 如果使用组，则将此变量设置为true，否则不要修改
        useGroup = true;
    }

    @Override
    public void caseClean() throws SQLException {

    }

    @Test(skipFailedInvocations = true)
    public void test() throws SQLException {
        hiveConn.execDQL( "sdbadmin", "select * from db.table" );
    }
}

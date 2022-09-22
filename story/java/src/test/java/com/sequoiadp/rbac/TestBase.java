package com.sequoiadp.rbac;

import com.mysql.jdbc.Driver;
import com.sequoiadp.rbac.utils.SQLBuilder;
import com.sequoiadp.utils.JDBCConnectionProxy;
import org.apache.hive.jdbc.HiveDriver;
import org.testng.annotations.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Descreption 该类是所有测试类的父类
 * @Author zhongziming
 * @CreateDate 2022/7/7
 */
public abstract class TestBase {
    public static String S3Bucket;
    public static String hiveurl, mysqlurl;
    // adminUser是MySQL和rbac管理员的用户名，拥有最高权限，在MySQL至少要能新建用户和赋权
    public static String adminUser;
    private static String adminPassword;

    //dbName是默认的数据库名，如果需要多个数据库请自行定义
    public String dbName;
    //testGroup是默认的组名，如果需要多个组请自行定义
    public String testGroup=null;
    //tbName, vwName是默认的表名和视图名
    protected String tbName, vwName;
    protected boolean useGroup=false;

    protected JDBCConnectionProxy hiveConn;
    private JDBCConnectionProxy mysqlConn;
    protected Map<String,String> userInfo;

    @Parameters({ "HIVEADDRESS", "MYSQLADDRESS", "ROOTUSER", "ROOTPASSWORD",
             "S3BUCKET" })
    @BeforeSuite(alwaysRun = true)
    public static void initSuite( String HIVEADDRESS, String MYSQLADDRESS,
            String ROOTUSER, String ROOTPASSWORD,
            String S3BUCKET ) {
        S3Bucket = S3BUCKET;
        hiveurl = "jdbc:hive2://" + HIVEADDRESS;
        mysqlurl = "jdbc:mysql://" + MYSQLADDRESS;

        adminUser = ROOTUSER;
        adminPassword=ROOTPASSWORD;
    }

    @AfterSuite
    public void cleanSuite() throws SQLException {
    }

    /**
     * 每个测试类执行前，至少进行如下准备： 根据自身测试编号确定所需使用的表名和视图名（这个可以在用例中自行再添加更多的表）
     * 获取hive的管理员权限的statement 获取hive的测试用户权限的statement 将测试数据库的usage权限赋予测试用户
     *
     * 删除旧的测试数据库，并新建测试数据库
     *
     * @throws SQLException
     */
    @BeforeTest
    public void initClass() throws SQLException {
        String className = this.getClass().getSimpleName();
        tbName = "table" + className.substring( className.lastIndexOf( '_' ) );
        vwName = "view" + className.substring( className.lastIndexOf( '_' ) );
        dbName = "database" + className.substring( className.lastIndexOf( '_' ) );

        mysqlConn = new JDBCConnectionProxy( Driver.class, mysqlurl );
        mysqlConn.addUser( adminUser, adminPassword );

        hiveConn = new JDBCConnectionProxy( HiveDriver.class, hiveurl );
        hiveConn.addUser( adminUser, adminPassword );

        //删除旧的测试数据库，并新建测试数据库
        String clensdb = "drop database if exists " + dbName + " cascade";
        hiveConn.execDDL( adminUser, clensdb );
        String createdbsql = "create database if not exists " + dbName;
        hiveConn.execDDL( adminUser, createdbsql );

        userInfo=new HashMap<>();
        casePrepare();
        //自行声明要用的用户并注册
        addUser(userInfo);

        if(useGroup){
            addGroup();
        }
    }

    /**
     * 每个测试类执行后，至少进行如下清理： 关闭hive的管理员权限的statement 关闭hive的测试用户权限的statement
     * 收回赋予测试用户的测试数据库的usage权限 收回赋予测试组的测试数据库的usage权限
     * 
     * @throws SQLException
     */
    @AfterTest
    public void cleanClass() throws SQLException {
        caseClean();
        dropUser( userInfo );
        mysqlConn.close();

        try {
            String clensdb = "drop database if exists " + dbName + " cascade";
            hiveConn.execDDL( adminUser, clensdb );
            //自动删除测试组
            if(testGroup!=null)
                hiveConn.execDDL( adminUser, SQLBuilder.dropGroupSQL( testGroup ) );
        } finally {
            hiveConn.close();
        }
    }

    public abstract void casePrepare() throws SQLException;

    public abstract void caseClean() throws SQLException;

    /**
     * @param user
     * @return 获取一个user的权限集合
     * @throws SQLException
     */
    protected ResultSet getUserGrant( String user ) throws SQLException {
        return hiveConn.execDQL( adminUser, SQLBuilder.showGrantSQL( "user", user ) );
    }

    /**
     * @param group
     * @return 获取一个group的权限集合
     * @throws SQLException
     */
    protected ResultSet getGroupGrant( String group ) throws SQLException {
        return hiveConn.execDQL( adminUser,
                SQLBuilder.showGrantSQL( "group", group ) );
    }

    /**
     * 每个用例自行增加用户的方法
     * 
     * @param userInfo
     * @throws SQLException
     */
    protected void addUser( Map< String, String > userInfo )
            throws SQLException {
        for ( Map.Entry< String, String > entry : userInfo.entrySet() ) {
            mysqlConn.execDDL( adminUser, "create user " + entry.getKey()
                    + " identified by " + entry.getValue() );

            hiveConn.addUser( entry.getKey(), entry.getValue() );
        }
    }

    /**
     * 每个用例自行删除用户的方法
     *
     * @param userInfo
     * @throws SQLException
     */
    protected void dropUser( Map< String, String > userInfo )
            throws SQLException {
        for ( Map.Entry< String, String > entry : userInfo.entrySet() ) {
            mysqlConn.execDDL( adminUser, "drop user " + entry.getKey()
                     );
        }
    }

    /**
     * 如果测试中需要一个组，请务必调用此方法，组名记录在testGroup变量中
     * 删除旧的group，并重新新建group，并将测试数据库的usage权限赋予测试组
     * 这个组不需要使用者手动清理
     * @throws SQLException
     */
    protected void addGroup()
            throws SQLException {
        testGroup="group"+this.getClass().getSimpleName().substring( this.getClass().getSimpleName().lastIndexOf( '_' ) );

        //目前删除不存在的组不会报错，如果未来rbac行为改变，这里需要增加捕获异常
        hiveConn.execDDL( adminUser, SQLBuilder.dropGroupSQL( testGroup ) );
        hiveConn.execDDL( adminUser, SQLBuilder.createGroupSQL( testGroup ) );

        hiveConn.execDDL( adminUser,
                SQLBuilder.grantDatabaseSQL( "usage", dbName,"group", testGroup ) );
    }
}

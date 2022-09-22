package com.sequoiadp.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * @Descreption 提供到jdbc连接，以及注册用户相关信息
 * @Author zhongziming
 * @CreateDate 2022/6/21
 */
public class JDBCConnectionProxy {
    private Collection< Connection > connSet;
    private Map< String, Statement > userStmts;
    private Class< java.sql.Driver > driverClass;
    private String url;

    public JDBCConnectionProxy(Class Jclass, String url ) {
        connSet = new ArrayList<>();
        userStmts = new HashMap<>();
        driverClass = Jclass;
        this.url = url;
    }

    /**
     * @param userInfo
     *            map for username and password
     * @throws SQLException
     */
    public void addUsers( Map< String, String > userInfo ) throws SQLException {
        for ( Map.Entry< String, String > entry : userInfo.entrySet() ) {
            addUser( entry.getKey(), entry.getValue() );
        }
    }

    public void addUser( String username, String password )
            throws SQLException {
        Connection conn = getConnect( username, password );
        Statement stmt = conn.createStatement();

        connSet.add( conn );
        userStmts.put( username, stmt );
    }

    private Connection getConnect( String user, String pwd ) {
        Connection conn = null;
        try {
            Properties info = new Properties();
            info.put( "user", user );
            info.put( "password", pwd );
            conn = driverClass.newInstance().connect( url, info );
        } catch ( SQLException e ) {
            e.printStackTrace();
        } catch ( IllegalAccessException e ) {
            e.printStackTrace();
        } catch ( InstantiationException e ) {
            e.printStackTrace();
        }
        return conn;
    }

    public void execDDL(String user, String sql ) throws SQLException {
        if ( userStmts.containsKey( user ) )
            userStmts.get( user ).execute( sql );
    }

    public ResultSet execDQL(String user, String sql ) throws SQLException {
        return userStmts.get( user ).executeQuery( sql );
    }

    public int execDML(String user, String sql ) throws SQLException {
        return userStmts.get( user ).executeUpdate( sql );
    }

    public void close() throws SQLException {
        for ( Statement stmt : userStmts.values() ) {
            stmt.close();
        }

        for ( Connection conn : connSet ) {
            conn.close();
        }
    }
}

package com.sequoiadp.rbac.utils;

import com.sequoiadp.rbac.TestBase;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Descreption 拼接SQL
 * @Author zhongziming
 * @CreateDate 2022/7/7
 */
public class SQLBuilder {
    public static String grantTableSQL( String privilege, String dbName,
            String tableName, String userType, String username ) {
        return String.format( "grant %s on table %s.%s to %s %s", privilege,
                dbName, tableName, userType, username );
    }

    public static String grantViewSQL( String privilege, String dbName,
            String viewName, String userType, String username ) {
        return String.format( "grant %s on view %s.%s to %s %s", privilege,
                dbName, viewName, userType, username );
    }

    /**
     * @param privilege
     * @param dbName
     * @param userType
     * @param username
     * @return grant default database
     */
    public static String grantDatabaseSQL( String privilege, String dbName,
            String userType, String username ) {
        return String.format( "grant %s on database %s to %s %s", privilege,
                dbName, userType, username );
    }

    public static String revokeTableSQL( String privilege, String dbName,
            String tableName, String userType, String username ) {
        return String.format( "revoke %s on table %s.%s to %s %s", privilege,
                dbName, tableName, userType, username );
    }

    public static String revokeViewSQL( String privilege, String dbName,
            String viewName, String userType, String username ) {
        return String.format( "revoke %s on view %s.%s to %s %s", privilege,
                dbName, viewName, userType, username );
    }

    /**
     * @param privilege
     * @param dbName
     * @param userType
     * @param username
     * @return revoke default database
     */
    public static String revokeDatabaseSQL( String privilege, String dbName,
            String userType, String username ) {
        return String.format( "revoke %s on database %s to %s %s", privilege,
                dbName, userType, username );
    }

    /**
     * @param objType
     * @param dbName
     * @param objName
     * @param userType
     * @param userName
     * @return owner obj to user
     */
    public static String alterOwnerSQL( String objType, String dbName, String objName,
            String userType, String userName ) {
        return String.format( "alter $s %s.%s owner to %s %s", objType, dbName,
                objName, userType, userName );
    }

    /**
     * @param userType
     * @param userName
     * @return show grants for {} {}
     */
    public static String showGrantSQL(String userType, String userName ) {
        return "show grants for " + userType + ' ' + userName;
    }

    /**
     * create table {tablename} (id int,sn string)
     *
     * @param tablename
     */
    public static String createTableSQL( String dbName, String tablename ) {
        LinkedHashMap< String, String > columnType = new LinkedHashMap<>();
        columnType.put( "id", "int" );
        columnType.put( "sn", "string" );
        return createTableSQL( dbName, tablename, columnType );
    }

    /**
     * create table {tablename} ({columnType})
     *
     * @param dbName
     * @param tablename
     * @param columnType
     */
    public static String createTableSQL(String dbName, String tablename,
                                        LinkedHashMap< String, String > columnType ) {
        StringBuffer str = new StringBuffer();
        for ( Map.Entry< String, String > entry : columnType.entrySet() ) {
            str.append( entry.getKey() ).append( ' ' )
                    .append( entry.getValue() ).append( ',' );
        }

        return String.format(
                "create table %s.%s ( %s ) using delta location \"s3a://%s/%s\"",
                dbName, tablename, str.toString(), TestBase.S3Bucket,
                tablename );
    }

    /**
     * create view {viewname} as select * from {tablename}
     * 
     * @param dbName
     * @param viewName
     * @param tableName
     */
    public static String createViewSQL( String dbName, String viewName,
            String tableName ) {
        return String.format( "create view %s.%s as select * from %s.%s",
                dbName, viewName, dbName, tableName );
    }

    public static String createGroupSQL(String groupName ) {
        return "create group " + groupName;
    }

    public static String dropTableSQL( String dbName, String tableName ) {
        return "drop table if exists " + dbName + '.' + tableName;
    }

    public static String dropViewSQL( String dbName, String viewName ) {
        return "drop view if exists " + dbName + '.' + viewName;
    }

    public static String dropGroupSQL(String groupName ) {
        return String.format( "drop group %s", groupName );
    }

    public static String addUserInGroupSQL( String group, String user ) {
        return String.format( "alter group %s add user %s", group, user );
    }

    public static String dropUserInGroupSQL( String group, String user ) {
        return String.format( "alter group %s drop user %s", group, user );
    }

    public static String addGroupInGroupSQL( String group, String added ) {
        return String.format( "alter group %s add group %s", group, added );
    }

    public static String dropGroupInGroupSQL( String group, String dropped ) {
        return String.format( "alter group %s drop group %s", group, dropped );
    }
}

# RBAC Test Framework

About rbac's testing framework


这是一个测试 rbac 的简单测试框架。 主要通过hive jdbc连接thriftserver，然后将需要的sql传递给thriftserver，配合testng框架判断并执行结果。

#### 背景

RBAC 是为 Spark SQL 提供的库表级权限控制服务。通过将 Spark SQL 中的数据库和表抽象为资源，分组抽象为角色，各种 SQL 语句抽象为行为，从而建立起了基于角色的访问控制模型，并以此进行权限控制，提供了用户对于 Spark SQL 特定功能进行授权及管理操作的能力， 目前我们的rbac和spark thriftserver交互，所以我们的自动化测试是通过hive的jdbc连接thriftserver，输入我们需要的sql，最后验证结果。



#### 依赖

目前spark需要关闭鉴权 才能够通过jdbc连接到thriftserver


#### 结构和功能

HiveConnection类：简单封装了测试用例需要的sql拼接和通过hive jdbc连接spark-thriftserver，

TestBase：测试的基础类，主要做了testng的参数传递，和测试环境的准备问题

@BeforeSuite：参数的传递工作

@BeforeClass:初始化每一条用例的测试环境，包括table,usage权限，group的创建

@AfterClass:删除测试用例用的table,group等操作



#### 编写规范

1. 测试时无论如何至少需要两个用户，一个symbol为admin的管理员用户，一个symbol为test的普通用户。
2. 通过用例类名可以明显的看出对应testlink哪一条手工用例，可以使用特性名+testlink编号(用例id)的方式来命名。
3. 所有用例类都应该继承TestBase。
3. 所有公共方法都是围绕着TestBase中的dbName设计的（虽然暂时没有达到），因此尽量不要用单独创建的数据库实现用例，否则将会自绝于公共方法。
4. 当预期结果是出现异常的时候，如果包含多条执行语句，尽量使用Assert.assertThrows进行精确捕获；如果只有一条执行语句，
   可以用注解中标记“expectedExceptions”的方式抛出；其他非预期异常允许直接抛出。
5. @Test主测试用例的方法下需要实现两个用户的连接，通过用例的内容，来通过管理员grant,测试用户来进行校验。
6. 配置文件为testng.xml，所有参数均有注释。
7. 测试报告路径：target\test-output
8. TestBase中包含initConn()与closeConn()两个方法，分别在测试前后运行，左右是创建、关闭一个管理员和一个普通用户的连接。如果还有任何初始化操
   作需要执行，可以在测试类中重载这两个方法，但是需要在重载方法的相应位置用super.initConn()或super.closeConn()调用父类方法。
9. 默认symbol为test的用户和testGroup拥有对默认database的usage权限，该权限会在结束测试时被收回，如果测试中需要撤销请手动撤销。 


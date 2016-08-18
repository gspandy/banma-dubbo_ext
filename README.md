# 功能
* 为dubbo增加cat监控，远程call，service埋点。
* mybatis埋点，监控sql执行的时间和内容。

# 如何引入
1. 引入jar包：com.zebra.carcloud:dubbo_ext:1.0.0-SNAPSHOT（最新的即可）
2. dubbo埋点不需要做任何东西
3. mybatis需要在mybatis.xml文件中增加插件`<plugin interceptor="com.zebra.carcloud.mybatisExt.Interceptor.SqlInterceptor"></plugin>`

# 必须的配置文件，这是cat的配置
* 需要在classpath下面增加META-INF/app.properties文件，文件内容为app.name=项目名（请注意不要重复）。比如 app.name=xiaoyiobd-service

# 应用日志配置
* 目前支持只支持log4j，增加一个appender。`<appender name="catAppender" class="com.dianping.cat.log4j.CatAppender"></appender>`,别忘了
在root上增加 `<appender-ref ref="cat"/>`
# 如何查看是否已经配置成功并上线
1. 打开cat服务器状态页面 http://139.196.14.168:2281/cat/r/state 账号：zebraCar 密码：mko0,lp-.;[=
2. 能够在项目列表中看到自己的项目说明已经配置成功
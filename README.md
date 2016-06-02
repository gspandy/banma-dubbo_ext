# 功能
* 为dubbo增加cat监控，远程call，service埋点。
* mybatis埋点。

# 如何引入
1. 引入jar包：com.zebra.carcloud:dubbo_ext:1.0.0-SNAPSHOT（最新的即可）
2. dubbo不需要做任何东西
3. mybatis需要在mybatis.xml文件中增加插件`<plugin interceptor="com.zebra.carcloud.mybatisExt.Interceptor.SqlInterceptor">
                                            <property name="someProperty" value="100"/>
                                        </plugin>`

# 必须的配置文件，这是cat的配置
* 需要在classpath下面增加META-INF/app.properties文件，文件内容为app.name=项目名（请注意不要重复）。比如 app.name=xiaoyiobd-service
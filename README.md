#功能
为dubbo增加cat监控，远程call，service埋点
#如何引入
<dependency>
    <artifactId>dubbo_ext</artifactId>
    <groupId>com.zebra.carcloud</groupId>
    <version>1.0.0-SNAPSHOT（最新的即可）</version>
</dependency>
#必须的配置文件
需要在classpath下面增加META-INF/app.properties文件，文件内容为app.name=项目名（请注意不要重复）。比如 app.name=xiaoyiobd-service
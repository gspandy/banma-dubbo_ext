package com.zebra.carcloud.cat;

/**
 * Created by ying on 16/6/3.
 * 扩展的cat埋点字段
 */
public class CatConstantsExt {
    //绑定传递的服务名字
    public static final String SERVER_APP_NAME_KEY = "serverAppName";
    public static final String CLIENT_APP_NAME_KEY = "clientAppName";

    //CROSS 默认的埋点名字
    public  static final String TYPE_CRYOSS_CONSUMER = "PigeonCall";
    public static final String TYPE_CROSS_SERVER = "PigeonService";

    //CROSS客户端埋点相关名字
    public static final String TYPE_CLIENT_CALL_APP = "PigeonCall.app";//调取的服务名字
    public static final String TYPE_CLIENT_CALL_SERVER = "PigeonCall.server";//调取的地址
    public static final String TYPE_CLIENT_CALL_PORT = "PigeonCall.port";//调取的端口

    //CROSS服务端埋点相关名字
    public static final String TYPE_SERVER_CALL_APP = "PigeonService.app";//服务的名字
    public static final String TYPE_SERVER_CALL_CLIENT = "PigeonService.client";//服务的地址

    public static final String TYPE_BUSSINESS_ERRO = "bussinessError";//业务异常
}

package com.zebra.carcloud.dubboExt.filter.cat;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.fastjson.JSONObject;
import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultEvent;
import com.zebra.carcloud.cat.CatConstantsExt;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Created by ying on 16/5/30.
 */
@Activate(group = Constants.CONSUMER,order = -9000)
public class CatConsumerFilter implements Filter {
    private final Logger logger = Logger.getLogger(CatConsumerFilter.class);

    public CatConsumerFilter(){
        Cat.getProducer();
    }
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if(Cat.isInitialized()){
            RpcContext rpcContext = RpcContext.getContext();
            Map<String,String> attachments = rpcContext.getAttachments();

            if(logger.isDebugEnabled()) {
                logger.debug("===========>consumerFilter:invoker:"+JSONObject.toJSONString(invoker));
                logger.debug("===========>consumerFilter:invocation:"+JSONObject.toJSONString(invocation));
                logger.debug("===========>consumerFilter:rpcContext:"+ JSONObject.toJSONString(RpcContext.getContext()));
            }

            StringBuilder sb = new StringBuilder();
            sb.append("dubbo:")
                    .append(invoker.getInterface().getSimpleName())
                    .append(".")
                    .append(invocation.getMethodName());

            Transaction t = Cat.getProducer().newTransaction(CatConstants.TYPE_CALL, sb.toString());


            //cross调用
//            Cat.logEvent(CatConstantsExt.TYPE_CLIENT_CALL_APP,getProviderName(invoker));
//            Cat.logEvent(CatConstantsExt.TYPE_CLIENT_CALL_SERVER, invoker.getUrl().getHost());
//            Cat.logEvent(CatConstantsExt.TYPE_CLIENT_CALL_PORT,String.valueOf(invoker.getUrl().getPort()));
            Event clientCallAppEvent = new DefaultEvent(CatConstantsExt.TYPE_CLIENT_CALL_APP,getProviderName(invoker));
            clientCallAppEvent.setStatus(Message.SUCCESS);
            clientCallAppEvent.complete();
            t.addChild(clientCallAppEvent);

            Event clientCallServerEvent = new DefaultEvent(CatConstantsExt.TYPE_CLIENT_CALL_SERVER,invoker.getUrl().getHost());
            clientCallServerEvent.setStatus(Message.SUCCESS);
            clientCallServerEvent.complete();
            t.addChild(clientCallServerEvent);

            Event clientCallPort = new DefaultEvent(CatConstantsExt.TYPE_CLIENT_CALL_PORT,String.valueOf(invoker.getUrl().getPort()));
            clientCallPort.setStatus(Message.SUCCESS);
            clientCallPort.complete();
            t.addChild(clientCallPort);
            //cross


            CatContext ctx = new CatContext();
            Cat.logRemoteCallClient(ctx);
            attachments.put(Cat.Context.ROOT, ctx.getProperty(Cat.Context.ROOT));
            attachments.put(Cat.Context.PARENT, ctx.getProperty(Cat.Context.PARENT));
            attachments.put(Cat.Context.CHILD, ctx.getProperty(Cat.Context.CHILD));

            //通知服务端客户端名字
//            attachments.put(CatConstantsExt.CLIENT_APP_NAME_KEY,invoker.getUrl().getParameter(Constants.APPLICATION_KEY));
            attachments.put(CatConstantsExt.CLIENT_APP_NAME_KEY,Cat.getManager().getDomain());

            Result result = null;
            try {
                result = invoker.invoke(invocation);

                if(result.hasException()){
                    Throwable throwable = result.getException();
                    if(RpcException.class == throwable.getClass()){//RpcException因为封装了其他异常 所以拆开处理
                        Throwable causeby = throwable.getCause();
                        if(causeby != null){
                            t.setStatus(causeby);
                            Cat.logError(causeby.getMessage(),causeby);
                        }else{
                            t.setStatus(throwable);
                            Cat.logError(throwable.getMessage(),throwable);
                        }
                    }else{
                        t.setStatus(throwable);
                        Cat.logError(throwable.getMessage(),throwable);
                    }
                }else {
                    t.setStatus(Transaction.SUCCESS);
                }

            } catch (RuntimeException e) {
                if(RpcException.class == e.getClass()){
                    Throwable causeby = e.getCause();
                    if(causeby != null){
                        t.setStatus(causeby);
                        Cat.logError(causeby.getMessage(), causeby);
                    }else{
                        t.setStatus(e);
                        Cat.logError(e.getMessage(),e);
                    }
                }else{
                    t.setStatus(e);
                    Cat.logError(e.getMessage(), e);
                }
                throw e;
            } finally {
                t.complete();
            }
            return result;
        }else{
            return invoker.invoke(invocation);
        }
    }

    /**
     * 获取服务名字
     * @param invoker
     * @return
     */
    private String getProviderName(Invoker invoker){
        String providerName;

        providerName = invoker.getUrl().getParameter(CatConstantsExt.SERVER_APP_NAME_KEY);

        if(providerName == null || providerName.length() == 0){//如果获取不到服务名字，直接截取接口名最后一个.前面部分
//            String interfaceName = invoker.getInterface().getName();
            String interfaceName = invoker.getUrl().getServiceInterface();//使用serviceInterface,防止泛化调用的时候显示不正确
            providerName = interfaceName.substring(0,interfaceName.lastIndexOf("."));
        }

        return providerName;
    }
}

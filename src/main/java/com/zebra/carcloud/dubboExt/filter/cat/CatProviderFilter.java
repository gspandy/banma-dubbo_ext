package com.zebra.carcloud.dubboExt.filter.cat;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.fastjson.JSONObject;
import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultEvent;
import com.zebra.carcloud.cat.CatConstantsExt;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Created by ying on 16/5/30.
 */
@Activate(group = Constants.PROVIDER ,order = -9000)
public class CatProviderFilter implements Filter {
    private final Logger logger = Logger.getLogger(CatProviderFilter.class);

    public CatProviderFilter(){
        Cat.getProducer();
    }
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        RpcContext rpcContext = RpcContext.getContext();
        Map<String,String> attachments = rpcContext.getAttachments();

//        if(logger.isDebugEnabled()) {
//            logger.debug("===========>providerFilter:" + attachments.get(Cat.Context.PARENT) + "|" + attachments.get(Cat.Context.CHILD) + "|" + attachments.get(Cat.Context.ROOT));
//            logger.debug("===========>providerFilter:invoker:"+ JSONObject.toJSONString(invoker));
//            logger.debug("===========>providerFilter:invocation:"+JSONObject.toJSONString(invocation));
//            logger.debug("===========>providerFilter:rpcContext:"+ JSONObject.toJSONString(RpcContext.getContext()));
//        }


        if(Cat.isInitialized()) {

            StringBuilder sb = new StringBuilder();
            sb.append("dubbo:")
                    .append(invoker.getInterface().getSimpleName())
                    .append(".")
                    .append(invocation.getMethodName());

            Transaction t = Cat.newTransaction(CatConstants.TYPE_SERVICE, sb.toString());

            //cross
            String consumerAppName = RpcContext.getContext().getAttachment(CatConstantsExt.CLIENT_APP_NAME_KEY);
            if(consumerAppName == null || consumerAppName.length() == 0){
                consumerAppName= RpcContext.getContext().getRemoteHost()+":"+ RpcContext.getContext().getRemotePort();
            }
//            Cat.logEvent(CatConstantsExt.TYPE_SERVER_CALL_APP,consumerAppName);
//            Cat.logEvent(CatConstantsExt.TYPE_SERVER_CALL_CLIENT, invoker.getUrl().getHost());
            Message serverCallAppEvent = new DefaultEvent(CatConstantsExt.TYPE_SERVER_CALL_APP,consumerAppName);
            serverCallAppEvent.setStatus(Message.SUCCESS);
            serverCallAppEvent.complete();
            t.addChild(serverCallAppEvent);

            Message serverCallClient = new DefaultEvent(CatConstantsExt.TYPE_SERVER_CALL_CLIENT, invoker.getUrl().getHost());
            serverCallClient.setStatus(Message.SUCCESS);
            serverCallClient.complete();
            t.addChild(serverCallClient);
            //cross

            CatContext ctx = new CatContext();
            ctx.addProperty(Cat.Context.PARENT, attachments.get(Cat.Context.PARENT));
            ctx.addProperty(Cat.Context.CHILD, attachments.get(Cat.Context.CHILD));
            ctx.addProperty(Cat.Context.ROOT, attachments.get(Cat.Context.ROOT));
            Cat.logRemoteCallServer(ctx);

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
}

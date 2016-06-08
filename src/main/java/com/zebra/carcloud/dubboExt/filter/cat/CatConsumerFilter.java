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

            Event clientCallServerEvent = new DefaultEvent(CatConstantsExt.TYPE_CLIENT_CALL_SERVER,getProviderName(invoker));
            clientCallServerEvent.setStatus(Message.SUCCESS);
            clientCallServerEvent.complete();
            t.addChild(clientCallServerEvent);

            Event clientCallPort = new DefaultEvent(CatConstantsExt.TYPE_CLIENT_CALL_PORT,getProviderName(invoker));
            clientCallPort.setStatus(Message.SUCCESS);
            clientCallPort.complete();
            t.addChild(clientCallPort);
            //cross


            CatContext ctx = new CatContext();
            Cat.logRemoteCallClient(ctx);
            attachments.put(Cat.Context.ROOT, ctx.getProperty(Cat.Context.ROOT));
            attachments.put(Cat.Context.PARENT, ctx.getProperty(Cat.Context.PARENT));
            attachments.put(Cat.Context.CHILD, ctx.getProperty(Cat.Context.CHILD));
            attachments.put(CatConstantsExt.CLIENT_APP_NAME_KEY,invoker.getUrl().getParameter(Constants.APPLICATION_KEY));

            Result result = null;
            try {
                result = invoker.invoke(invocation);
                t.setStatus(Transaction.SUCCESS);
            } catch (RuntimeException e) {
                logger.error(e);
                t.setStatus(e);
                Cat.logError(e.getMessage(),e);
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
            String interfaceName = invoker.getInterface().getName();
            providerName = interfaceName.substring(0,interfaceName.lastIndexOf("."));
        }

        return providerName;
    }
}

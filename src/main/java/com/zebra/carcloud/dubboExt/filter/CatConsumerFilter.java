package com.zebra.carcloud.dubboExt.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Created by ying on 16/5/30.
 */
@Activate(group = Constants.CONSUMER)
public class CatConsumerFilter implements Filter {
    private final Logger logger = Logger.getLogger(CatConsumerFilter.class);

    public CatConsumerFilter(){
        Cat.getProducer();
    }
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Map<String,String> attachments = invocation.getAttachments();
        if(Cat.isInitialized()){

            StringBuilder sb = new StringBuilder(invoker.getInterface().getSimpleName());
            sb.append(":").append(invocation.getMethodName());
            Transaction t = Cat.getProducer().newTransaction("DubboCall", sb.toString());

            RemoteContext ctx = new RemoteContext();
            Cat.logRemoteCallClient(ctx);
            attachments.put("rId", ctx.getProperty(Cat.Context.ROOT));
            attachments.put("pId", ctx.getProperty(Cat.Context.PARENT));
            attachments.put("cId", ctx.getProperty(Cat.Context.CHILD));

            Result result = null;
            try {
                result = invoker.invoke(invocation);
                t.setStatus(Transaction.SUCCESS);
            } catch (RuntimeException e) {
                Cat.logEvent("RemoteCall","callInfo", "",invocation.toString());
                logger.error(e);
                t.setStatus(e);
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

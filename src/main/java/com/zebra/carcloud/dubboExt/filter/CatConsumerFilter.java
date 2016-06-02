package com.zebra.carcloud.dubboExt.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
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
        if(Cat.isInitialized()){
            Map<String,String> attachments = RpcContext.getContext().getAttachments();

            if(logger.isDebugEnabled()) {
                logger.debug("===========>consumerFilter");
            }

            StringBuilder sb = new StringBuilder();
            sb.append("dubbo:")
                    .append(invoker.getInterface().getSimpleName())
                    .append(".")
                    .append(invocation.getMethodName());

            Transaction t = Cat.getProducer().newTransaction(CatConstants.TYPE_CALL, sb.toString());

            CatContext ctx = new CatContext();
            Cat.logRemoteCallClient(ctx);
            attachments.put(Cat.Context.ROOT, ctx.getProperty(Cat.Context.ROOT));
            attachments.put(Cat.Context.PARENT, ctx.getProperty(Cat.Context.PARENT));
            attachments.put(Cat.Context.CHILD, ctx.getProperty(Cat.Context.CHILD));

            Result result = null;
            try {
                result = invoker.invoke(invocation);
                t.setStatus(Transaction.SUCCESS);
            } catch (RpcException e) {
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
}

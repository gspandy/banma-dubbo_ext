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
@Activate(group = Constants.PROVIDER)
public class CatProviderFilter implements Filter {
    private final Logger logger = Logger.getLogger(CatProviderFilter.class);

    public CatProviderFilter(){
        Cat.getProducer();
    }
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Map<String,String> attachments = RpcContext.getContext().getAttachments();

        if(logger.isDebugEnabled()) {
            logger.debug("===========>consumerFilter:" + attachments.get(Cat.Context.PARENT) + "|" + attachments.get(Cat.Context.CHILD) + "|" + attachments.get(Cat.Context.ROOT));
        }


        if(Cat.isInitialized()) {
            CatContext ctx = new CatContext();
            ctx.addProperty(Cat.Context.PARENT, attachments.get(Cat.Context.PARENT));
            ctx.addProperty(Cat.Context.CHILD, attachments.get(Cat.Context.CHILD));
            ctx.addProperty(Cat.Context.ROOT, attachments.get(Cat.Context.ROOT));
            Cat.logRemoteCallServer(ctx);

            StringBuilder sb = new StringBuilder();
            sb.append("dubbo:")
                    .append(invoker.getInterface().getSimpleName())
                    .append(".")
                    .append(invocation.getMethodName());

            Transaction t = Cat.newTransaction(CatConstants.TYPE_SERVICE, sb.toString());
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

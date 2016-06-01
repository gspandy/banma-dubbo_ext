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
@Activate(group = Constants.PROVIDER)
public class CatProviderFilter implements Filter {
    private final Logger logger = Logger.getLogger(CatProviderFilter.class);

    public CatProviderFilter(){
        Cat.getProducer();
    }
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Map<String,String> attachments = invocation.getAttachments();
//        System.out.println("---------------------------->" + attachments.get("pId")+"|"+attachments.get("cId")+"|"+attachments.get("rId"));
        if(Cat.isInitialized()) {
            RemoteContext ctx = new RemoteContext();
            ctx.addProperty(Cat.Context.PARENT, attachments.get("pId"));
            ctx.addProperty(Cat.Context.CHILD, attachments.get("cId"));
            ctx.addProperty(Cat.Context.ROOT, attachments.get("rId"));
            Cat.logRemoteCallServer(ctx);

            StringBuilder sb = new StringBuilder(invoker.getInterface().getSimpleName());
            sb.append(":").append(invocation.getMethodName());

            Transaction t = Cat.newTransaction("RemoteService", sb.toString());
            Result result = null;
            try {
                result = invoker.invoke(invocation);
                t.setStatus(Transaction.SUCCESS);
            } catch (Exception e) {
                logger.error(e);
                t.setStatus(e);
            } finally {
                t.complete();
            }

            return result;
        }else{
            return invoker.invoke(invocation);
        }
    }
}

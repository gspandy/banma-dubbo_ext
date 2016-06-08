package com.zebra.carcloud.dubboExt.filter.cat;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.Registry;
import com.dianping.cat.Cat;
import com.zebra.carcloud.cat.CatConstantsExt;

import java.util.List;

/**
 * Created by ying on 16/6/3.
 */
public class ParamRegistryWrapper implements Registry{
    private Registry registry;

    public ParamRegistryWrapper(Registry registry){
        this.registry = registry;
    }

    /**
     * 注册的时候增加些参数
     * @param url
     * @return
     */
    private URL appendParam(URL url){
        String side = url.getParameter(Constants.SIDE_KEY);
        if(Constants.PROVIDER_SIDE.equals(side)){
//            return url.addParameter(CatConstantsExt.SERVER_APP_NAME_KEY,url.getParameter(Constants.APPLICATION_KEY));
            //修改为获取cat定义的appname
            return url.addParameter(CatConstantsExt.SERVER_APP_NAME_KEY,Cat.getManager().getDomain());
        }

        return url;
    }

    @Override
    public URL getUrl() {
        return appendParam(this.registry.getUrl());
    }

    @Override
    public boolean isAvailable() {
        return this.registry.isAvailable();
    }

    @Override
    public void destroy() {
        this.registry.destroy();
    }

    @Override
    public void register(URL url) {
        this.registry.register(appendParam(url));
    }

    @Override
    public void unregister(URL url) {
        this.registry.unregister(appendParam(url));
    }

    @Override
    public void subscribe(URL url, NotifyListener notifyListener) {
        this.registry.subscribe(appendParam(url), notifyListener);
    }

    @Override
    public void unsubscribe(URL url, NotifyListener notifyListener) {
        this.registry.unsubscribe(appendParam(url), notifyListener);
    }

    @Override
    public List<URL> lookup(URL url) {
        return this.registry.lookup(appendParam(url));
    }
}

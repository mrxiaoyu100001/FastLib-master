package com.aries.library.fast.retrofit;

import android.text.TextUtils;

import com.aries.library.fast.FastConstant;
import com.aries.library.fast.manager.LoggerManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created: AriesHoo on 2017/9/22 11:07
 * E-Mail: AriesHoo@126.com
 * Function:FastMultiUrl 以简洁的 Api,让 Retrofit 不仅支持多 BaseUrl
 * 还可以在 App 运行时动态切换任意 BaseUrl,在多 BaseUrl 场景下也不会影响到其他不需要切换的 BaseUrl
 * Description:设置支持多BaseUrl
 * 1、2017-11-24 14:40:06 AriesHoo 新增部分set put方法返回本对象以便链式调用
 */
public class FastMultiUrl {

    private static final String TAG = "FastMultiUrl";
    private static final String BASE_URL_NAME = "BASE_URL_NAME";
    private static final String GLOBAL_BASE_URL_NAME = "GLOBAL_BASE_URL_NAME";
    /**
     * 用于单独设置其它BaseUrl的Service设置Header标记
     */
    public static final String BASE_URL_NAME_HEADER = BASE_URL_NAME + ": ";

    /**
     * 是否开启拦截开始运行,可以随时停止运行,比如你在 App 启动后已经不需要在动态切换 baseUrl 了
     */
    private boolean mIsIntercept = true;
    //    private boolean mParserUrlEnable = true;
    private final Map<String, HttpUrl> mBaseUrlMap = new HashMap<>();
    private final Interceptor mInterceptor;
    private final List<OnUrlChangedListener> mListeners = new ArrayList<>();
    private FastUrlParser mUrlParser;
    private static volatile FastMultiUrl sInstance;

    public interface OnUrlChangedListener {
        /**
         * 当 Url 的 BaseUrl 被改变时回调
         * 调用时间是在接口请求服务器之前
         *
         * @param newUrl
         * @param oldUrl
         */
        void onUrlChanged(HttpUrl newUrl, HttpUrl oldUrl);
    }

    public interface FastUrlParser {
        /**
         * 将 {@link FastMultiUrl#mBaseUrlMap} 中映射的 Url 解析成完整的{@link HttpUrl}
         * 用来替换 @{@link Request#url} 里的BaseUrl以达到动态切换 Url的目的
         *
         * @param domainUrl
         * @return
         */
        HttpUrl parseUrl(HttpUrl domainUrl, HttpUrl url);
    }

    public static FastMultiUrl getInstance() {
        if (sInstance == null) {
            synchronized (FastMultiUrl.class) {
                if (sInstance == null) {
                    sInstance = new FastMultiUrl();
                }
            }
        }
        return sInstance;
    }

    private FastMultiUrl() {
        setUrlParser(new FastUrlParser() {
            @Override
            public HttpUrl parseUrl(HttpUrl domainUrl, HttpUrl url) {
                // 只支持 http 和 https
                if (null == domainUrl) {
                    return url;
                }
                //解析得到service里的方法名(即@POST或@GET里的内容)
                String method = "";
                try {
                    HttpUrl base = getGlobalBaseUrl();
                    method = url.toString().replace(base.toString(), "");
                } catch (Exception e) {
                    LoggerManager.e(TAG, "parseUrl:" + e.getMessage());
                }
                LoggerManager.d(TAG, "Old Url is{" + url.newBuilder().toString() + "};Method is <<" + method + ">>");
                return checkUrl(domainUrl.toString() + method);
            }
        });
        this.mInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                if (!isIntercept()) // 可以在 App 运行时,随时通过 setIntercept(false) 来结束本管理器的拦截
                    return chain.proceed(chain.request());
                return chain.proceed(processRequest(chain.request()));
            }
        };
    }

    /**
     * 将 {@link okhttp3.OkHttpClient.Builder} 传入,配置一些本管理器需要的参数
     *
     * @param builder
     * @return
     */
    public FastMultiUrl with(OkHttpClient.Builder builder) {
        builder.addInterceptor(mInterceptor);
        return sInstance;
    }

    /**
     * 对 {@link Request} 进行一些必要的加工
     *
     * @param request
     * @return
     */
    public Request processRequest(Request request) {
        Request.Builder newBuilder = request.newBuilder();
        String keyName = getBaseUrlKeyFromHeaders(request);
        HttpUrl httpUrl;
        // 如果有 header,获取 header 中配置的url,否则检查全局的 BaseUrl,未找到则为null
        if (!TextUtils.isEmpty(keyName)) {
            httpUrl = getBaseUrl(keyName);
            newBuilder.removeHeader(BASE_URL_NAME);
        } else {
            httpUrl = getBaseUrl(GLOBAL_BASE_URL_NAME);
        }
        if (null != httpUrl) {
            HttpUrl newUrl = mUrlParser.parseUrl(httpUrl, request.url());
            LoggerManager.i(FastMultiUrl.TAG, "Target Base Url is{" + httpUrl + "}" +
                    ";New Url is { " + newUrl + " }" +
                    ";Old Url is { " + request.url() + " }");
            Object[] listeners = listenersToArray();
            if (listeners != null) {
                for (int i = 0; i < listeners.length; i++) {
                    ((OnUrlChangedListener) listeners[i]).onUrlChanged(newUrl, request.url()); // 通知监听器此 Url 的 BaseUrl 已被改变
                }
            }
            return newBuilder
                    .url(newUrl)
                    .build();
        }
        return newBuilder.build();
    }

    /**
     * 是否拦截--可以在固定的时机停止{@link #setIntercept}
     *
     * @return
     */
    public boolean isIntercept() {
        return mIsIntercept;
    }

    /**
     * 控制管理器是否拦截,在每个域名地址都已经确定,不需要再动态更改时可设置为 false
     *
     * @param enable
     */
    public FastMultiUrl setIntercept(boolean enable) {
        this.mIsIntercept = enable;
        return sInstance;
    }

    /**
     * 获取存放BaseUrl的集合
     *
     * @return
     */
    public Map<String, HttpUrl> getBaseUrlMap() {
        return mBaseUrlMap;
    }

    /**
     * 全局动态替换 BaseUrl,优先级 Header中配置的url > 全局配置的url
     * 除了作为备用的 BaseUrl ,当你项目中只有一个 BaseUrl ,但需要动态改变
     * 这种方式不用在每个接口方法上加 Header,也是个很好的选择
     *
     * @param url
     */
    public FastMultiUrl setGlobalBaseUrl(String url) {
        synchronized (mBaseUrlMap) {
            mBaseUrlMap.put(GLOBAL_BASE_URL_NAME, checkUrl(url));
        }
        return sInstance;
    }

    /**
     * 获取全局 BaseUrl
     */
    public HttpUrl getGlobalBaseUrl() {
        return mBaseUrlMap.get(GLOBAL_BASE_URL_NAME);
    }

    /**
     * 移除全局 BaseUrl
     */
    public FastMultiUrl removeGlobalBaseUrl() {
        synchronized (mBaseUrlMap) {
            mBaseUrlMap.remove(GLOBAL_BASE_URL_NAME);
        }
        return sInstance;
    }

    /**
     * 存放 BaseUrl 的映射关系
     *
     * @param urlKey
     * @param urlValue
     */
    public FastMultiUrl putBaseUrl(String urlKey, String urlValue) {
        synchronized (mBaseUrlMap) {
            mBaseUrlMap.put(urlKey, checkUrl(urlValue));
        }
        return sInstance;
    }
//
//    /**
//     * 设置多BaseUrl是否进行二次解析 最终只变更 第一个(除了http://外)/之前部分
//     * {@link FastUrlParser#parseUrl(HttpUrl, HttpUrl)}
//     * {@link FastMultiUrl#FastMultiUrl()}
//     *
//     * @param enable
//     * @return
//     */
//    public FastMultiUrl setParserUrlEnable(boolean enable) {
//        this.mParserUrlEnable = enable;
//        return sInstance;
//    }

    /**
     * 取出对应 urlKey 的 Url
     *
     * @param urlKey
     * @return
     */
    public HttpUrl getBaseUrl(String urlKey) {
        return mBaseUrlMap.get(urlKey);
    }

    /**
     * 根据key删除BaseUrl
     *
     * @param urlKey
     */
    public FastMultiUrl removeBaseUrl(String urlKey) {
        synchronized (mBaseUrlMap) {
            mBaseUrlMap.remove(urlKey);
        }
        return sInstance;
    }

    /**
     * 清除所有BaseUrl
     */
    public FastMultiUrl clearAllBaseUrl() {
        mBaseUrlMap.clear();
        return sInstance;
    }

    /**
     * 检查是否包含某个url的key
     *
     * @param urlKey
     * @return
     */
    public boolean containsBaseUrl(String urlKey) {
        return mBaseUrlMap.containsKey(urlKey);
    }

    /**
     * 可自行实现 {@link FastUrlParser} 动态切换 Url 解析策略
     *
     * @param parser
     */
    public FastMultiUrl setUrlParser(FastUrlParser parser) {
        this.mUrlParser = parser;
        return sInstance;
    }

    /**
     * 注册当 Url 的 BaseUrl 被改变时会被回调的监听器
     *
     * @param listener
     */
    public FastMultiUrl registerUrlChangeListener(OnUrlChangedListener listener) {
        synchronized (mListeners) {
            mListeners.add(listener);
        }
        return sInstance;
    }

    /**
     * 注销当 Url 的 BaseUrl 被改变时会被回调的监听器
     *
     * @param listener
     */
    public FastMultiUrl unregisterUrlChangedListener(OnUrlChangedListener listener) {
        synchronized (mListeners) {
            mListeners.remove(listener);
        }
        return sInstance;
    }

    private Object[] listenersToArray() {
        Object[] listeners = null;
        synchronized (mListeners) {
            if (mListeners.size() > 0) {
                listeners = mListeners.toArray();
            }
        }
        return listeners;
    }


    /**
     * 从 {@link Request#header(String)} 中取出BASE_URL_NAME
     *
     * @param request
     * @return
     */
    private String getBaseUrlKeyFromHeaders(Request request) {
        List<String> headers = request.headers(BASE_URL_NAME);
        if (headers == null || headers.size() == 0)
            return null;
        if (headers.size() > 1)
            throw new IllegalArgumentException("Only one " + BASE_URL_NAME + " in the headers");
        return request.header(BASE_URL_NAME);
    }

    /**
     * 校验url合法性
     *
     * @param url
     * @return
     */
    private HttpUrl checkUrl(String url) {
        HttpUrl parseUrl = HttpUrl.parse(url);
        if (null == parseUrl) {
            throw new NullPointerException(FastConstant.EXCEPTION_EMPTY_URL);
        } else {
            return parseUrl;
        }
    }
}

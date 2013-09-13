package com.textmagic.sms.core.invoker;

import com.textmagic.sms.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * The default {@link com.textmagic.sms.core.invoker.HttpServiceInvoker} implementation.
 * The implementation is based on <a href="http://hc.apache.org/httpcomponents-client-4.3.x/">apache commons httpclient</a>
 * By default the invoker access the gateway through direct https connection.
 * <br/><br/>
 * To change default httpclient configuration, one should inherit <tt>HttpServiceInvokerImpl</tt>
 * and provide customization in childs constructor:
 *<br/><br/>
 *  <pre>
 *      public class ExtendedServiceInvokerImpl extends HttpServiceInvoker {
 *        public ExtendedServiceInvokerImpl() {
 *            super();
 *            httpclient.getHostConfiguration().setProxy("dummy.com", 80);
 *         }
 *      }
 *  </pre>
 * <br/>
 * To change https to http protocol, one should override <code>textMagicUrl<code> value 
 *
 * @author Rafael Bagmanov
 */
public class HttpServiceInvokerImpl implements HttpServiceInvoker
{
    Log log = LogFactory.getLog(HttpServiceInvokerImpl.class);

    protected String textMagicUrl = "https://www.textmagic.com/app/api";
    protected HttpClient httpclient;


    /**
     *  Constructs the invoker and instantiate httpclient as {@link HttpClient}
     */
    public HttpServiceInvokerImpl()
    {
        httpclient = new DefaultHttpClient();
    }


    public String invoke(String login, String password, String commandName, Map<String, String> parameters) throws ServiceInvokerException
    {
        HttpPost httpPost = new HttpPost(textMagicUrl);


        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("cmd",commandName));
        nameValuePairs.add(new BasicNameValuePair("username",login));
        nameValuePairs.add(new BasicNameValuePair("password",password));

        Set<Map.Entry<String,String>> entries = parameters.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, Consts.UTF_8));

        try {
            if (log.isDebugEnabled()) {

                String logStr = String.format("<<< [login = %s; command = %s; parameters = %s]", login,
                                              commandName, StringUtils.toString(parameters));
                log.debug(logStr);
            }

            HttpResponse response = httpclient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();


            if (statusCode < 200 || statusCode > 299) {
                throw new ServiceInvokerException("Server responded with " + statusCode + " http code");
            }
            return EntityUtils.toString(response.getEntity());
        }
        catch (IOException ex) {
            if (log.isDebugEnabled()) {
                log.debug(">>> exception thrown" + ex.getMessage());
            }
            throw new ServiceInvokerException(ex.getMessage(), ex);
        }
        finally {
            httpPost.releaseConnection();
        }
    }
}
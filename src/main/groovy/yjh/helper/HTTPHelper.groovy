package yjh.helper

import com.teradata.tool.Base64Coder
import com.teradata.tool.TimeHelper
import org.apache.http.Header
import org.apache.http.HttpHost
import org.apache.http.HttpResponse
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CookieStore
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.cookie.Cookie
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.DefaultProxyRoutePlanner
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.impl.cookie.BasicClientCookie
import org.apache.http.message.BasicHeader

import java.text.SimpleDateFormat

/**
 * Created by Peter.Yang on 12/4/2017.
 */
class HTTPHerlper {
    public static void main(String[] args) {


        String stTime = TimeHelper.getCurrentDatetime()
        //s0 para
        String kylinRestUrl = "http://192.168.56.102:7070/kylin"
//        DefaultHttpClient httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager())
        String username = "gamma"
        String password = "user@ldap"
        String jobType = 'd'
        String cubename = "cube_sale3"
        String projectname = "SOURTH_GROUP"
        String encodedUsernamePassword = Base64Coder.encodeString("${username}:${password}")


        int txDeta = -1;
        String txdate = TimeHelper.calcTxDate(TimeHelper.currentDate,txDeta)
        String txdateUnix = TimeHelper.str2unix(txdate)
        String curdateUnix = TimeHelper.str2unix(TimeHelper.calcTxDate(TimeHelper.currentDate,txDeta+1))

        String billmon01Unix = TimeHelper.str2unix(TimeHelper.calcTxMonth(TimeHelper.currentDate,txDeta)+"01")
        String curmon01Unix = TimeHelper.str2unix(TimeHelper.calcTxMonth(TimeHelper.currentDate,txDeta+1).substring(0,
                6)+"01")


        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("Basic", encodedUsernamePassword);
        provider.setCredentials(AuthScope.ANY, credentials);
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(provider).build();

        //s1 login
        HttpPost postRequest = new HttpPost( "${kylinRestUrl}/api/user/authentication")
        postRequest.setHeader("Content-Type", "application/json;charset=UTF-8")
        postRequest.setHeader("Authorization", "Basic ${encodedUsernamePassword}")
        CookieStore cookieStore = new BasicCookieStore();
        HttpClientContext localContext = HttpClientContext.create()
        localContext.setCookieStore(cookieStore)

        HttpResponse httpResponse = httpClient.execute(postRequest,localContext);
        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new IOException("getKylinClient: authorize failed: " + httpResponse.toString());
        }

        //s2 make rebuild request
        Cookie jsCookie= cookieStore.getCookies().get(0)
        BasicClientCookie projectCookie= new BasicClientCookie("project",/%22${projectname}%22/)
        projectCookie.setPath(jsCookie.getPath())
        projectCookie.setDomain(jsCookie.getDomain())
        projectCookie.setExpiryDate(jsCookie.getExpiryDate())
        cookieStore.addCookie(projectCookie)

        String entity
        if(jobType.equals('m')){
            entity = /{"buildType":"BUILD","startTime":${billmon01Unix},"endTime":${curmon01Unix}}/
        }else if(jobType.equals('d')){
            entity = /{"buildType":"BUILD","startTime":${txdateUnix},"endTime":${curdateUnix}}/
        }

        HttpPut putRebuildCubeRequest = new HttpPut("${kylinRestUrl}/api/cubes/${cubename}/rebuild");
//        putRebuildCubeRequest.setHeader("Content-Type", "application/json;charset=UTF-8")
//        putRebuildCubeRequest.setHeader("Authorization", "Basic ${encodedUsernamePassword}")
//        putRebuildCubeRequest.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36")
        putRebuildCubeRequest.setEntity(new StringEntity(entity, "application/json","UTF-8"))
        httpResponse=httpClient.execute(putRebuildCubeRequest,localContext)
        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new IOException("getKylinClient: rebuild failed: " + httpResponse.toString());
        }
        String endTime = TimeHelper.getCurrentDatetime()

    }

}

package com.xxl.job.executor.util;

import java.io.IOException;
import java.util.Locale;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
/**
 *
 * @author Administrator
 */
public class HttpClient4 {
	
	public static Boolean ipTest(String ProxyAddr, int ProxyPort, String ProxyUser,String ProxyPasswd) {
 
        boolean result=false; 
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(ProxyAddr, ProxyPort),
                new UsernamePasswordCredentials(ProxyUser, ProxyPasswd));
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider).build();
        try {
            HttpHost target = new HttpHost("1212.ip138.com", 80);
            HttpHost proxy = new HttpHost(ProxyAddr, ProxyPort);

            RequestConfig config = RequestConfig.custom().setConnectTimeout(3000)
                 .setSocketTimeout(3000)
                 .setConnectionRequestTimeout(3000)
                 .setProxy(proxy)
                 .build();
            
            HttpGet httpget = new HttpGet("/ic.asp");
            httpget.setConfig(config);
            
            CloseableHttpResponse response = httpclient.execute(target, httpget);
            response.setLocale(Locale.CHINESE);
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                
               // System.out.println(EntityUtils.toString(response.getEntity()));
              //  String statusCode = response.getStatusLine().toString();
              // System.out.println(ProxyAddr+":"+ProxyPort);
//				if(statusCode.indexOf("200 OK")!=-1) {
//					result = true;
//                }else {
//                	result = false;
//                }
				result =true;
            }catch (Exception e) {
				
				result = false;
			}finally {
                try {
					response.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//
				}
            }
        } catch (Exception e) {
			//
		}finally {
            try {
				httpclient.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//
			}
        }
        return result;
	}

 
    
}

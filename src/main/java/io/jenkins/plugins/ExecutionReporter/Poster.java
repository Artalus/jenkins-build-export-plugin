package io.jenkins.plugins.executionreporter;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.*;
import java.util.logging.*;

import com.google.gson.Gson;

class Poster {
    private static final Logger logger = Logger.getLogger(Poster.class.getName());

    public static void post(ArrayList<NodeData> data, String url) {
        Gson gson = new Gson();
        String json = gson.toJson(data);


        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(url);

            StringEntity requestEntity = new StringEntity(
                json,
                ContentType.APPLICATION_JSON
            );
            httpPost.setEntity(requestEntity);
            CloseableHttpResponse response2 = httpclient.execute(httpPost);
            try {
                logger.info(response2.getStatusLine().toString());
                HttpEntity entity2 = response2.getEntity();
                // do something useful with the response body
                // and ensure it is fully consumed
                EntityUtils.consume(entity2);
            } finally {
                response2.close();
            }
        } catch (Exception e) {
            logger.severe(
                String.format("[ExecutionReporter] Failed POST: %s", e)
            );
        } finally {
            try {
                httpclient.close();
            } catch (java.io.IOException e) {
                logger.severe("[ExecutionReporter] failed to close httpclient, omg java sucks");
            }
        }
    }
}

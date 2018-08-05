package commonFrameWork.es;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Set;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.common.xcontent.XContentType.JSON;
import static org.elasticsearch.script.ScriptMetaData.TYPE;
import static org.elasticsearch.threadpool.ThreadPool.Names.INDEX;

/**
 * Create by linyoujie on 2018/8/1
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = EsClient.class)
public class EsClient {


    public Logger logger = LoggerFactory.getLogger(EsClient.class);

    //创建一个客户端
    public TransportClient transportClient(){
        try {
            TransportClient client  = new PreBuiltTransportClient(Settings.EMPTY)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"),9300));
            return client;
        } catch (UnknownHostException e) {
            logger.error("");
        }

        return null;
    }


    /**
     * 创建索引
     * */
    @Test
    public void contextloads() throws IOException {
        IndexResponse response = transportClient().prepareIndex("twitter","tweet","1")
                .setSource(jsonBuilder().startObject()
                .field("user","beny")
                .field("postDate",new Date())
                .field("message","try out Elascserarce")
                .endObject()).get();
    }

     /**
      * 添加文档
      * */
     @Test
    public void addDocuments(){

         String json = "{" +
                         "\"user\":\"kimchy\"," +
                         "\"postDate\":\"2013-01-30\"," +
                         "\"message\":\"trying out Elasticsearch\"" +
                         "}";
        IndexResponse response  = transportClient().prepareIndex("twitter","tweet")
                .setSource(json, JSON).get();

        String _index = response.getIndex();
        String _type = response.getType();
        String _id = response.getId();

        long _version = response.getVersion();
        System.out.println(_index+"_"+_type+"_"+_id);
    }

    /**
     * 通过id 寻找对应的文档
     * */
    @Test
    public void findById(){
        GetResponse response = transportClient().prepareGet("twitter","tweet","1").get();
        System.out.println("ha"+response.getSource());
    }


    /**
     * 更新文档
     * */
    @Test
    public void updata() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index("twitter");
        updateRequest.type("tweet");
        updateRequest.id("1");
        updateRequest.doc(jsonBuilder()
        .startObject()
        .field("user","changeName")
        .endObject());
        transportClient().update(updateRequest);
    }

    /**
     * 一次性获取多个文档
     * */
    public void multGetApi(){
        MultiGetResponse responses = transportClient().prepareMultiGet()
                .add(INDEX,TYPE,"AWRFv-yAro3r8sDxIpib")
                .add(INDEX,TYPE,"AWRFvA7k0udstXU4tl60","AWRJA72Uro3r8sDxIpip")
                .add("blog","blog","AWG9GKCwhg1e21lmGSLH")
                .get();

        for (MultiGetItemResponse itemResponse:responses){
            GetResponse response = itemResponse.getResponse();
            if (response.isExists()){
                String source = response.getSourceAsString();
                JSONObject jsonObject = com.alibaba.fastjson.JSON.parseObject(source);
                Set<String> sets = jsonObject.keySet();
                for (String str:sets){
                    System.out.println("key->" + str);
                    System.out.println("value - >"+jsonObject.get(str));
                    System.out.println("========");
                }

            }
        }
    }


    //提供搜索的api,会根据特定的搜索的条件进行一个api的搜索
    public void searchApi(){
        SearchResponse response = transportClient().prepareSearch(INDEX).setTypes(TYPE)
                .setQuery(QueryBuilders.matchQuery("user","hha")).get();
        SearchHit[] hits = response.getHits().getHits();
        for (int i=0;i<hits.length;i++){
            String json = hits[i].getSourceAsString();
            JSONObject jsonObject = com.alibaba.fastjson.JSON.parseObject(json);
            Set<String> stringSet = jsonObject.keySet();
            for (String string:stringSet){
                System.out.println(jsonObject.get(string));
            }
        }
    }




    







}

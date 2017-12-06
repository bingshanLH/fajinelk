package com.test;

import com.demo.basicfile.BasicFileService;
import com.demo.common.model.BasicFile;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class ElkTest extends JFinalModelCase {

	private static BasicFileService basicFileService = new BasicFileService();

	private Settings settings = Settings.settingsBuilder().build();

	private TransportClient client;

	//es index x相当于数据库名
	private String indexName = "es_fajing";

	//相当于表名
	private String indexType = "basic_file";

	public ElkTest() {
		try {
			client = TransportClient.builder()
					.settings(settings)
					.build()
					.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("39.106.116.178"), 9300));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 第一步创建mapping,设计数据表结构
	 *
	 * @return
	 */
	private XContentBuilder createMapping() {
		XContentBuilder mapping = null;
		try {
			mapping = XContentFactory.jsonBuilder()
					.startObject().startObject(indexType).startObject("properties")

					.startObject("id").field("type", "long").endObject()
					.startObject("creation_time").field("type", "string").endObject()
					.startObject("pid").field("type", "long").endObject()
					.startObject("pname").field("type", "string").endObject()
					.startObject("content").field("type", "string").endObject()
					.startObject("remark").field("type", "string").endObject()
					.startObject("sequence").field("type", "string").endObject()
					.startObject("sequence_number").field("type", "integer").endObject()
					.startObject("basic_static").field("type", "integer").endObject()

					.endObject().endObject().endObject();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mapping;
	}

	/**
	 * 创建索引
	 */
	public void createIndex() {
		XContentBuilder mapping = createMapping();

		//建立数据库，数据表，数据结构
		client.admin().indices().prepareCreate(indexName).execute().actionGet();

		PutMappingRequest putMappingRequest = Requests.putMappingRequest(indexName).type(indexType).source(mapping);

		PutMappingResponse putMappingResponse = client.admin().indices().putMapping(putMappingRequest).actionGet();

		if (!putMappingResponse.isAcknowledged()) {
			System.out.println("无法创建mapping");
		} else {
			System.out.println("创建mapping成功");
		}
	}


	/**
	 * 向索引添加数据
	 *
	 * @return
	 */
	public Integer addDataToIndex() {

//		Page<BasicFile> page = basicFileService.paginate(1, 10);

		List<BasicFile> list = basicFileService.queryAllList();

		List<String> basicFileList = new ArrayList<String>();

		for (BasicFile basicFile :
				list) {
			basicFileList.add(objToJson(basicFile));
		}


		//创建索引
		List<IndexRequest> requests = new ArrayList<IndexRequest>();
		for (String str : basicFileList) {
			IndexRequest request = client.prepareIndex(indexName, indexType)
					.setSource(str).request();
			requests.add(request);
		}

		//批量创建索引
		BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
		for (IndexRequest request : requests) {
			bulkRequestBuilder.add(request);
		}

		BulkResponse bulkResponse = bulkRequestBuilder.execute().actionGet();

		if (bulkResponse.hasFailures()) {
			System.out.println("创建索引出错");
		}
		return bulkRequestBuilder.numberOfActions();
	}

	/**
	 * 将对象转为json字符串
	 *
	 * @param basicFile
	 * @return
	 */
	private String objToJson(BasicFile basicFile) {
		String jsonStr = null;

		try {
			XContentBuilder jsonBuild = XContentFactory.jsonBuilder();

			jsonBuild.startObject()
					.field("id", basicFile.getId())
					.field("creation_time", basicFile.getCreationTime())
					.field("pid", basicFile.getPid())
					.field("pname", basicFile.getPname())
					.field("content", basicFile.getContent())
					.field("remark", basicFile.getRemark())
					.field("sequence", basicFile.getSequence())
					.field("sequence_number", basicFile.getSequenceNumber())
					.field("basic_static", basicFile.getBasicStatic())
					.endObject();

			jsonStr = jsonBuild.string();
			System.out.println(jsonStr);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonStr;
	}


	public void searchNearbyMember(String str,Integer size) {

		SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName).setTypes(indexType);

		//设置分页
		searchRequestBuilder.setFrom(0).setSize(size);

		//
		QueryBuilder queryBuilder=QueryBuilders.matchQuery("content",str);
		searchRequestBuilder.setQuery(queryBuilder);

		//开始搜索
		SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
		//一般的搜索引擎中，高亮
		SearchHits searchHits = searchResponse.getHits();
		SearchHit[] hits = searchHits.getHits();
		for (SearchHit hit :
				hits) {
			System.out.println(hit.getSource().get("content"));
		}
		//耗时
		Float useTime = searchResponse.getTookInMillis() / 1000f;
		System.out.println(useTime);
		System.out.println(hits.length);

	}


	@Test
	public void TestDB() {

		searchNearbyMember("保护隐私",2000);
//		ElkTest elk = new ElkTest();
//		elk.createIndex();
//		elk.addDataToIndex();
//		Page<BasicFile> page = basicFileService.paginate(1, 10);
//
//		List<BasicFile> list = page.getList();
//
//		for (BasicFile b :
//				list) {
//			System.out.println(b.toJson());
//		}
	}

}

package com.test;

import com.demo.basicfile.BasicFileService;
import com.demo.common.model.BasicFile;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
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


//	/**
//	 * 查找附近的人
//	 *
//	 * @param lat
//	 * @param lon
//	 * @param radius
//	 * @param size
//	 * @return
//	 */
//	public SearchResult searchNearbyMember(Double lat, Double lon, int radius, int size, String gender) {
//
//		SearchResult searchResult = new SearchResult();
//
//		SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName).setTypes(indexType);
//
//		//设置分页
//		searchRequestBuilder.setFrom(0).setSize(size);
//
//		//构建坐标查询规则
//		QueryBuilder queryBuilder = new GeoDistanceRangeQueryBuilder("location")
//				.point(lat, lon)
//				.from("0m").to(radius + "m")
//				.optimizeBbox("memory")
//				.geoDistance(GeoDistance.PLANE);
//		searchRequestBuilder.setPostFilter(queryBuilder);
//
//		//创建排序规则
//		GeoDistanceSortBuilder geoDistanceSortBuilder = SortBuilders.geoDistanceSort("location");
//		geoDistanceSortBuilder.unit(DistanceUnit.METERS);
//		geoDistanceSortBuilder.order(SortOrder.ASC);
//		geoDistanceSortBuilder.point(lat, lon);
//		searchRequestBuilder.addSort(geoDistanceSortBuilder);
//
//		//
//		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//		if (gender != null && !"".equals(gender.trim())) {
//			boolQueryBuilder.must(QueryBuilders.matchQuery("gender", gender));
//		}
//		searchRequestBuilder.setQuery(boolQueryBuilder);
//
//		//开始搜索
//		SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
//		//一般的搜索引擎中，高亮
//		SearchHits searchHits = searchResponse.getHits();
//		SearchHit[] hits = searchHits.getHits();
//		for (SearchHit hit :
//				hits) {
//			BigDecimal geoDis = new BigDecimal((Double) hit.getSortValues()[0]);
//			//获取高亮中的记录
//			Map<String, Object> hitMap = hit.getSource();
//			//向结果中填值
//			hitMap.put("geo", geoDis.setScale(0, BigDecimal.ROUND_HALF_DOWN));
//			searchResult.getData().add(hitMap);
//		}
//		//耗时
//		Float useTime = searchResponse.getTookInMillis() / 1000f;
//		searchResult.setUseTime(useTime);
//
//		//单位
//		searchResult.setDistance("m");
//
//		searchResult.setTotal(searchHits.getTotalHits());
//
//		return searchResult;
//	}


	@Test
	public void TestDB() {
		ElkTest elk = new ElkTest();
		elk.createIndex();
		elk.addDataToIndex();
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

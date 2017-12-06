package com.test;

import com.demo.basicfile.BasicFileService;
import com.demo.common.model.BasicFile;
import com.jfinal.plugin.activerecord.Page;
import org.junit.Test;

import java.util.List;

public class ElkTest extends JFinalModelCase{

	private static BasicFileService basicFileService = new BasicFileService();

	@Test
	public void TestDB(){
		Page<BasicFile> page = basicFileService.paginate(1, 10);

		List<BasicFile> list=page.getList();

		for (BasicFile b :
				list) {
			System.out.println(b.toJson());
		}
	}
}

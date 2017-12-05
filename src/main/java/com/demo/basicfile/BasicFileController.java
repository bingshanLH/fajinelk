package com.demo.basicfile;

import com.demo.common.model.BasicFile;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Page;

/**
 * @author lh
 */
public class BasicFileController extends Controller {
	static BasicFileService basicFileService = new BasicFileService();

	public void index() {
		Page<BasicFile> page = basicFileService.paginate(1, 10);
		render("index.html");
	}

}

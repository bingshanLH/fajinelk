package com.demo.basicfile;

import com.demo.common.model.BasicFile;
import com.jfinal.plugin.activerecord.Page;

import java.util.List;

/**
 * @author lh
 */
public class BasicFileService {
	public static final BasicFile BASIC_FILE_DAO = new BasicFile().dao();

	public Page<BasicFile> paginate(int pageNumber, int pageSize) {
		return BASIC_FILE_DAO.paginate(pageNumber, pageSize, "select *", "from basic_file order by id asc");
	}

	public List<BasicFile> queryAllList(){
		return BASIC_FILE_DAO.find("select * from basic_file");
	}

	public BasicFile findById(int id) {
		return BASIC_FILE_DAO.findById(id);
	}

}

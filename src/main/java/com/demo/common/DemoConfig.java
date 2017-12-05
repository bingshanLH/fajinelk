package com.demo.common;

import com.demo.basicfile.BasicFileController;
import com.demo.index.IndexController;
import com.jfinal.config.*;
import com.jfinal.core.JFinal;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.template.Engine;
import com.demo.common.model._MappingKit;

/**
 * @author lh
 */
public class DemoConfig extends JFinalConfig {


	public static DruidPlugin createDruidPlugin() {
		return new DruidPlugin(PropKit.get("jdbcUrl"), PropKit.get("user"), PropKit.get("password").trim());
	}

	@Override
	public void configConstant(Constants constants) {
		// 加载少量必要配置，随后可用PropKit.get(...)获取值
		PropKit.use("init.properties");
		constants.setDevMode(PropKit.getBoolean("devMode", false));
	}

	@Override
	public void configRoute(Routes routes) {
		// 第三个参数为该Controller的视图存放路径
		//routes.add("/", IndexController.class, "/");
		routes.add("/", BasicFileController.class,"/");
	}

	@Override
	public void configEngine(Engine engine) {

	}

	@Override
	public void configPlugin(Plugins plugins) {
		// 配置 druid 数据库连接池插件
		DruidPlugin druidPlugin = new DruidPlugin(PropKit.get("jdbcUrl"), PropKit.get("user"), PropKit.get("password").trim());
		plugins.add(druidPlugin);

		// 配置ActiveRecord插件
		ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
		// 所有映射在 MappingKit 中自动化搞定
		_MappingKit.mapping(arp);
		plugins.add(arp);
	}

	@Override
	public void configInterceptor(Interceptors interceptors) {

	}

	@Override
	public void configHandler(Handlers handlers) {

	}

	public static void main(String[] args) {
		/**
		 * 特别注意：Eclipse 之下建议的启动方式
		 */
//		JFinal.start("src/main/webapp", 80, "/", 5);

		/**
		 * 特别注意：IDEA 之下建议的启动方式，仅比 eclipse 之下少了最后一个参数
		 */
		JFinal.start("src/main/webapp", 80, "/");
	}
}

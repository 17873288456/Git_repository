package com.wenge.datagroup.common;

import java.lang.reflect.Method;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.generator.Generator;
import com.jfinal.plugin.druid.DruidPlugin;

/**
 * 
 * @author hb
 */
public class InitContext {
	private static final Logger logger = LoggerFactory.getLogger(SysConstants.class);
	private static String jdbcUrl = "jdbc:mysql://#IP/#DB?characterEncoding=utf8";

	/**
	 * 解析多数据源配置
	 */
	public static void initDatasource() {

		/**
		 * 初始化用户库
		 */
		initDataSource();

	}

	/**
	 * 
	 * <p>
	 * Title:
	 * </p>
	 * <p>
	 * Description:初始化数据源的方法
	 * </p>
	 * <p>
	 * Company:中科闻歌
	 * </p>
	 * 
	 * @param ipConfig
	 *            数据库ip; DbConfig 数据库名 ; userConifg 用户名 ;passwordConfig 密码 ;
	 *            mapConfig添加主键的(_MappingKit)类全名
	 * @author lzk
	 * @date 2018年2月6日
	 * @return void
	 */
	public static void initDataSource() {
		try {
			String dbIp = SysConstants.DBIP;
			String dbName = SysConstants.DBNAME;
			String user = SysConstants.USER;
			String password = SysConstants.PASSWORD;
			String mappingKit = SysConstants.MAPPINGKIT;
			String str_kb = jdbcUrl.replace("#IP", dbIp).replace("#DB", dbName);
			DruidPlugin dp_kb = new DruidPlugin(str_kb, user, password);
			Class<?> obj = Class.forName(mappingKit);
			Method method = obj.getMethod("mapping", ActiveRecordPlugin.class);
			ActiveRecordPlugin arp_kb = new ActiveRecordPlugin(dbName, dp_kb);
			method.invoke(obj, new Object[] { arp_kb });
			arp_kb.setShowSql(false);
			dp_kb.start();
			arp_kb.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		// base model 所使用的包名
		String baseModelPackageName = "com.wenge.datagroup.model2.base";
		// base model 文件保存路径
		String baseModelOutputDir = "src/com/wenge/datagroup/model2/base";
		
		// model 所使用的包名 (MappingKit 默认使用的包名)
		String modelPackageName = "com.wenge.datagroup.model2";
		// model 文件保存路径 (MappingKit 与 DataDictionary 文件默认保存路径)
		String modelOutputDir = baseModelOutputDir + "/..";
		
		// 创建生成器
		Generator generator = new Generator(getDataSource(), baseModelPackageName, baseModelOutputDir, modelPackageName, modelOutputDir);
		// 添加不需要生成的表名
		generator.addExcludedTable("adv");
		// 设置是否在 Model 中生成 dao 对象
		generator.setGenerateDaoInModel(true);
		// 设置是否生成链式 setter 方法
		generator.setGenerateChainSetter(true);
		// 设置是否生成字典文件
		generator.setGenerateDataDictionary(false);
		// 设置需要被移除的表名前缀用于生成modelName。例如表名 "osc_user"，移除前缀 "osc_"后生成的model名为 "User"而非 OscUser
		generator.setRemovedTableNamePrefixes("t_");
		// 生成
		generator.generate();
	}
	public static DataSource getDataSource() {
//		PropKit.use("a_little_config.txt"); 
		DruidPlugin druidPlugin = new DruidPlugin("jdbc:mysql://192.168.10.51:3306/basic_platform_manager?characterEncoding=utf8","dba","db*#2016");
		druidPlugin.start();
		return druidPlugin.getDataSource();
	}
}

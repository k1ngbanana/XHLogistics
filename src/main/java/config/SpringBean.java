package config;

import config.aspect.BaseDaoAspect;
import org.apache.commons.dbcp2.BasicDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

@Configuration
@EnableAspectJAutoProxy
@Component
public class SpringBean {
	
	@Bean("dataSource")
	public DataSource getDataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		Properties pro = new Properties();
		try {
			pro.load(this.getClass().getResourceAsStream("/jdbc.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(pro);
		dataSource.setDriverClassName(pro.getProperty("driver"));
		dataSource.setUrl(pro.getProperty("url"));
		dataSource.setUsername(pro.getProperty("username"));
		dataSource.setPassword(pro.getProperty("password"));
		dataSource.setMaxTotal(Integer.valueOf(pro.getProperty("maxTotal")));
		dataSource.setMaxIdle(Integer.valueOf(pro.getProperty("maxIdle")));
		dataSource.setMaxWaitMillis(Integer.valueOf(pro.getProperty("maxWait")));
		return dataSource;
	}
	
	@Bean("sqlSessionFactory")
	public SqlSessionFactoryBean getSqlSessionFactoryBean(@Autowired DataSource dataSource) {
		SqlSessionFactoryBean ssfb = new SqlSessionFactoryBean();
		ssfb.setDataSource(dataSource);
		ssfb.setConfigLocation(new ClassPathResource("/mybatis-config.xml"));
		return ssfb;
	}
	
	@Bean
	public MapperScannerConfigurer getMapperScannerConfigurer() {
		MapperScannerConfigurer msc = new MapperScannerConfigurer();
		msc.setBasePackage("pojo");
		msc.setSqlSessionFactoryBeanName("sqlSessionFactory");
		msc.setAnnotationClass(org.springframework.stereotype.Repository.class);
		return msc;
	}

	@Bean
	public BaseDaoAspect getBaseDaoAspect(){
		return new BaseDaoAspect();
	}
}

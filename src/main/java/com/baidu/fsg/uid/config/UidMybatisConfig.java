package com.baidu.fsg.uid.config;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@ConditionalOnClass(UidGeneratorProperties.class)
@Slf4j
public class UidMybatisConfig implements EnvironmentAware {

    private Environment environment;



    @Bean(name = "uidDataSource")
    public DataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        String dataUrl =environment.getProperty("baidu.uid.url");
        String username = environment.getProperty("baidu.uid.username");
        String password = environment.getProperty("baidu.uid.password");
        String driverClassName =environment.getProperty("baidu.uid.driver-class-name");
        dataSource.setUrl(dataUrl);
        dataSource.setDriverClassName(driverClassName);
        dataSource.setPassword(password);
        dataSource.setUsername(username);
        dataSource.setMinIdle(5);
        dataSource.setMaxActive(20);
        dataSource.setValidationQuery("select 1 from dual");
        dataSource.setMaxWait(10000);
        return dataSource;
    }



    @Bean(name = "uidSqlSessionFactoryBean")
    public SqlSessionFactoryBean sqlSessionFactoryBean(@Qualifier("uidDataSource")DataSource dataSource) throws IOException {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        //设置数据源
        sqlSessionFactoryBean.setDataSource(dataSource);
        //设置mybatis的主配置文件
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource mybatisXml = resourcePatternResolver.getResource("classpath:/uid/mybatis/mybatis-config.xml");
        sqlSessionFactoryBean.setConfigLocation(mybatisXml);
        // 配置mapper的扫描，找到所有的mapper.xml映射文件
        Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources("classpath:/uid/mybatis/mapper/*.xml");
        sqlSessionFactoryBean.setMapperLocations(resources);
        //设置别名包
        sqlSessionFactoryBean.setTypeAliasesPackage("com.baidu.fsg.uid.worker.entity");
        return sqlSessionFactoryBean;
    }

    @Bean(name = "uidSqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("uidSqlSessionFactoryBean")SqlSessionFactoryBean sqlSessionFactoryBean) throws Exception {
        return sqlSessionFactoryBean.getObject();
    }


    @Bean(name = "uidMapperScannerConfigurer")
    public MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
        mapperScannerConfigurer.setSqlSessionFactoryBeanName("uidSqlSessionFactory");
        mapperScannerConfigurer.setBasePackage("com.baidu.fsg.uid.worker.dao");
        return mapperScannerConfigurer;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}

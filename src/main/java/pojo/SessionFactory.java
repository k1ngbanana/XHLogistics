package pojo;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public class SessionFactory {

    public SqlSessionFactory getSqlSessionFactory(){
        InputStream is = Object.class.getClass().getResourceAsStream("/mybatis-config.xml");
        SqlSessionFactory ssf = new SqlSessionFactoryBuilder().build(is);
        //关闭inputsream
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ssf;
    }
}

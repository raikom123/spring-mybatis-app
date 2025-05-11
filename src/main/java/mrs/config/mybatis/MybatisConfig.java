package mrs.config.mybatis;

import org.apache.ibatis.plugin.Interceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisConfig {

    @Bean
    Interceptor statementHandlerInterceptor() {
        return new StatementHandlerInterceptor();
    }

}

package moe.ahao.commerce.market;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * SpringBoot方式启动类
 * @author Ahaochan
 */
@SpringBootApplication(scanBasePackages = "moe.ahao")
@EnableAspectJAutoProxy(exposeProxy = true)
public class MarketApplication {

    private static final Logger logger = LoggerFactory.getLogger(MarketApplication.class);

    public static void main(String[] args) {
        try {
            SpringApplication app = new SpringApplication(MarketApplication.class);
            app.run(args);
            logger.info("{}启动成功!", MarketApplication.class.getSimpleName());
        } catch (Exception e) {
            logger.error("{}启动失败!", MarketApplication.class.getSimpleName(), e);
        }
    }
}

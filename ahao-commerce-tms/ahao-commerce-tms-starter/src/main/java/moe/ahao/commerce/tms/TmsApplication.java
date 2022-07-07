package moe.ahao.commerce.tms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SpringBoot方式启动类
 *
 * @author Ahaochan
 */
@SpringBootApplication(scanBasePackages = "moe.ahao")
public class TmsApplication {
    private static final Logger logger = LoggerFactory.getLogger(TmsApplication.class);

    public static void main(String[] args) {
        try {
            SpringApplication app = new SpringApplication(TmsApplication.class);
            app.run(args);
            logger.info("{}启动成功!", TmsApplication.class.getSimpleName());
        } catch (Exception e) {
            logger.error("{}启动失败!", TmsApplication.class.getSimpleName(), e);
        }
    }
}

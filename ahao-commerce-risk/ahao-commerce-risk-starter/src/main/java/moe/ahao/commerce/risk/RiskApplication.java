package moe.ahao.commerce.risk;

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
public class RiskApplication {
    private static final Logger logger = LoggerFactory.getLogger(RiskApplication.class);

    public static void main(String[] args) {
        try {
            SpringApplication app = new SpringApplication(RiskApplication.class);
            app.run(args);
            logger.info("{}启动成功!", RiskApplication.class.getSimpleName());
        } catch (Exception e) {
            logger.error("{}启动失败!", RiskApplication.class.getSimpleName(), e);
        }
    }
}

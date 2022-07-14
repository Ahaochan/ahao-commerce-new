package moe.ahao.commerce.risk.adapter;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.risk.api.RiskFeignApi;
import moe.ahao.commerce.risk.api.command.CheckOrderRiskCommand;
import moe.ahao.commerce.risk.api.dto.CheckOrderRiskDTO;
import moe.ahao.domain.entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(RiskFeignApi.PATH)
public class RiskController implements RiskFeignApi {
    @Override
    public Result<CheckOrderRiskDTO> checkOrderRisk(@RequestBody CheckOrderRiskCommand command) {
        // 执行风控检查, 默认风控检查通过
        CheckOrderRiskDTO checkOrderRiskDTO = new CheckOrderRiskDTO();
        checkOrderRiskDTO.setResult(true);
        return Result.success(checkOrderRiskDTO);
    }
}

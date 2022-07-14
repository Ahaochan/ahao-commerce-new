package moe.ahao.commerce.tms.adapter;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.tms.api.TmsFeignApi;
import moe.ahao.commerce.tms.api.command.CancelSendOutCommand;
import moe.ahao.commerce.tms.api.command.SendOutCommand;
import moe.ahao.commerce.tms.api.dto.SendOutDTO;
import moe.ahao.commerce.tms.application.CancelSendOutAppService;
import moe.ahao.commerce.tms.application.SendOutAppService;
import moe.ahao.domain.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(TmsFeignApi.PATH)
public class TmsController implements TmsFeignApi {
    @Autowired
    private SendOutAppService sendOutAppService;
    @Autowired
    private CancelSendOutAppService cancelSendOutAppService;

    @Override
    public Result<SendOutDTO> sendOut(@RequestBody SendOutCommand command) {
        SendOutDTO sendOutDTO = sendOutAppService.sendOut(command);
        return Result.success(sendOutDTO);
    }

    @Override
    public Result<Boolean> cancelSendOut(@RequestBody CancelSendOutCommand command) {
        Boolean success = cancelSendOutAppService.cancelSendOut(command);
        return Result.success(success);
    }
}

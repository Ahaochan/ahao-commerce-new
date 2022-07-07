package moe.ahao.commerce.tms.api;

import moe.ahao.commerce.tms.api.command.CancelSendOutCommand;
import moe.ahao.commerce.tms.api.command.SendOutCommand;
import moe.ahao.commerce.tms.api.dto.SendOutDTO;
import moe.ahao.domain.entity.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface TmsFeignApi extends TmsApi {
    String CONTEXT = "/api/tms/";

    @PostMapping("/sendOut")
    Result<SendOutDTO> sendOut(@RequestBody SendOutCommand command);
    @PostMapping("/cancelSendOut")
    Result<Boolean> cancelSendOut(@RequestBody CancelSendOutCommand command);
}

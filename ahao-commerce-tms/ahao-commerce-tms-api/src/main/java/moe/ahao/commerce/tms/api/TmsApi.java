package moe.ahao.commerce.tms.api;

import moe.ahao.commerce.tms.api.command.CancelSendOutCommand;
import moe.ahao.commerce.tms.api.command.SendOutCommand;
import moe.ahao.commerce.tms.api.dto.SendOutDTO;
import moe.ahao.domain.entity.Result;

public interface TmsApi {
    /**
     * 发货
     */
    Result<SendOutDTO> sendOut(SendOutCommand command);
    /**
     * 取消发货
     */
    Result<Boolean> cancelSendOut(CancelSendOutCommand command);
}

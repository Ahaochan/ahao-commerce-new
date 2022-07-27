package moe.ahao.commerce.aftersale.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.aftersale.api.command.AfterSaleAuditCommand;
import moe.ahao.commerce.aftersale.infrastructure.component.AfterSaleOperateLogFactory;
import moe.ahao.commerce.aftersale.infrastructure.enums.AfterSaleStatusChangeEnum;
import moe.ahao.commerce.aftersale.infrastructure.enums.AfterSaleStatusEnum;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleInfoDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleLogDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.mapper.AfterSaleInfoMapper;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.mapper.AfterSaleLogMapper;
import moe.ahao.commerce.common.constants.RocketMqConstant;
import moe.ahao.commerce.common.enums.CustomerAuditResult;
import moe.ahao.commerce.common.enums.CustomerAuditSourceEnum;
import moe.ahao.commerce.common.infrastructure.rocketmq.MQMessage;
import moe.ahao.commerce.order.api.command.AfterSaleAuditPassReleaseAssetsEvent;
import moe.ahao.commerce.order.infrastructure.exception.OrderExceptionEnum;
import moe.ahao.commerce.order.infrastructure.publisher.CustomerAuditPassSendReleaseAssetsProducer;
import moe.ahao.util.commons.io.JSONHelper;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;

@Slf4j
@Service
public class AfterSaleAuditAppService {
    @Autowired
    @Lazy
    private AfterSaleAuditAppService _this;

    @Autowired
    private AfterSaleInfoMapper afterSaleInfoMapper;
    @Autowired
    private AfterSaleLogMapper afterSaleLogMapper;
    @Autowired
    private AfterSaleOperateLogFactory afterSaleOperateLogFactory;

    @Autowired
    private CustomerAuditPassSendReleaseAssetsProducer customerAuditPassSendReleaseAssetsProducer;

    public void audit(AfterSaleAuditCommand command) {
        // 1. 客服审核拒绝
        Integer auditResult = command.getAuditResult();
        if (CustomerAuditResult.REJECT.getCode().equals(auditResult)) {
            // 1.1. 更新 审核拒绝 售后信息
            _this.auditReject(command);
        }
        // 2. 客服审核通过
        else if (CustomerAuditResult.ACCEPT.getCode().equals(auditResult)) {
            // 2.1. 更新 审核接受 售后信息, 发送释放权益资产事务MQ
            this.auditAcceptAndSendReleaseAssetsEvent(command);
        }
    }

    /**
     * 接收客服审核拒绝结果 入口
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditReject(AfterSaleAuditCommand command) {
        String afterSaleId = command.getAfterSaleId();
        AfterSaleInfoDO afterSaleInfoDO = afterSaleInfoMapper.selectOneByAfterSaleId(afterSaleId);
        // 1. 幂等校验：防止客服重复审核订单
        if (afterSaleInfoDO.getAfterSaleStatus() > AfterSaleStatusEnum.COMMITED.getCode()) {
            throw OrderExceptionEnum.CUSTOMER_AUDIT_CANNOT_REPEAT.msg();
        }

        // 2. 更新售后信息
        afterSaleInfoMapper.updateReviewInfoByAfterSaleId(afterSaleId, AfterSaleStatusEnum.REVIEW_REJECTED.getCode(),
            CustomerAuditResult.REJECT.getName(),
            command.getAuditResult(),
            String.valueOf(CustomerAuditSourceEnum.SELF_MALL.getCode()),
            new Date());

        // 3. 记录售后日志
        AfterSaleStatusChangeEnum statusChange = AfterSaleStatusChangeEnum.AFTER_SALE_CUSTOMER_AUDIT_REJECT;
        AfterSaleLogDO afterSaleLog = afterSaleOperateLogFactory.get(afterSaleInfoDO, statusChange);
        afterSaleLogMapper.insert(afterSaleLog);
    }

    /**
     * 接收客服审核通过结果 入口
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditAccept(AfterSaleAuditCommand command) {
        String afterSaleId = command.getAfterSaleId();
        AfterSaleInfoDO afterSaleInfoDO = afterSaleInfoMapper.selectOneByAfterSaleId(afterSaleId);
        // 1. 幂等校验：防止客服重复审核订单
        if (afterSaleInfoDO.getAfterSaleStatus() > AfterSaleStatusEnum.COMMITED.getCode()) {
            throw OrderExceptionEnum.CUSTOMER_AUDIT_CANNOT_REPEAT.msg();
        }

        // 2. 更新售后信息
        afterSaleInfoMapper.updateReviewInfoByAfterSaleId(afterSaleId, AfterSaleStatusEnum.REVIEW_PASS.getCode(),
            CustomerAuditResult.ACCEPT.getName(),
            command.getAuditResult(),
            String.valueOf(CustomerAuditSourceEnum.SELF_MALL.getCode()),
            new Date());

        // 3. 记录售后日志
        AfterSaleStatusChangeEnum statusChange = AfterSaleStatusChangeEnum.AFTER_SALE_CUSTOMER_AUDIT_PASS;
        AfterSaleLogDO afterSaleLog = afterSaleOperateLogFactory.get(afterSaleInfoDO, statusChange);
        afterSaleLogMapper.insert(afterSaleLog);
    }

    private void auditAcceptAndSendReleaseAssetsEvent(AfterSaleAuditCommand command) {
        try {
            TransactionMQProducer transactionMQProducer = customerAuditPassSendReleaseAssetsProducer.getProducer();
            transactionMQProducer.setTransactionListener(this.getTransactionListener());

            AfterSaleAuditPassReleaseAssetsEvent event = new AfterSaleAuditPassReleaseAssetsEvent();
            event.setOrderId(command.getOrderId());
            event.setAfterSaleId(command.getAfterSaleId());

            String topic = RocketMqConstant.CUSTOMER_AUDIT_PASS_RELEASE_ASSETS_TOPIC;
            String json = JSONHelper.toString(event);
            Message message = new MQMessage(topic, json.getBytes(StandardCharsets.UTF_8));
            TransactionSendResult result = transactionMQProducer.sendMessageInTransaction(message, command);
            if (!result.getLocalTransactionState().equals(LocalTransactionState.COMMIT_MESSAGE)) {
                throw OrderExceptionEnum.SEND_AUDIT_PASS_RELEASE_ASSETS_FAILED.msg();
            }
        } catch (Exception e) {
            throw OrderExceptionEnum.SEND_TRANSACTION_MQ_FAILED.msg();
        }
    }

    private TransactionListener getTransactionListener() {
        return new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
                try {
                    AfterSaleAuditCommand command = (AfterSaleAuditCommand) arg;
                    // 更新 审核通过 售后信息
                    _this.auditAccept(command);
                    return LocalTransactionState.COMMIT_MESSAGE;
                } catch (Exception e) {
                    log.error("system error", e);
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt msg) {
                AfterSaleAuditPassReleaseAssetsEvent event = JSONHelper.parse(new String(msg.getBody(), StandardCharsets.UTF_8), AfterSaleAuditPassReleaseAssetsEvent.class);
                AfterSaleInfoDO afterSaleInfo = afterSaleInfoMapper.selectOneByAfterSaleId(event.getAfterSaleId());
                if (Objects.equals(AfterSaleStatusEnum.REVIEW_PASS.getCode(), afterSaleInfo.getAfterSaleStatus())) {
                    return LocalTransactionState.COMMIT_MESSAGE;
                }
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        };
    }
}

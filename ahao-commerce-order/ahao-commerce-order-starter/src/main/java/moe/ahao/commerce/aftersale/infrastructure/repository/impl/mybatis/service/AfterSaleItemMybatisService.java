package moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleItemDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.mapper.AfterSaleItemMapper;
import org.springframework.stereotype.Service;

@Service
public class AfterSaleItemMybatisService extends ServiceImpl<AfterSaleItemMapper, AfterSaleItemDO> {
}

package moe.ahao.commerce.address.infrastructure.repository.impl.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import moe.ahao.commerce.address.infrastructure.repository.impl.mybatis.data.AddressDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface AddressMapper extends BaseMapper<AddressDO> {
    List<AddressDO> selectListByParentCode(@Param("parentCode") String parentCode);

    List<AddressDO> selectListByCodesOrName(@Param("codes") Set<String> code, @Param("name") String name);
}

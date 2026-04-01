package com.axin.flashsale.payment.outbox;

import com.axin.flashsale.common.mq.entity.OutboxMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OutboxMapper extends BaseMapper<OutboxMessage> {

    @Select("SELECT * FROM outbox_message WHERE status = 'PENDING' AND retry_count < 5 ORDER BY create_time ASC LIMIT #{limit}")
    List<OutboxMessage> findPending(@Param("limit") int limit);

    @Update("UPDATE outbox_message SET status = #{status}, sent_time = #{sentTime}, " +
            "retry_count = retry_count + 1 WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status,
                     @Param("sentTime") LocalDateTime sentTime);

    @Update("UPDATE outbox_message SET status = 'FAILED', retry_count = retry_count + 1 WHERE id = #{id}")
    int markFailed(@Param("id") Long id);
}
package com.nowcoder.seckill.service.impl;

import com.nowcoder.seckill.common.BusinessException;
import com.nowcoder.seckill.common.ErrorCode;
import com.nowcoder.seckill.common.Toolbox;
import com.nowcoder.seckill.component.ObjectValidator;
import com.nowcoder.seckill.dao.SerialNumberMapper;
import com.nowcoder.seckill.dao.ShareRecordsMapper;
import com.nowcoder.seckill.dao.UserMapper;
import com.nowcoder.seckill.entity.SerialNumber;
import com.nowcoder.seckill.entity.ShareRecords;
import com.nowcoder.seckill.entity.User;
import com.nowcoder.seckill.service.ShareRecordsService;
import com.nowcoder.seckill.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

@Service
public class ShareRecordsImpl implements ShareRecordsService, ErrorCode{

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ShareRecordsMapper shareRecordsMapper;

    @Autowired
    private SerialNumberMapper serialNumberMapper;

    @Autowired
    private UserService userService;

    /**
     * 格式：日期 + 流水
     * 示例：20210123000000000001
     *
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private String generateOrderID() {
        StringBuilder sb = new StringBuilder();

        // 拼入日期
        sb.append(Toolbox.format(new Date(), "yyyyMMdd"));

        // 获取流水号
        SerialNumber serial = serialNumberMapper.selectByPrimaryKey("order_serial");
        Integer value = serial.getValue();

        // 更新流水号
        serial.setValue(value + serial.getStep());
        serialNumberMapper.updateByPrimaryKey(serial);

        // 拼入流水号
        String prefix = "000000000000".substring(value.toString().length());
        sb.append(prefix).append(value);

        return sb.toString();
    }

    @Transactional
    public void publish(int userId, String title ,String description) {
        // 校验用户
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new BusinessException(PARAMETER_ERROR, "指定的用户不存在！");
        }

        if (title == null
                || title.isEmpty()
                || description == null
                || description.isEmpty()) {
            throw new BusinessException(PARAMETER_ERROR, "参数不能为空！");
        }

        ShareRecords shareRecords = new ShareRecords();
        shareRecords.setId(this.generateOrderID());
        shareRecords.setUserId(userId);
        shareRecords.setTitle(title);
        shareRecords.setDescription(description);
        shareRecords.setShareTime(new Timestamp(System.currentTimeMillis()));

        shareRecordsMapper.insert(shareRecords);
    }
}

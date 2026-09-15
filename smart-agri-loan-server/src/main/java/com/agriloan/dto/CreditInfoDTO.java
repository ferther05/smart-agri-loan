package com.agriloan.dto;

import com.agriloan.domain.CreditLog;
import lombok.Data;

import java.util.List;

/**
 * 信用积分详情：前端信用页 / 首页信用卡片的数据来源
 */
@Data
public class CreditInfoDTO {

    private Integer score;

    /** 当前等级 AAA / AA / A / B / C */
    private String level;

    /** 等级评价，如"信用良好" */
    private String comment;

    /** 仪表盘刻度 */
    private Integer scoreMin;

    private Integer scoreMax;

    /** 距离下一等级还差多少分（0 表示已是最高等级或已达标） */
    private Integer toNextLevel;

    /** 下一等级名 */
    private String nextLevel;

    private List<DimensionDTO> dimensions;

    private List<CreditLog> logs;

    /** 当前等级权益（自适应下发） */
    private List<String> benefits;

    private PolicyDTO policy;
}

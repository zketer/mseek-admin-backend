package com.lynn.museum.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lynn.museum.info.dto.CheckinRecordQueryRequest;
import com.lynn.museum.info.dto.CheckinRecordResponse;
import com.lynn.museum.info.model.entity.CheckinRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 打卡记录Mapper接口
 *
 * @author lynn
 * @since 2024-12-16
 */
@Mapper
public interface CheckinRecordMapper extends BaseMapper<CheckinRecord> {

    /**
     * 分页查询打卡记录（关联用户和博物馆信息）
     *
     * @param page 分页参数
     * @param query 查询条件
     * @return 打卡记录响应列表
     */
    @Select("<script>" +
            "SELECT " +
            "  cr.id, cr.user_id, " +
            "  CONCAT('用户', cr.user_id) as user_name, " +
            "  CONCAT('用户', cr.user_id) as user_nickname, " +
            "  cr.museum_id, mi.name as museum_name, mi.address as museum_address, " +
            "  cr.checkin_time, cr.latitude, cr.longitude, cr.address, cr.remark, " +
            "  cr.audit_status, cr.audit_time, cr.audit_user_id, " +
            "  CASE WHEN cr.audit_user_id IS NOT NULL THEN CONCAT('管理员', cr.audit_user_id) ELSE NULL END as audit_user_name, " +
            "  cr.audit_remark, cr.anomaly_type, " +
            "  cr.photo_urls, cr.photos, cr.feeling, cr.rating, " +
            "  cr.mood, cr.weather, cr.companions, cr.tags, " +
            "  cr.device_info, cr.create_at, " +
            "  CASE " +
            "    WHEN cr.latitude IS NOT NULL AND cr.longitude IS NOT NULL " +
            "         AND mi.latitude IS NOT NULL AND mi.longitude IS NOT NULL " +
            "    THEN ROUND(6371 * 2 * ASIN(SQRT(" +
            "           POWER(SIN((mi.latitude - cr.latitude) * PI() / 180 / 2), 2) + " +
            "           COS(cr.latitude * PI() / 180) * COS(mi.latitude * PI() / 180) * " +
            "           POWER(SIN((mi.longitude - cr.longitude) * PI() / 180 / 2), 2)" +
            "         )) * 1000) " +
            "    ELSE NULL " +
            "  END as distance " +
            "FROM checkin_record cr " +
            "LEFT JOIN museum_info mi ON cr.museum_id = mi.id " +
            "WHERE cr.deleted = 0 " +
            "  <if test='query.userId != null'>AND cr.user_id = #{query.userId}</if>" +
            "  <if test='query.museumId != null'>AND cr.museum_id = #{query.museumId}</if>" +
            "  <if test='query.museumName != null and query.museumName != \"\"'>AND mi.name LIKE CONCAT('%', #{query.museumName}, '%')</if>" +
            "  <if test='query.auditStatus != null'>AND cr.audit_status = #{query.auditStatus}</if>" +
            "  <if test='query.anomalyType != null and query.anomalyType != \"\"'>AND cr.anomaly_type = #{query.anomalyType}</if>" +
            "  <if test='query.startTime != null'>AND cr.checkin_time >= #{query.startTime}</if>" +
            "  <if test='query.endTime != null'>AND cr.checkin_time &lt;= #{query.endTime}</if>" +
            "  <if test='query.anomalyOnly != null and query.anomalyOnly == true'>AND cr.anomaly_type IS NOT NULL</if>" +
            "ORDER BY cr.create_at DESC" +
            "</script>")
    IPage<CheckinRecordResponse> selectCheckinRecordsWithDetails(Page<CheckinRecordResponse> page, @Param("query") CheckinRecordQueryRequest query);

    // ==================== 以下为成就系统新增方法 ====================

    /**
     * 统计用户打卡次数（仅统计审核通过的记录）
     *
     * @param userId 用户ID
     * @return 打卡次数
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM checkin_record WHERE user_id = #{userId} AND audit_status = 1 AND deleted = 0" +
            "</script>")
    Integer countUserCheckins(@Param("userId") Long userId);

    /**
     * 统计用户打卡的不同博物馆数量（仅统计审核通过的记录）
     *
     * @param userId 用户ID
     * @return 不同博物馆数量
     */
    @Select("<script>" +
            "SELECT COUNT(DISTINCT museum_id) FROM checkin_record WHERE user_id = #{userId} AND audit_status = 1 AND deleted = 0" +
            "</script>")
    Integer countUserUniqueMuseums(@Param("userId") Long userId);

    /**
     * 统计用户在指定时间段内的打卡次数
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 打卡次数
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM checkin_record WHERE user_id = #{userId} AND audit_status = 1 AND deleted = 0 AND checkin_time BETWEEN #{startTime} AND #{endTime}" +
            "</script>")
    Integer countUserCheckinsInPeriod(@Param("userId") Long userId, 
                                    @Param("startTime") String startTime, 
                                    @Param("endTime") String endTime);

    /**
     * 查询用户打卡的不同博物馆列表（用于统计）
     *
     * @param userId 用户ID
     * @return 不同博物馆的记录列表
     */
    @Select("<script>" +
            "SELECT DISTINCT museum_id, museum_name FROM checkin_record WHERE user_id = #{userId} AND is_draft = 0 AND deleted = 0" +
            "</script>")
    List<CheckinRecord> selectDistinctMuseumsByUser(@Param("userId") Long userId);
}

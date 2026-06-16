package com.building.common;

public class Constants {

    private Constants() {}

    /** 角色 */
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MANAGER = "MANAGER";

    /** 房屋状态 */
    public static final String ROOM_STATUS_FREE = "空闲";
    public static final String ROOM_STATUS_IN_USE = "使用中";
    public static final String ROOM_STATUS_RENTED = "出租";
    public static final String ROOM_STATUS_SEALED = "封存";

    /** 楼宇状态 */
    public static final int BUILDING_STATUS_NORMAL = 1;
    public static final int BUILDING_STATUS_SEALED = 0;

    /** 审批状态 */
    public static final String APPROVAL_PENDING = "待审批";
    public static final String APPROVAL_APPROVED = "已通过";
    public static final String APPROVAL_REJECTED = "已驳回";

    /** 租赁状态 */
    public static final String RENTAL_ACTIVE = "有效";
    public static final String RENTAL_EXPIRED = "到期";
    public static final String RENTAL_TERMINATED = "终止";

    /** 合约状态 */
    public static final String CONTRACT_ACTIVE = "生效";
    public static final String CONTRACT_EXPIRED = "到期";
    public static final String CONTRACT_TERMINATED = "终止";
}

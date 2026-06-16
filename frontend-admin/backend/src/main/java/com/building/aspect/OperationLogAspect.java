package com.building.aspect;

import com.building.entity.OperationLog;
import com.building.mapper.OperationLogMapper;
import com.building.security.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogMapper operationLogMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterReturning("@annotation(operLog)")
    public void doAfterReturning(JoinPoint joinPoint, OperLog operLog) {
        try {
            UserContext user = UserContext.get();
            if (user == null) {
                return;
            }

            OperationLog logEntry = new OperationLog();
            logEntry.setUserId(user.getUserId());
            logEntry.setUsername(user.getUsername());
            logEntry.setModule(operLog.module());
            logEntry.setAction(operLog.action());

            // 获取方法参数作为操作详情
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String methodName = signature.getName();
            Object[] args = joinPoint.getArgs();
            String detail = methodName;
            if (args.length > 0) {
                try {
                    detail = objectMapper.writeValueAsString(args[0]);
                    if (detail.length() > 500) {
                        detail = detail.substring(0, 500) + "...";
                    }
                } catch (Exception ignored) {
                }
            }
            logEntry.setTarget(signature.getDeclaringType().getSimpleName() + "." + methodName);
            logEntry.setDetail(detail);

            // 获取请求 IP
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                logEntry.setIp(getClientIp(request));
            }

            logEntry.setCreatedAt(LocalDateTime.now());
            operationLogMapper.insert(logEntry);

            log.info("操作日志: [{}] {} - {} by {}", operLog.module(), operLog.action(), logEntry.getTarget(), user.getUsername());
        } catch (Exception e) {
            log.error("记录操作日志失败: {}", e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

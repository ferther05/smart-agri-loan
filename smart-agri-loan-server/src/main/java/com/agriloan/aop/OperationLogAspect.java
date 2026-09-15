package com.agriloan.aop;

import com.agriloan.domain.OperationLog;
import com.agriloan.repository.OperationLogRepository;
import com.agriloan.security.LoginUser;
import com.agriloan.security.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AOP 操作日志切面：自动记录所有带 {@link OpLog} 的业务操作
 *
 * <p>记录内容：操作人、角色、模块、动作、目标对象、请求参数、IP、耗时、成功/失败与异常信息。
 * 无论方法成功还是抛异常都会落一条日志（异常会继续向上抛出，不影响原有事务与返回）。</p>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private static final int MAX_TEXT = 1000;

    private final OperationLogRepository operationLogRepository;

    private final ObjectMapper objectMapper;

    private final SpelExpressionParser parser = new SpelExpressionParser();

    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(opLog)")
    public Object around(ProceedingJoinPoint joinPoint, OpLog opLog) throws Throwable {
        long start = System.currentTimeMillis();
        boolean success = true;
        String errorMsg = null;
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            success = false;
            errorMsg = throwable.getMessage();
            throw throwable;
        } finally {
            try {
                saveLog(joinPoint, opLog, success, errorMsg, System.currentTimeMillis() - start);
            } catch (Exception e) {
                // 日志失败绝不能影响主业务
                log.warn("写入操作日志失败：{}", e.getMessage());
            }
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint, OpLog opLog,
                         boolean success, String errorMsg, long costMs) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        OperationLog entity = new OperationLog();
        LoginUser loginUser = SecurityUtils.current();
        entity.setUsername(loginUser == null ? "匿名" : loginUser.getUsername());
        entity.setRole(loginUser == null ? "" : loginUser.getRoleCode());
        entity.setModule(opLog.module());
        entity.setAction(opLog.action());
        entity.setMethod(method.getDeclaringClass().getSimpleName() + "#" + method.getName());
        entity.setTarget(resolveTarget(opLog.target(), method, joinPoint.getArgs()));
        entity.setParams(buildParams(method, joinPoint.getArgs()));
        entity.setIp(currentIp());
        entity.setSuccess(success);
        entity.setErrorMsg(truncate(errorMsg, 500));
        entity.setCostMs(costMs);
        entity.setOperateTime(LocalDateTime.now());
        operationLogRepository.save(entity);
    }

    /** 解析 SpEL 目标表达式，如 "#id"、"#request.applicantName" */
    private String resolveTarget(String expression, Method method, Object[] args) {
        if (expression == null || expression.isBlank()) {
            return "";
        }
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            String[] names = nameDiscoverer.getParameterNames(method);
            if (names != null) {
                for (int i = 0; i < names.length && i < args.length; i++) {
                    context.setVariable(names[i], args[i]);
                }
            }
            Object value = parser.parseExpression(expression).getValue(context);
            return truncate(value == null ? "" : String.valueOf(value), 100);
        } catch (Exception e) {
            return "";
        }
    }

    /** 拼接请求参数摘要，屏蔽密码等敏感字段 */
    private String buildParams(Method method, Object[] args) {
        try {
            String[] names = nameDiscoverer.getParameterNames(method);
            Map<String, Object> map = new LinkedHashMap<>();
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse) {
                    continue;
                }
                String name = names != null && i < names.length ? names[i] : ("arg" + i);
                map.put(name, arg);
            }
            String json = objectMapper.writeValueAsString(map);
            json = json.replaceAll("(\"[a-zA-Z]*[Pp]assword\"\\s*:\\s*)\"[^\"]*\"", "$1\"***\"");
            return truncate(json, MAX_TEXT);
        } catch (Exception e) {
            return "";
        }
    }

    private String currentIp() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "";
        }
        HttpServletRequest request = attributes.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return null;
        }
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}

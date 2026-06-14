package org.example.project_badminton.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* org.example.project_badminton.controller..*(..)) || execution(* org.example.project_badminton.service..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;
        log.info(
                "[PERFORMANCE] User: {} | Method: {} | Duration: {} ms",
                getCurrentUser(),
                joinPoint.getSignature().toShortString(),
                executionTime
        );
        return proceed;
    }

    private String getCurrentUser() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        return (auth == null
                || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal()))
                ? "ANONYMOUS"
                : auth.getName();
    }

    // ==========================
    // CONTROLLER REQUEST
    // ==========================

    @Before("execution(* org.example.project_badminton.controller..*(..))")
    public void logRequest(JoinPoint joinPoint) {
        log.info(
                "[REQUEST] User: {} | Method: {} | Args: {}",
                getCurrentUser(),
                joinPoint.getSignature().toShortString(),
                Arrays.toString(joinPoint.getArgs())
        );
    }

    // ==========================
    // SERVICE SUCCESS / FAILED
    // ==========================

    @AfterReturning("execution(* org.example.project_badminton.service..*(..))")
    public void logSuccess(JoinPoint joinPoint) {
        log.info(
                "[SUCCESS] User: {} | Method: {}",
                getCurrentUser(),
                joinPoint.getSignature().toShortString()
        );
    }

    @AfterThrowing(
            pointcut = "execution(* org.example.project_badminton.service..*(..))",
            throwing = "ex"
    )
    public void logError(
            JoinPoint joinPoint,
            Throwable ex
    ) {
        log.error(
                "[FAILED] User: {} | Method: {} | Error: {}",
                getCurrentUser(),
                joinPoint.getSignature().toShortString(),
                ex.getMessage()
        );
    }

    // ==========================
    // BOOKING AUDIT
    // ==========================

    @AfterReturning(
            "execution(* org.example.project_badminton.service.BookingService.*(..))"
    )
    public void bookingAudit() {
        log.info(
                "[AUDIT BOOKING SUCCESS] User: {}",
                getCurrentUser()
        );
    }

    // ==========================
    // COURT AUDIT
    // ==========================

    @AfterReturning(
            "execution(* org.example.project_badminton.service.ManagerCourtService.*(..))"
    )
    public void courtAudit(JoinPoint joinPoint) {
        log.info(
                "[AUDIT COURT] User: {} | Action: {}",
                getCurrentUser(),
                joinPoint.getSignature().getName()
        );
    }
}
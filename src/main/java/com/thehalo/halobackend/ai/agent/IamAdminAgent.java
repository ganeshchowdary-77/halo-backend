package com.thehalo.halobackend.ai.agent;

import com.thehalo.halobackend.ai.agent.base.AgentContext;
import com.thehalo.halobackend.ai.agent.base.HaloAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * IAM_ADMIN agent — returns a denial message.
 * <p>
 * IAM Admins have no chatbot access per the security policy.
 * This class enforces that rule as a safety net even if the controller
 * pre-authorization check is somehow bypassed.
 */
@Component
@Slf4j
public class IamAdminAgent {

    public HaloAgent buildFor(AgentContext ctx) {
        log.warn("IamAdminAgent: IAM_ADMIN userId={} attempted AI chat access — DENIED", ctx.userId());
        return userMessage ->
                "The Halo AI assistant is not available for IAM Admin accounts. "
                + "Please use the IAM Admin Portal to manage users, verify platforms, and view audit logs.";
    }
}

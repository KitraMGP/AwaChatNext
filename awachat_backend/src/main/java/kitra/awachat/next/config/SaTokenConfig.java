package kitra.awachat.next.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置类，用于注册 Sa-Token 拦截器，自动检测登录
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器
        registry.addInterceptor(new SaInterceptor(handler -> {
            // 指定校验规则：必须登录
            SaRouter.match("/**")
                .notMatch("/user/login", "/user/register") // 排除公开接口
                .check(r -> StpUtil.checkLogin());
        }));
    }
}
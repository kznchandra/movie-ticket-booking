package com.pbs.bookingservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.*;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "API", version = "1.0"),
        security = {
                @SecurityRequirement(name = "jwt-bearer"),
                @SecurityRequirement(name = "oauth2")
        }
)
@SecuritySchemes({
        @SecurityScheme(
                name = "jwt-bearer",
                type = SecuritySchemeType.HTTP,
                scheme = "bearer",
                bearerFormat = "JWT"
        ),
        @SecurityScheme(
                name = "oauth2",
                type = SecuritySchemeType.OAUTH2,
                flows = @OAuthFlows(
                        authorizationCode = @OAuthFlow(
                                authorizationUrl = "http://localhost:9000/oauth2/authorize",
                                tokenUrl = "http://localhost:9000/oauth2/token",
                                scopes = {
                                        @OAuthScope(name = "openid", description = "OpenID scope"),
                                        @OAuthScope(name = "read", description = "Read access"),
                                        @OAuthScope(name = "write", description = "Write access")
                                }
                        )
                )
        )
})
public class OpenApiConfig {
}
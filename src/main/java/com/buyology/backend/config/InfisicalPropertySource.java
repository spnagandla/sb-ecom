package com.buyology.backend.config;

import com.infisical.sdk.InfisicalSdk;
import com.infisical.sdk.config.SdkConfig;
import com.infisical.sdk.models.Secret;
import com.infisical.sdk.util.InfisicalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InfisicalPropertySource implements ApplicationContextInitializer<ConfigurableApplicationContext> {


    private static final Logger log = LoggerFactory.getLogger(InfisicalPropertySource.class);
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        log.info("🔐 InfisicalPropertySource starting...");
        try {

            // Step 1: Read the 3 identity values from environment variables
            // System.getenv() reads variables set on the machine/pipeline/AWS
            // These are NOT the actual secrets — they are just the "login credentials" for Infisical
            String clientId     = System.getenv("INFISICAL_CLIENT_ID");
            String clientSecret = System.getenv("INFISICAL_CLIENT_SECRET");
            String projectId    = System.getenv("INFISICAL_PROJECT_ID");


            // Step 2: Create the Infisical SDK client
            // This is like opening a connection to Infisical
            // SdkConfig.Builder().build() uses default settings (connects to app.infisical.com)
            var sdk = new InfisicalSdk(
                    new SdkConfig.Builder().build()
            );


            // Step 3: Login to Infisical using the machine identity credentials
            // This is like logging in — after this, the SDK has permission to read secrets
            sdk.Auth().UniversalAuthLogin(clientId, clientSecret);

            // Step 4: Fetch all secrets from Infisical
            // Parameters explained:
            //   projectId  → which Infisical project to read from
            //   "prod"     → which environment (matches what you set in the UI)
            //   "/"        → root folder (where your secrets are stored)
            //   false      → don't expand secret references
            //   false      → don't fetch recursively from subfolders
            //   false      → don't include imported secrets
            List<Secret> secrets = sdk.Secrets().ListSecrets(
                    projectId,
                    "prod",
                    "/",
                    false,
                    false,
                    false
            );

            // Step 5: Convert the list of secrets into a simple key→value Map
            // For example: { "DB_URL" → "jdbc:postgresql://...", "DB_PASSWORD" → "abc123" }
            Map<String, Object> secretMap = new HashMap<>();
            for (Secret secret : secrets) {
                secretMap.put(secret.getSecretKey(), secret.getSecretValue());
            }

            // Step 6: Inject the secrets INTO Spring's environment
            // This is the magic step — after this, Spring treats these secrets
            // exactly like they were written in application.properties
            // "addFirst" means Infisical values take PRIORITY over anything in application.properties
            applicationContext.getEnvironment()
                    .getPropertySources()
                    .addFirst(new MapPropertySource("infisical", secretMap));

            log.info("✅ Infisical secrets loaded: " + secretMap.size() + " secrets");

        } catch (InfisicalException e) {
            throw new RuntimeException("❌ Infisical auth/fetch failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("❌ Unexpected error loading Infisical secrets: " + e.getMessage(), e);
        }
    }
}

/*
### The big picture in simple terms:
```
Normal Spring without Infisical:
  App starts → reads application.properties → finds ${DB_URL} → 💥 crash (no value)

Your app with Infisical:
  App starts → runs InfisicalPropertySource FIRST
             → logs into Infisical with 3 identity vars
             → downloads all 6 secrets
             → gives them to Spring
             → Spring reads ${DB_URL} → ✅ finds real value → app runs
 */
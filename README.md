# spring-vault-config

> Spring Boot starter that seamlessly pulls secrets from HashiCorp Vault at application startup with automatic lease renewal.

---

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>io.github.spring-vault-config</groupId>
  <artifactId>spring-vault-config-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

---

## Usage

Configure your Vault connection in `application.yml`:

```yaml
vault:
  uri: https://vault.example.com
  token: s.your-vault-token
  secret-path: secret/data/myapp
  lease-renewal-interval: 60s
```

Inject secrets directly into your Spring beans:

```java
@Configuration
public class AppConfig {

    @Value("${db.password}")
    private String dbPassword;

    // Vault secret is automatically resolved and injected at startup
}
```

Secrets are fetched from Vault on startup and bound to your Spring `Environment`, making them available via `@Value` or `@ConfigurationProperties` just like any other property source. Leases are renewed automatically in the background.

---

## Requirements

- Java 17+
- Spring Boot 3.x
- HashiCorp Vault 1.10+

---

## Contributing

Pull requests are welcome. Please open an issue first to discuss any major changes.

---

## License

This project is licensed under the [MIT License](LICENSE).
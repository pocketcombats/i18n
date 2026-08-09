# PocketCombats i18n

Small Java library for localized messages built around a serializable `LocalizedString` abstraction and a pluggable message source/formatter stack (ICU MessageFormat). It integrates with Spring Boot 4 (auto‑configuration), Jackson, and JPA.

## Features
- `LocalizedString` types: `direct`, `simple`, `formatted`, `joined`, composable and nestable
- ICU MessageFormat arguments (including other `LocalizedString` as values)
- Multiple message variants per code (e.g., `greeting`, `greeting.1`, …) with stable, per‑instance selection via an internal seed
- Message sources from UTF‑8 `.properties` files; paths support wildcards (`classpath*:`) and `;`‑separated lists
- Spring Boot auto‑configuration for the service and Jackson module
- JPA attribute converter for persisting `LocalizedString` as JSON, not as translated text

## Quick start (Spring Boot)

Add a dependency:

```xml
<dependency>
  <groupId>com.pocketcombats</groupId>
  <artifactId>i18n</artifactId>
  <version>1.5</version>
</dependency>
```

Configure your message files and defaults. Examples:

application.properties
```properties
i18n.default-locale=en
i18n.locale-to-messages.en=messages_en.properties
i18n.locale-to-messages.ru=messages_ru.properties
```

or application.yml
```yaml
i18n:
  default-locale: en
  debug-mode: false
  locale-to-messages:
    en: messages_en.properties
    ru: messages_ru.properties
```

Now use `LocalizedString` directly in your models/DTOs. The bundled Jackson module will serialize it into a translated string for the current request locale.

```java
import com.pocketcombats.i18n.LocalizedString;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

record GreetingDto(LocalizedString message) {}

@RestController
class GreetingController {
    
  @GetMapping("/greet")
  GreetingDto greet() {
    return new GreetingDto(LocalizedString.formatted(
        "greeting.with_name",
        Map.of("NAME", LocalizedString.simple("character.janny.name"))
    ));
  }
}
```

Notes:
- Auto‑config class: `com.pocketcombats.i18n.spring.I18nConfiguration` (picks up properties, wires `I18nService`, registers the Jackson module).
- Message files are read as UTF‑8, not the ISO‑8859‑1 default of Java `.properties` — no `\uXXXX` escaping needed for non‑ASCII text.
- Locale resolution uses Spring’s `LocaleContextHolder` (e.g., from `Accept-Language`). The requested locale is matched exactly, then by language only (`ru-RU` → `ru`), then falls back to `default-locale`; a matched non‑default locale still falls back to the default for any codes it is missing.
- `debug-mode: true` skips translation and returns each `LocalizedString`'s raw `toString()` (code, args, seed) — handy for spotting missing or wrong codes during development.
- `default-locale` is required, must be a BCP 47 tag (`en-US`, not `en_US`), and must have its own `locale-to-messages` entry. Invalid values fail at startup.

## Persistence (JPA)

Store `LocalizedString` as a raw value (not translated text) using the provided converter:

```java
import com.pocketcombats.i18n.LocalizedString;
import com.pocketcombats.i18n.persistence.LocalizedStringAttributeConverter;
import jakarta.persistence.*;

@Entity
class Item {
  @Id @GeneratedValue Long id;

  LocalizedString title;
}
```

The converter auto‑applies, so `LocalizedString` fields need no `@Convert` annotation.
This keeps all translation logic outside the database — translation to display text happens later, at serialization time.

## Message variants and the internal seed

Message sources may define alternative phrasings of the same code using numeric suffixes:

```properties
greeting=Hello
greeting.1=Hi
greeting.2=Hey
```

Suffixes run from `.1` to `.9`, and a gap ends the sequence: without `greeting.1`, a `greeting.2` is never used.

When resolving `LocalizedString.simple("greeting")`, one variant is selected. Selection is stable for the lifetime of that `LocalizedString` instance thanks to an internal seed.   
Creating a new instance may choose a different variant.  
Different localization files may define different numbers of variants for the same code.

## Advanced/manual usage

While most applications won’t need it, you can also resolve messages directly via `I18nService` (inject it in Spring):

```java
import com.pocketcombats.i18n.I18nService;
import com.pocketcombats.i18n.LocalizedString;
import org.springframework.stereotype.Service;

@Service
class Example {
  private final I18nService i18n;
  Example(I18nService i18n) { this.i18n = i18n; }

  public String name() {
    return i18n.getMessage(LocalizedString.simple("character.janny.name"));
  }
}
```

## License

Apache 2.0

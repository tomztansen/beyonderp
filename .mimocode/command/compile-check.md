---
description: Run Maven compile to verify code changes build successfully
---

# compile-check

Run `mvn compile -q` in the project root to verify that recent code changes compile without errors.

## Usage

```
/compile-check
```

## Steps

1. Run `mvn compile -q` from the project root (`D:\PROJECT GMN\vaadinerp`)
2. If compilation succeeds (exit code 0), report "Compilation successful"
3. If compilation fails, capture the error output and report:
   - Which file has the error
   - The exact error message
   - The line number if available
4. Do NOT attempt to fix errors automatically — just report them

## Notes

- This project uses Java 21, Spring Boot 3.3.0, Vaadin 24.10.7
- The `-q` flag suppresses non-error output for cleaner results
- Typical compile time: 30-60 seconds

# Conventional Commits

This project uses [Conventional Commits](https://www.conventionalcommits.org/) for commit messages.

## Format

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

## Types

- **feat**: A new feature
- **fix**: A bug fix
- **docs**: Documentation only changes
- **style**: Changes that do not affect the meaning of the code (formatting, semicolons, etc.)
- **refactor**: Code change that neither fixes a bug nor adds a feature
- **test**: Adding or correcting tests
- **chore**: Changes to build process, dependencies, or auxiliary tools

## Examples

```
feat: add user authentication API

fix: resolve memory leak in connection pool

docs: update API documentation

refactor: simplify error handling in service layer

chore: update dependencies
```

## References

In PRs/commits, reference issues like `Closes #123` or `Fixes #456`.
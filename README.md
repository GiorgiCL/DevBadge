This project is being built as a Spring Boot application that analyzes a GitHub user’s public activity and generates a developer performance score based on commits, pull requests, issues, and repository activity.

Below is a summary of what has been implemented so far.

✅ 1. Project Setup

Spring Boot 3.5.x backend created and configured.

MySQL database initialized (devbadge).

Lombok added to reduce boilerplate code.

GitHub personal access token and base API URL configured via application.properties.

Custom RestTemplate bean created for authenticated GitHub API communication.

✅ 2. GitHub API Configuration

Implemented a dedicated configuration class:

GitHubApiConfig.java

Key features:

Injects GitHub API URL and token using @Value.

Builds a specialized RestTemplate with:

Authorization header (Bearer <token>)

GitHub API headers

Request timeouts

Root URI for cleaner endpoint calls

Registered as a named bean (githubRestTemplate) for injection.

This component is responsible for all outbound communication to GitHub.

✅ 3. DTO Layer (Mapping GitHub JSON → Java Objects)

Created DTOs for all GitHub data the project needs:

3.1 User Data

GitHubUserDTO

Stores profile information (login, id, bio, followers, etc.)

3.2 Repository Data

GitHubRepoDTO

Maps metadata for each repository (name, stars, forks, timestamps).

3.3 Commit Data

GitHubCommitDTO

Includes nested structures for commit details and stats.

Used to analyze commit quality, consistency, and impact.

3.4 Pull Request Data

GitHubPullRequestDTO

Captures PR metadata such as review comments, commit counts, and merge info.

3.5 Issue Data

GitHubIssueDTO

Maps issue information, including timestamps and comment counts.

Identifies whether an issue is actually a pull request.

All DTOs use:

Lombok (@Data) for getters/setters

Jackson (@JsonProperty) for mapping GitHub’s snake_case fields

✅ 4. GitHubApiService (Data Retrieval Layer)

Central service responsible for making authenticated requests to GitHub.

Implemented methods:

fetchUserProfile(username)

fetchUserRepositories(username)

fetchRepositoryCommits(username, repo, limit)

fetchPullRequests(username, repo)

fetchIssues(username, repo)

Each method:

Uses the configured githubRestTemplate

Handles errors with HttpClientErrorException

Logs activity using @Slf4j

Returns clean Java objects or empty lists

Uses ParameterizedTypeReference for mapping JSON arrays

This service is now capable of retrieving all fundamental GitHub data required for scoring.

📌 Next Steps

The next phase will introduce:

A test controller to validate API integration.

User entity and database persistence.

Scoring logic for commits, PRs, issues, consistency, and impact.

The main analysis workflow that aggregates all user activity.

📌 Current Status

The core foundation for GitHub integration is complete:

✔ Configuration
✔ DTO mapping
✔ API communication
✔ Error handling
✔ Logging
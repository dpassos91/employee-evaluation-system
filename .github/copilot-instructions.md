# Copilot Instructions for ProjectoFinalAoR_grupo7

## Project Overview
This is a full-stack web application for training and evaluation management, split into a Java backend (Jakarta EE, Maven, Wildfly) and a React frontend (Create React App, Tailwind CSS).

## Architecture & Key Patterns
- **Backend** (`backend/`):
  - Java, Jakarta EE, RESTful APIs, Hibernate, PostgreSQL.
  - Main code: `src/main/java/aor/projetofinal/` (see `entity/`, `dao/`, `service/`, `websocket/`).
  - Endpoints follow `/rest/{resource}` pattern, e.g., `/rest/courses`, `/rest/evaluations`.
  - Uses DTOs for API responses, DAOs for persistence, and services for business logic.
  - Logging: Log4j2 (`log4j2.xml`).
  - Tests: JUnit, Mockito, Selenium (see `src/test/java/`).
  - Build/deploy: `mvn clean package` (WAR output in `target/`).
  - Wildfly plugin for local deployment (see `pom.xml`).

- **Frontend** (`frontend/`):
  - React (JS), Tailwind CSS, state managed via custom stores (see `src/stores/`).
  - API layer: All endpoints centralized in `src/api/apiConfig.js` and resource-specific files (e.g., `courseAPI.js`).
  - Pages in `src/pages/`, reusable components in `src/components/`.
  - User/session state in `userStore.js`.
  - Run: `npm start` (dev), `npm run build` (prod), `npm test` (unit tests).
  - Uses JWT for auth, stored in sessionStorage as `authToken`.

## Developer Workflows
- **Backend**:
  - Build: `cd backend && mvn clean package`
  - Deploy: Wildfly auto-deploys WAR from `target/` (see plugin config in `pom.xml`).
  - Test: `mvn test` (unit/integration), Selenium for UI tests.
  - Logs: `backend/logs/app.log`

- **Frontend**:
  - Dev: `cd frontend && npm start`
  - Build: `npm run build` (output in `build/`)
  - Test: `npm test`
  - API endpoints: Update `apiConfig.js` for new/changed backend routes.

## Project Conventions
- **API**: All API calls use `apiCall` from `apiConfig.js`, which injects JWT and handles errors/session expiry.
- **State**: Use custom stores in `src/stores/` for global state (user, notifications, chat).
- **Components**: Reusable UI in `src/components/`, pages in `src/pages/`.
- **Translations**: User-facing strings can be translated via `userStore.translations`.
- **Testing**: Place tests alongside code (`*.test.js` for frontend, `src/test/java/` for backend).

## Integration Points
- **Frontend ↔ Backend**: All communication via REST endpoints defined in `apiConfig.js`.
- **Database**: PostgreSQL, configured in backend (see `pom.xml` and likely `resources/`).
- **Authentication**: JWT-based, managed in frontend/sessionStorage, validated by backend.

## Examples
- Add a new API endpoint: Update `apiConfig.js` and create a new API helper in `src/api/`.
- Add a new page: Create a file in `src/pages/`, add route in main app.
- Add a backend entity: Create in `entity/`, add DAO/service, expose via REST in `websocket/`.

Refer to `frontend/README.md` and `backend/pom.xml` for more details on scripts and dependencies.

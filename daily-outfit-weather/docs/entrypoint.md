# Project Entrypoint: Daily Outfit Weather

이 문서는 프로젝트의 모든 설계, 규칙, 작업 기록을 연결하는 중앙 인덱스입니다.

## ⚡ Mandatory Feature Development Trigger Rule
어떠한 신규 기능을 개발하거나 기존 코드를 수정하더라도, **모든 작업의 첫 번째 필수 단계(Trigger)**로서 [`Impact Analysis Guide`](./impact-analysis-guide.md)를 조회(`view_file`)하여 사전 영향도를 파악하고 하위 호환 대책을 먼저 수립해야 합니다.

## Getting Started
- [README](../README.md): 프로젝트 개요 및 로컬 실행 가이드
- [Development Plan](./development-plan.md): Phase 1~9 구현 로드맵 (필수 트리거 포함)

## Project Standards
- [Architecture](./architecture.md): 대표 아키텍처 문서
- [Impact Analysis Guide](./impact-analysis-guide.md): 신규 기능 추가 및 변경 시 시스템 영향도 분석 가이드라인
- [Feature Development Trigger](./project/standards/feature_development_trigger.md): 기능 추가 시 필수 사전 트리거 규칙
- [Security Deployment Policy](./security-deployment-policy.md): 운영 전 CSRF/CORS/cookie/OAuth smoke test 기준
- [Operations DB Runbook](./operations-db-runbook.md): 운영 DB 백업, 복구, migration rollback 절차
- [Operations Logs and Monitoring Runbook](./operations-logs-monitoring-runbook.md): 운영 로그 확인, 보관, 모니터링, 장애 triage 절차
- [Architecture Overlay](./project/standards/architecture.md): Harness Kit overlay용 아키텍처 진입점
- [Coding Conventions](./project/standards/coding_conventions_project.md): Java 21, React, 도메인 기반 폴더 구조 규칙
- [Testing Profile](./project/standards/testing_profile.md): JUnit 5 및 Vitest 기반 테스트 전략

## Decision Logs
- [Decision Log](./decisions/README.md): 대표 결정 기록
- [Decision Overlay](./project/decisions/README.md): Harness Kit overlay용 결정 기록 진입점

## Task Workspaces
- [Task 001: Project Bootstrap](./task/001_project_bootstrap/issue.md): 초기 환경 구축 (진행 중)
- [Task 012: Weekly Weather Forecast & Outfit Outlook](./task/012_weekly_weather_forecast/requirements.md): 주간 날씨 및 옷차림 예보 신규 기능 (기획/사전영향도 완료)

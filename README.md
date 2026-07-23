# 📍 Multitrack

**A Spring Boot + AWS app that tracks multiple parallel learning/goal tracks and flags the ones you've quietly stopped working on.**

## Why this exists

Juggling several long-term goals at once (a cert, a language, a side project, a personal learning roadmap) makes it easy to lose track of which one you've silently dropped. Multitrack solves a real problem: it doesn't just log progress, it actively surfaces the track that's gone quiet before you notice yourself.

## Motivation

This started as a personal problem: juggling an AWS certification path, a self-directed hardware learning track, a language, and a couple of side projects at once made it easy to quietly stop working on one without noticing. Multitrack exists to catch that before I do.

## What it does

- Create multiple **Tracks** (e.g. "AWS SAA-C03", "IC Design Roadmap", "Guardrail project")
- Break each Track into **Milestones**
- **Check in** daily with a short activity log per Track
- View a **dashboard** showing which Tracks are active and which have gone stale
- Get an **automatic email nudge** when a Track has had no activity for N days

## Architecture

This is a monolith with one async task decoupled — not a distributed system. The scale of the problem (one user, a handful of tracks) doesn't call for one, and keeping the core simple was a deliberate choice, not an oversight.

```
Client → ALB → Spring Boot app (ECS/Fargate)
                     │
        Spring Data JPA → RDS (PostgreSQL)
                     │
        Spring Scheduler — daily check for stale tracks
                     │
              publishes event → SQS
                     │
              Lambda worker consumes → sends reminder via SES
```

The notification path is the one piece pulled out of the main app: a short, infrequent (once-daily) task doesn't need a server sitting idle waiting for it, so it runs as Lambda instead of inside the always-on Spring Boot service.

## Tech stack

**Core:** Java 17, Spring Boot 4.1, Spring Web, Spring Data JPA, PostgreSQL, Spring Validation

**AWS:** RDS, ECS/Fargate, ALB, SQS, Lambda, SES, CloudWatch

**Local dev:** Docker (PostgreSQL container)

**Later addition:** Spring Security (JWT) once core CRUD is stable

## Domain model

- `Track` — a long-running goal
- `Milestone` — a step within a Track
- `ActivityLog` — a check-in entry with a timestamp

## Status

🚧 Work in progress. Built as a personal tool first, portfolio project second — genuinely used to keep track of the author's own parallel learning goals.

## License

MIT
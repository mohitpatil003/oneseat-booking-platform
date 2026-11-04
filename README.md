# OneSeat

**A seat is sold exactly once, even when thousands of users compete for it.**

## Problem
Popular events sell out in seconds, and naive systems oversell seats or double-charge customers under heavy concurrent load.

## Goals
- Zero oversold seats and zero double charges under concurrent load
- Measured latency and throughput, with a naive-versus-fixed comparison
- Deployed on Kubernetes with CI/CD and monitoring

## Stack
Java 21, Spring Boot 3, PostgreSQL, Redis, Kafka, Docker, Kubernetes

## Status
Week 1: setup, requirements, design
